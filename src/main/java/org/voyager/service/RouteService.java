package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.route.Path;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

import java.util.List;
import java.util.Set;

// TODO: implement method to invalidate delta caches
public interface RouteService {
    Boolean originExists(String origin);
    Boolean destinationExists(String destination);
    Option<Route> getRouteById(Integer id);
    Option<Route> getByOriginAndDestinationAndAirline(String origin, String destination, Airline airline);
    List<Route> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline);
    List<Route> getActiveRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline,Boolean isActive);
    Route save(RouteForm routeForm);
    Route patch(Route route,RoutePatch routePatch);
    Path buildPathWithExclusions(String origin, String destination, Set<String> exclusions);
}
