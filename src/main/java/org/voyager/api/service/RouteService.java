package org.voyager.api.service;

import io.vavr.control.Option;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.route.*;

import java.util.List;
import java.util.Set;

// TODO: implement method to invalidate delta caches
public interface RouteService {
    Boolean originExists(String origin);
    Boolean destinationExists(String destination);
    Route patchRoute(Integer id, RoutePatch routePatch);
    Option<Route> getRouteById(Integer id);
    List<Route> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airlineOption);
    Route save(RouteForm routeForm);
    List<RoutePath> getRoutePathList(Set<String> originSet, Set<String> destinationSet,
                                     Integer limit,
                                     Set<String> excludeAirportCodes,
                                     Set<Integer> excludeRouteIds,
                                     Set<String> excludeFlightNumbers);

    PathResponse<AirlinePath> getAirlinePathList(Set<String> originSet, Set<String> destinationSet,
                                                 Option<Airline> airlineOption, Integer limit,
                                                 Set<String> excludeAirportCodes,
                                                 Set<Integer> excludeRouteIds,
                                                 Set<String> excludeFlightNumbers);

    Option<Route> getRoute(String origin, String destination);
}
