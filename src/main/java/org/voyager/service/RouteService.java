package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.route.Path;
import org.voyager.model.route.PathAirline;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;

import java.util.List;
import java.util.Set;

// TODO: implement method to invalidate delta caches
public interface RouteService {
    Boolean originExists(String origin);
    Boolean destinationExists(String destination);
    Option<Route> getRouteById(Integer id);
    List<Route> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airlineOption);
    Route save(RouteForm routeForm);
    List<PathAirline> getAirlinePathList(String origin, String destination,
                                         Option<Airline> airlineOption,
                                         Integer limit,
                                         Set<String> excludeAirportCodes,
                                         Set<Integer> excludeRouteIds,
                                         Set<String> excludeFlightNumbers);

    List<Path> getPathList(String origin, String destination, Integer limit, Set<String> excludeAirportCodes, Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers);
}
