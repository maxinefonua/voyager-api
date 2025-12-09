package org.voyager.api.service;

import io.vavr.control.Option;
import org.springframework.validation.annotation.Validated;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.commons.model.route.RouteQuery;
import org.voyager.commons.model.route.RouteForm;
import java.util.List;

public interface RouteService {
    boolean existsById(Integer id);
    Route patchRoute(RouteEntity routeEntity, RoutePatch routePatch);
    Option<Route> getRouteById(Integer id);
    Option<RouteEntity> getRouteEntityById(Integer id);
    List<Route> getRoutes();
    List<Route> getRoutes(@Validated RouteQuery routeQuery);
    Route save(RouteForm routeForm);
    Option<Route> getRoute(String origin, String destination);
}