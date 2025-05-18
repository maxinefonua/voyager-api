package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.route.PathDisplay;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

import java.util.List;
import java.util.Set;

public interface RouteService {
    Boolean originExists(String origin);
    Boolean destinationExists(String destination);
    Option<RouteDisplay> getRouteById(Integer id);
    Option<RouteDisplay> getByOriginAndDestinationAndAirline(String origin, String destination, Airline airline);
    List<RouteDisplay> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline);
    List<RouteDisplay> getActiveRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline,Boolean isActive);
    RouteDisplay save(RouteForm routeForm);
    RouteDisplay patch(RouteDisplay routeDisplay,RoutePatch routePatch);
    PathDisplay buildPathWithExclusions(String origin, String destination, Set<String> exclusions);
}
