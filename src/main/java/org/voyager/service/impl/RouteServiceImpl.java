package org.voyager.service.impl;

import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.entity.RouteEntity;
import org.voyager.model.route.Path;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.repository.DeltaRepository;
import org.voyager.repository.RouteRepository;
import org.voyager.service.RouteService;
import org.voyager.service.utils.MapperUtils;

import java.util.*;

@Service
public class RouteServiceImpl implements RouteService {
    @Autowired
    RouteRepository routeRepository;

    @Autowired
    DeltaRepository deltaRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteServiceImpl.class);

    @Override
    public Boolean originExists(String origin) {
        return routeRepository.originExists(origin);
    }

    @Override
    public Boolean destinationExists(String destination) {
        return routeRepository.destinationExists(destination);
    }

    @Override
    public Option<Route> getRouteById(Integer id) {
        Optional<RouteEntity> route = routeRepository.findById(id);
        if (route.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToRoute(route.get()));
    }

    @Override
    public Option<Route> getByOriginAndDestinationAndAirline(String origin, String destination, Airline airline) {
        Optional<RouteEntity> route = routeRepository.findByOriginAndDestinationAndAirline(origin,destination,airline);
        if (route.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToRoute(route.get()));
    }

    @Override
    public List<Route> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline) {
        if (origin.isEmpty() && destination.isEmpty() && airline.isEmpty()) return routeRepository.findAll().stream().map(MapperUtils::entityToRoute).toList();
        if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findByAirline(airline.get()).stream().map(MapperUtils::entityToRoute).toList();
        if (origin.isEmpty() && airline.isEmpty()) return routeRepository.findByDestination(destination.get()).stream().map(MapperUtils::entityToRoute).toList();
        if (destination.isEmpty() && airline.isEmpty()) return routeRepository.findByOrigin(origin.get()).stream().map(MapperUtils::entityToRoute).toList();
        if (airline.isEmpty()) return routeRepository.findByOriginAndDestination(origin.get(), destination.get()).stream().map(MapperUtils::entityToRoute).toList();
        if (origin.isEmpty()) return routeRepository.findByDestinationAndAirline(destination.get(),airline.get()).stream().map(MapperUtils::entityToRoute).toList();
        if (destination.isEmpty()) return routeRepository.findByOriginAndAirline(origin.get(),airline.get()).stream().map(MapperUtils::entityToRoute).toList();
        return routeRepository.findByOriginAndDestinationAndAirline(origin.get(), destination.get(),airline.get()).stream().map(MapperUtils::entityToRoute).toList();
    }

    @Override
    public List<Route> getActiveRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline, Boolean isActive) {
        if (origin.isEmpty() && destination.isEmpty() && airline.isEmpty()) return routeRepository.findByIsActive(isActive).stream().map(MapperUtils::entityToRoute).toList();
        if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findByAirlineAndIsActive(airline.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
        if (origin.isEmpty() && airline.isEmpty()) return routeRepository.findByDestinationAndIsActive(destination.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
        if (destination.isEmpty() && airline.isEmpty()) return routeRepository.findByOriginAndIsActive(origin.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
        if (airline.isEmpty()) return routeRepository.findByOriginAndDestinationAndIsActive(origin.get(), destination.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
        if (origin.isEmpty()) return routeRepository.findByDestinationAndAirlineAndIsActive(destination.get(),airline.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
        if (destination.isEmpty()) return routeRepository.findByOriginAndAirlineAndIsActive(origin.get(),airline.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
        return routeRepository.findByOriginAndDestinationAndAirlineAndIsActive(origin.get(), destination.get(),airline.get(),isActive).stream().map(MapperUtils::entityToRoute).toList();
    }

    @Override
    public Route save(RouteForm routeForm) {
        RouteEntity routeEntity = MapperUtils.formToRouteEntity(routeForm);
        try {
            routeEntity = routeRepository.save(routeEntity);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositorySaveErrorMessage("route"),
                    e);
        }
        return MapperUtils.entityToRoute(routeEntity);
    }

    @Override
    public Route patch(Route route, RoutePatch routePatch) {
        RouteEntity patched = MapperUtils.patchToRouteEntity(route,routePatch);
        try {
            patched = routeRepository.save(patched);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositoryPatchErrorMessage("route",String.valueOf(route.getId())),
                    e);
        }
        return MapperUtils.entityToRoute(patched);
    }

    @Override
    public Path buildPathWithExclusions(String origin, String destination, Set<String> excludeAirports,List<Integer> excludeRoutes) {
        List<Integer> routeIds = buildRouteIdListOfShortestPath(origin,destination, excludeAirports,excludeRoutes);
        List<Route> routeList = routeIds.stream().map(id -> routeRepository.findById(id).get()).map(MapperUtils::entityToRoute).toList();
        return Path.builder().routeList(routeList).build();
    }

    private List<Integer> buildRouteIdListOfShortestPath(String origin, String destination, Set<String> exclusions, List<Integer> excludeRoutes) {
        Queue<Tuple2<String,List<Integer>>> toSearch = new ArrayDeque<>();
        toSearch.add(new Tuple2<>(origin,new ArrayList<>()));
        Set<String> visited = new HashSet<>();
        while (!toSearch.isEmpty()) {
            Tuple2<String,List<Integer>> curr = toSearch.poll();
            visited.add(curr._1());
            List<Integer> routeIds = curr._2();
            List<RouteEntity> routeEntities = routeRepository.findByOrigin(curr._1());
            for (RouteEntity routeEntity : routeEntities) {
                if (!routeEntity.getIsActive() || excludeRoutes.contains(routeEntity.getId())) continue;
                String next = routeEntity.getDestination();
                if (next.equals(destination)) {
                    routeIds.add(routeEntity.getId());
                    return routeIds;
                }
                if (!visited.contains(next) && !exclusions.contains(next)) {
                    routeIds.add(routeEntity.getId());
                    toSearch.add(new Tuple2<>(next,new ArrayList<>(routeIds)));
                    routeIds.remove(routeEntity.getId());
                }
            }
        }
        return List.of();
    }
}
