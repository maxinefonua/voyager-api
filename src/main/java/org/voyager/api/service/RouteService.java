package org.voyager.api.service;

import io.vavr.control.Option;
import org.springframework.validation.annotation.Validated;
import org.voyager.commons.model.route.*;
import org.voyager.commons.model.path.route.RoutePath;
import java.util.List;
import java.util.Set;

// TODO: implement method to invalidate delta caches
public interface RouteService {
    Boolean originExists(String origin);
    Boolean destinationExists(String destination);
    Route patchRoute(Integer id, RoutePatch routePatch);
    Option<Route> getRouteById(Integer id);
    List<Route> getRoutes();
    List<Route> getRoutes(@Validated RouteQuery routeQuery);
    Route save(RouteForm routeForm);
    List<RoutePath> getRoutePathList(Set<String> originSet, Set<String> destinationSet,
                                     Integer limit,
                                     Set<String> excludeAirportCodes,
                                     Set<Integer> excludeRouteIds,
                                     Set<String> excludeFlightNumbers);

    Option<Route> getRoute(String origin, String destination);
    boolean existsById(Integer integer);
}
