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
import org.voyager.commons.model.route.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuickPathSearchServiceImpl implements QuickPathSearchService {
    @Autowired
    RouteService routeService;

    @Autowired
    FlightService flightService;

    @Override
    public List<PathDetailed> findQuickPaths(PathSearchRequest request) {
        Set<String> originSet = request.getOrigins();
        Set<String> destinationSet = request.getDestinations();
        List<PathDetailed> pathDetailedList = new ArrayList<>();
        List<Route> routeList = new ArrayList<>();

        // for each origin -> destination, check for direct routes
        for (String origin : originSet) {
            for (String destination : destinationSet) {
                if (request.getExcludeDestinations().contains(destination)) continue;
                Option<Route> routeOption = routeService.getRoute(origin,destination);
                // if no routes, add no direct flights path
                if (routeOption.isEmpty()) {
                    pathDetailedList.add(PathDetailed.noDirectFlights(origin,destination));
                } else {
                    Route route = routeOption.get();
                    if (request.getExcludeRouteIds().contains(route.getId())) continue;
                    routeList.add(routeOption.get());
                }
            }
        }

        // if no routes at all, return path list
        if (routeList.isEmpty()) {
            return pathDetailedList;
        }

        // get all flights of route list
        Map<Integer, Route> routeMap = routeList.stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));
        List<Integer> allRouteIds = routeMap.keySet().stream().toList();
        List<Flight> allFlights = flightService.getFlights(
                FlightAirlineQuery.builder()
                        .isActive(true)
                        .routeIdList(allRouteIds)
                        .startTime(request.getStartTime())
                        .endTime(request.getStartTime().plusDays(1L))
                        .airlineList(request.getAirlines())
                        .build());

        // if no flights, add all no direct flight paths and return
        if (allFlights.isEmpty()) {
            pathDetailedList.addAll(routeList.stream().map(route->
                    PathDetailed.noDirectFlights(route.getOrigin(),route.getDestination())).toList());
            return pathDetailedList;
        }

        // Filter excluded flight numbers : TODO: add exclude flights to flightquery?
        if (request.getExcludeFlightNumbers() != null) {
            allFlights = allFlights.stream()
                    .filter(flight -> !request.getExcludeFlightNumbers().contains(flight.getFlightNumber()))
                    .toList();
        }

        // Group by route ID
        Map<Integer, List<Flight>> flightsByRoute = allFlights.stream()
                .collect(Collectors.groupingBy(Flight::getRouteId));

        // Create PathDetailed for each route group
        pathDetailedList.addAll(flightsByRoute.entrySet().stream()
                .map(entry -> {
                    Integer routeId = entry.getKey();
                    List<Flight> routeFlights = entry.getValue();
                    Route route = routeMap.get(routeId);

                    // Convert to FlightDetailed and sort by departure time
                    List<FlightDetailed> flightDetaileds = routeFlights.stream()
                            .map(flight -> FlightDetailed.create(flight, route))
                            .sorted(Comparator.comparing(FlightDetailed::getZonedDateTimeDeparture))
                            .toList();

                    return new PathDetailed(flightDetaileds.stream().map(List::of).toList());
                })
                .toList());
        return pathDetailedList;
    }
}
