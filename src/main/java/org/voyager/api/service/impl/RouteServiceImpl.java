package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.commons.model.route.RouteQuery;
import org.voyager.api.repository.RouteRepository;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.utils.MapperUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class RouteServiceImpl implements RouteService {
    @Autowired
    RouteRepository routeRepository;

    @Override
    public boolean existsById(Integer id) {
        return routeRepository.existsById(id);
    }

    @Override
    public Route patchRoute(@Validated RouteEntity routeEntity, RoutePatch routePatch) {
        routeEntity.setDistanceKm(routePatch.getDistanceKm());
        return MapperUtils.entityToRoute(routeRepository.save(routeEntity));
    }

    @Override
    public Option<Route> getRouteById(Integer id) {
        Optional<RouteEntity> routeEntity = routeRepository.findById(id);
        if (routeEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToRoute(routeEntity.get()));
    }

    @Override
    public Option<RouteEntity> getRouteEntityById(Integer id) {
        Optional<RouteEntity> routeEntity = routeRepository.findById(id);
        return routeEntity.map(Option::of).orElseGet(Option::none);
    }

    @Override
    public List<Route> getRoutes() {
        return routeRepository.findAll().stream().map(MapperUtils::entityToRoute).toList();
    }

    @Override
    @Cacheable("routeQueryCache")
    public List<Route> getRoutes(@Validated RouteQuery routeQuery) {
        List<String> originList = Optional.ofNullable(routeQuery.getOriginList())
                .orElse(Collections.emptyList());
        List<String> destinationList = Optional.ofNullable(routeQuery.getDestinationList())
                .orElse(Collections.emptyList());
        List<String> excludeDestinationList = new ArrayList<>(
                Optional.ofNullable(routeQuery.getExcludeDestinationSet())
                        .orElse(Collections.emptySet()));
        List<Integer> excludeRouteIdList = new ArrayList<>(
                Optional.ofNullable(routeQuery.getExcludeRouteIdSet())
                        .orElse(Collections.emptySet()));

        List<RouteEntity> routeEntityList = routeRepository.findRoutes(
                originList.isEmpty() ? null : originList,
                destinationList.isEmpty() ? null : destinationList,
                excludeDestinationList.isEmpty() ? null : excludeDestinationList,
                excludeRouteIdList.isEmpty() ? null : excludeRouteIdList
        );

        return routeEntityList.stream().map(MapperUtils::entityToRoute).toList();
    }

    @Override
    public Route save(RouteForm routeForm) {
        return handleJPAExceptions(()-> {
            RouteEntity routeEntity = MapperUtils.formToRouteEntity(routeForm);
                routeEntity = routeRepository.save(routeEntity);
            return MapperUtils.entityToRoute(routeEntity);
        });
    }

    @Override
    public Option<Route> getRoute(String origin, String destination) {
        Optional<RouteEntity> routeEntity = routeRepository.findByOriginAndDestination(origin,destination);
        if (routeEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToRoute(routeEntity.get()));
    }
}
