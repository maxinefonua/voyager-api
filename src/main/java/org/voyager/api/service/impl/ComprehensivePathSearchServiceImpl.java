package org.voyager.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.ComprehensivePathSearchService;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteQuery;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ComprehensivePathSearchServiceImpl implements ComprehensivePathSearchService {
    @Autowired
    RouteService routeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensivePathSearchServiceImpl.class);

    @Override
    public void streamPaths(PathSearchRequest request, Consumer<Path> pathConsumer) {
        Set<String> excludeCodes = new HashSet<>(request.getExcludeDestinations());
        excludeCodes.addAll(request.getDestinations());

        List<Route> routeList = routeService.getRoutes(
                        RouteQuery.builder()
                                .originList(new ArrayList<>(request.getOrigins()))
                                .excludeRouteIdSet(request.getExcludeRouteIds())
                                .excludeDestinationSet(excludeCodes)
                                .build());

        Queue<Path> queue = routeList.stream()
                .filter(route -> route.getDistanceKm() != null)
                .map(route -> Path.builder()
                        .routeList(List.of(route))
                        .totalDistanceKm(route.getDistanceKm())
                        .build())
                .collect(Collectors.toCollection(
                        () -> new PriorityQueue<>(Comparator.comparing(Path::getTotalDistanceKm))
                ));
        processQueue(queue, request, pathConsumer);
    }

    private void processQueue(Queue<Path> queue,
                              PathSearchRequest request,
                              Consumer<Path> pathConsumer) {
        Set<String> visited = new HashSet<>(request.getExcludeDestinations());
        visited.addAll(request.getOrigins());
        Set<Integer> excludeRouteIds = request.getExcludeRouteIds();
        Set<String> destinationSet = request.getDestinations();

        while (!queue.isEmpty()) {
            Queue<Path> nextQueue = new PriorityQueue<>(Comparator
                    .comparing(Path::getRouteList,Comparator.comparing(List::size))
                    .thenComparing(Path::getTotalDistanceKm));
            while (!queue.isEmpty()) {
                Path pathSoFar = queue.poll();
                List<Route> soFarRouteList = pathSoFar.getRouteList();
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
                        pathConsumer.accept(nextPath);
                    } else if (nextPath.getRouteList().size() < 3) {
                        nextQueue.add(nextPath);
                    }
                }
            }
            queue = nextQueue;
        }
        LOGGER.info("completed processing queue for {}:{} for {}",
                request.getOrigins().stream().sorted().collect(Collectors.toList()),
                request.getDestinations().stream().sorted().collect(Collectors.toList()),
                request.getStartTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }


}
