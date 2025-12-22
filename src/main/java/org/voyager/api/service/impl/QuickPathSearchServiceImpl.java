package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.FlightDetailed;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.QuickPathSearchService;
import org.voyager.api.service.RouteService;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightAirlineQuery;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.route.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuickPathSearchServiceImpl implements QuickPathSearchService {
    @Autowired
    RouteService routeService;

    @Override
    public List<Path> findQuickPaths(PathSearchRequest request) {
        Set<String> originSet = request.getOrigins();
        Set<String> destinationSet = request.getDestinations();
        List<Path> pathList = new ArrayList<>();
        // for each origin -> destination, check for direct routes
        for (String origin : originSet) {
            for (String destination : destinationSet) {
                if (request.getExcludeDestinations().contains(destination)) continue;
                Option<Route> routeOption = routeService.getRoute(origin,destination);
                // add empty route
                if (routeOption.isEmpty()) {
                    pathList.add(Path.builder().routeList(List.of(
                            Route.builder().origin(origin).destination(destination).build()))
                            .build());
                } else {
                    Route route = routeOption.get();
                    pathList.add(Path.builder()
                            .routeList(List.of(route))
                            .totalDistanceKm(route.getDistanceKm())
                            .build());
                }
            }
        }
        return pathList;
    }

    @Override
    public void streamDirectPaths(PathSearchRequest request, Consumer<Path> pathConsumer) {
        Set<String> originSet = request.getOrigins();
        Set<String> destinationSet = request.getDestinations();
        // for each origin -> destination, check for direct routes
        for (String origin : originSet) {
            for (String destination : destinationSet) {
                if (request.getExcludeDestinations().contains(destination)) continue;
                Option<Route> routeOption = routeService.getRoute(origin,destination);
                // add empty route
                if (routeOption.isEmpty()) {
                    pathConsumer.accept(Path.builder().routeList(List.of(
                                    Route.builder().origin(origin).destination(destination).build()))
                            .build());
                } else {
                    Route route = routeOption.get();
                    pathConsumer.accept(Path.builder()
                            .routeList(List.of(route))
                            .totalDistanceKm(route.getDistanceKm())
                            .build());
                }
            }
        }
    }
}
