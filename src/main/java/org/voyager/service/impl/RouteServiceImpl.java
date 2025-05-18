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
import org.voyager.model.entity.Route;
import org.voyager.model.route.PathDisplay;
import org.voyager.model.route.RouteDisplay;
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
    public Option<RouteDisplay> getRouteById(Integer id) {
        Optional<Route> route = routeRepository.findById(id);
        if (route.isEmpty()) return Option.none();
        return Option.of(MapperUtils.routeToDisplay(route.get()));
    }

    @Override
    public Option<RouteDisplay> getByOriginAndDestinationAndAirline(String origin, String destination, Airline airline) {
        Optional<Route> route = routeRepository.findByOriginAndDestinationAndAirline(origin,destination,airline);
        if (route.isEmpty()) return Option.none();
        return Option.of(MapperUtils.routeToDisplay(route.get()));
    }

    @Override
    public List<RouteDisplay> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline) {
        if (origin.isEmpty() && destination.isEmpty() && airline.isEmpty()) return routeRepository.findAll().stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findByAirline(airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty() && airline.isEmpty()) return routeRepository.findByDestination(destination.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (destination.isEmpty() && airline.isEmpty()) return routeRepository.findByOrigin(origin.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (airline.isEmpty()) return routeRepository.findByOriginAndDestination(origin.get(), destination.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty()) return routeRepository.findByDestinationAndAirline(destination.get(),airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (destination.isEmpty()) return routeRepository.findByOriginAndAirline(origin.get(),airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
        return routeRepository.findByOriginAndDestinationAndAirline(origin.get(), destination.get(),airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
    }

    @Override
    public List<RouteDisplay> getActiveRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline, Boolean isActive) {
        if (origin.isEmpty() && destination.isEmpty() && airline.isEmpty()) return routeRepository.findByIsActive(isActive).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findByAirlineAndIsActive(airline.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty() && airline.isEmpty()) return routeRepository.findByDestinationAndIsActive(destination.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
        if (destination.isEmpty() && airline.isEmpty()) return routeRepository.findByOriginAndIsActive(origin.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
        if (airline.isEmpty()) return routeRepository.findByOriginAndDestinationAndIsActive(origin.get(), destination.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty()) return routeRepository.findByDestinationAndAirlineAndIsActive(destination.get(),airline.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
        if (destination.isEmpty()) return routeRepository.findByOriginAndAirlineAndIsActive(origin.get(),airline.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
        return routeRepository.findByOriginAndDestinationAndAirlineAndIsActive(origin.get(), destination.get(),airline.get(),isActive).stream().map(MapperUtils::routeToDisplay).toList();
    }

    @Override
    public RouteDisplay save(RouteForm routeForm) {
        Route route = MapperUtils.formToRoute(routeForm);
        try {
            route = routeRepository.save(route);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.buildRespositorySaveErrorMessage("route"));
        }
        return MapperUtils.routeToDisplay(route);
    }

    @Override
    public RouteDisplay patch(RouteDisplay routeDisplay, RoutePatch routePatch) {
        Route patched = MapperUtils.patchDisplayToRoute(routeDisplay,routePatch);
        try {
            patched = routeRepository.save(patched);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.buildRespositorySaveErrorMessage("route"));
        }
        return MapperUtils.routeToDisplay(patched);
    }

    @Override
    public PathDisplay buildPathWithExclusions(String origin, String destination, Set<String> exclusions) {
        List<Integer> routeIds = buildRouteIdListOfShortestPath(origin,destination,exclusions);
        List<RouteDisplay> routeDisplayList = routeIds.stream().map(id -> routeRepository.findById(id).get()).map(MapperUtils::routeToDisplay).toList();
        return PathDisplay.builder().routeDisplayList(routeDisplayList).build();
    }

    private List<Integer> buildRouteIdListOfShortestPath(String origin, String destination, Set<String> exclusions) {
        Queue<Tuple2<String,List<Integer>>> toSearch = new ArrayDeque<>();
        toSearch.add(new Tuple2<>(origin,new ArrayList<>()));
        Set<String> visited = new HashSet<>();
        while (!toSearch.isEmpty()) {
            Tuple2<String,List<Integer>> curr = toSearch.poll();
            visited.add(curr._1());
            List<Integer> routeIds = curr._2();
            List<Route> routes = routeRepository.findByOrigin(curr._1());
            for (Route route : routes) {
                if (!route.getIsActive()) continue;
                String next = route.getDestination();
                if (next.equals(destination)) {
                    routeIds.add(route.getId());
                    return routeIds;
                }
                if (!visited.contains(next) && !exclusions.contains(next)) {
                    routeIds.add(route.getId());
                    toSearch.add(new Tuple2<>(next,new ArrayList<>(routeIds)));
                    routeIds.remove(route.getId());
                }
            }
        }
        return List.of();
    }
}
