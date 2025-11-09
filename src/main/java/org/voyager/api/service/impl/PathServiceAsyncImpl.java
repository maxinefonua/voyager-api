package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.FlightSearchStatus;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathDetailedResponse;
import org.voyager.api.model.query.PathQuery;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.api.service.FlightSearchService;
import org.voyager.api.service.PathService;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.path.airline.AirlinePath;
import org.voyager.commons.model.path.airline.PathAirlineQuery;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class PathServiceAsyncImpl implements PathService {
    @Autowired
    ThreadPoolExecutor taskExecutor;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    FlightSearchService flightSearchService;

    @Override
    public PathDetailedResponse getPathDetailedList(PathQuery pathQuery) {
        String flightSearchId = UUID.randomUUID().toString();

        // Return immediate partial results (e.g., direct flights)
        List<PathDetailed> immediateResults = flightSearchService.findDirectFlights(pathQuery.getOriginSet(), pathQuery.getDestinationSet());
        immediateResults = immediateResults.stream().limit(pathQuery.getPageSize()).collect(Collectors.toList());

        // Start background search for more complex routes
//        CompletableFuture<List<PathDetailed>> backgroundSearch =
//                CompletableFuture.supplyAsync(() ->
//                        flightSearchService.findComplexRoutes(pathQuery.getOriginSet(), pathQuery.getDestinationSet()), taskExecutor);
//
//        // Store the future in cache
//        cacheManager.getCache("backgroundSearches").put(flightSearchId, backgroundSearch);

        FlightSearchStatus status = immediateResults.size() >= pathQuery.getPageSize() ?
                FlightSearchStatus.COMPLETED : FlightSearchStatus.IN_PROGRESS;

        return PathDetailedResponse.builder().flightSearchId(flightSearchId).pathDetailedList(immediateResults)
                .totalPathCount(immediateResults.size()).page(0).pageSize(pathQuery.getPageSize())
                .flightSearchStatus(status).build();
    }

    @Override
    public PagedResponse<AirlinePath> getAirlinePathList(PathAirlineQuery pathAirlineQuery) {
        return null;
    }

    @Override
    public PagedResponse<Path> getPathList(PathAirlineQuery pathAirlineQuery) {
        return null;
    }

    public PathDetailedResponse getNextPage(String searchId, int page, int size) {
        // Check if background search is complete
        CompletableFuture<List<PathDetailed>> backgroundSearch =
                cacheManager.getCache("backgroundSearches").get(searchId, CompletableFuture.class);

        if (backgroundSearch != null && backgroundSearch.isDone()) {
            try {
                List<PathDetailed> allResults = backgroundSearch.get();
                // Combine with immediate results and paginate
                List<PathDetailed> pageResults = paginate(allResults, page, size);
                return PathDetailedResponse.builder().flightSearchId(searchId).pathDetailedList(pageResults)
                        .totalPathCount(allResults.size()).page(page).pageSize(size)
                        .flightSearchStatus(FlightSearchStatus.COMPLETED).build();
            } catch (Exception e) {
                // Handle background search failure
            }
        }

        // Background search still running - return what we have or indicate loading
        return PathDetailedResponse.builder().flightSearchId(searchId).pathDetailedList(Collections.EMPTY_LIST)
                .totalPathCount(-1).page(page).pageSize(size)
                .flightSearchStatus(FlightSearchStatus.IN_PROGRESS).build();
    }

    private List<PathDetailed> paginate(List<PathDetailed> allPaths, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= allPaths.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + size, allPaths.size());
        return allPaths.subList(fromIndex, toIndex);
    }
}
