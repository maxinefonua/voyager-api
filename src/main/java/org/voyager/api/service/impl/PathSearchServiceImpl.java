package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.path.SearchSession;
import org.voyager.api.model.response.SearchResponse;
import org.voyager.api.model.response.SearchStatus;
import org.voyager.api.service.*;
import org.voyager.api.manager.SearchSessionManager;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteQuery;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PathSearchServiceImpl implements PathSearchService {
    private volatile boolean enrichmentPaused = false;

    @Autowired
    RouteService routeService;

    @Autowired
    AirlineService airlineService;

    @Autowired
    QuickPathSearchService quickPathSearchService;

    @Autowired
    ComprehensivePathSearchService comprehensivePathSearchService;

    @Autowired
    private SearchSessionManager sessionManager;

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    public boolean isPausedForEnrichment() {
        return enrichmentPaused;
    }

    @Override
    public void setPauseForEnrichment(boolean pauseForEnrichment) {
        enrichmentPaused = pauseForEnrichment;
    }

    @Override
    public SearchResponse searchPaths(PathSearchRequest request) {
        if (enrichmentPaused) {
            // TODO: better handling of this
            return SearchResponse.builder().content(List.of()).hasMore(false).status(SearchStatus.PAUSED).build();
        }
        // Phase 1: Get direct paths from quick search
        List<PathDetailed> directPaths = quickPathSearchService.findQuickPaths(request, request.getSize());

        String searchId = sessionManager.createSession(request,directPaths.size());

        // Start background search for more results
        startBackgroundSearch(searchId, request);

        return buildFirstResponse(searchId,directPaths);
    }

    @Override
    public SearchResponse getMorePaths(String searchId, int size) {
        if (enrichmentPaused) {
            // TODO: better handling of this
            return SearchResponse.builder().content(List.of()).hasMore(false).status(SearchStatus.PAUSED).build();
        }
        SearchSession session = sessionManager.getSession(searchId);

        // Try to get results from background search
        List<PathDetailed> additionalResults = session.getNextBatch(size);
        boolean hasMore = session.hasMoreResults() || !session.isSearchComplete();

        return SearchResponse.builder()
                .searchId(searchId)
                .content(additionalResults)
                .status(session.isSearchComplete() ? SearchStatus.COMPLETE : SearchStatus.SEARCHING)
                .hasMore(hasMore)
                .totalFound(session.getTotalFound().get())
                .expiresAt(session.getExpiresAt())
                .build();
    }

    private SearchResponse buildFirstResponse(String searchId, List<PathDetailed> content) {
        return SearchResponse.builder()
                .searchId(searchId)
                .content(content)
                .status(SearchStatus.SEARCHING)
                .hasMore(true)
                .totalFound(content.size()) // Could be enhanced to track total
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build();
    }

    private void startBackgroundSearch(String searchId, PathSearchRequest request) {
        taskExecutor.execute(() -> {
            SearchSession session = sessionManager.getSession(searchId);
            try {
                comprehensivePathSearchService.streamPaths(
                        request,
                        session::addResult
                );
                session.markComplete();
            } catch (Exception e) {
                session.markFailed();
            }
        });
    }

    private List<Path> processQueue(Queue<Path> queue,
                                    Set<String> destinationSet,
                                    Set<String> visited,
                                    Set<Integer> excludeRouteIds,
                                    List<Path> results,
                                    List<Airline> validAirlines) {
        while (!queue.isEmpty()) {
            Queue<Path> nextQueue = new PriorityQueue<>(Comparator
                    .comparing(Path::getRouteList,Comparator.comparing(List::size))
                    .thenComparing(Path::getTotalDistanceKm));
            while (!queue.isEmpty()) {
                Path pathSoFar = queue.poll();
                List<Route> soFarRouteList = pathSoFar.getRouteList();
                if (soFarRouteList.size() > 3) return results;
                String nextOrigin = soFarRouteList.get(soFarRouteList.size()-1).getDestination();
                visited.add(nextOrigin);
                for (Route route : routeService.getRoutes(RouteQuery.builder()
                        .originList(List.of(nextOrigin))
                        .excludeDestinationSet(visited)
                        .excludeRouteIdSet(excludeRouteIds)
                        .build())) {
                    if (route.getDistanceKm() == null) continue; // TODO: validate route data
                    List<Route> routeList = new ArrayList<>(soFarRouteList);
                    routeList.add(route);
                    Path nextPath = Path.builder()
                            .routeList(routeList)
                            .totalDistanceKm(route.getDistanceKm() + pathSoFar.getTotalDistanceKm())
                            .build();
                    if (destinationSet.contains(route.getDestination())) {
                        results.add(nextPath);
                        if (results.size() == 10) return results;
                    } else {
                        List<String> iataList = new ArrayList<>(routeList.stream().map(Route::getOrigin).toList());
                        iataList.add(route.getDestination());
                        if (airlineService.hasAnyActiveAirlineForAllAirports(validAirlines,iataList)) {
                            nextQueue.add(nextPath);
                        }
                    }
                }
            }
            queue = nextQueue;
        }
        return results;
    }
}
