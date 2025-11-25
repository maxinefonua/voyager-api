package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.FlightDetailed;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.QuickPathSearchService;
import org.voyager.api.service.RouteService;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightAirlineQuery;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuickPathSearchServiceImpl implements QuickPathSearchService {
    @Autowired
    RouteService routeService;

    @Autowired
    AirlineService airlineService;

    @Autowired
    FlightService flightService;

    @Override
    public List<PathDetailed> findQuickPaths(PathSearchRequest request, int limit) {
        Set<String> originSet = request.getOrigins();
        Set<String> destinationSet = request.getDestinations();

        List<Route> routeList = routeService.getRoutes(RouteQuery.builder()
                .originList(new ArrayList<>(originSet))
                .destinationList(new ArrayList<>(destinationSet))
                .excludeDestinationSet(request.getExcludeDestinations())
                .excludeRouteIdSet(request.getExcludeRouteIds())
                .build());

        // Get route IDs and create lookup map
        List<Integer> allRouteIds = routeList.stream()
                .map(Route::getId)
                .toList();

        Map<Integer, Route> routeMap = routeList.stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));

        // Get all flights
        List<Flight> allFlights = flightService.getFlights(
                FlightAirlineQuery.builder()
                        .isActive(true)
                        .routeIdList(allRouteIds)
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .airlineList(request.getAirlines())
                        .build());

        if (allFlights.isEmpty()) return List.of();

        // Filter excluded flight numbers
        if (request.getExcludeFlightNumbers() != null) {
            allFlights = allFlights.stream()
                    .filter(flight -> !request.getExcludeFlightNumbers().contains(flight.getFlightNumber()))
                    .toList();
        }

        // Group by route ID
        Map<Integer, List<Flight>> flightsByRoute = allFlights.stream()
                .collect(Collectors.groupingBy(Flight::getRouteId));

        // Create PathDetailed for each route group
        return flightsByRoute.entrySet().stream()
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
                .limit(limit)
                .toList();
    }
}
