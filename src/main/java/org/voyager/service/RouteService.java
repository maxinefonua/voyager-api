package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;

import java.util.List;
import java.util.Optional;

public interface RouteService {
    Option<RouteDisplay> getRouteById(Integer id);
    Option<RouteDisplay> getByOriginAndDestinationAndAirline(String origin, String destination, Airline airline);
    List<RouteDisplay> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airlineOption);
    RouteDisplay save(RouteForm routeForm);
}
