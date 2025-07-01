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
import org.voyager.model.entity.FlightEntity;
import org.voyager.model.entity.RouteEntity;
import org.voyager.model.route.Path;
import org.voyager.model.route.PathAirline;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.repository.AirlineRepository;
import org.voyager.repository.FlightRepository;
import org.voyager.repository.RouteRepository;
import org.voyager.service.RouteService;
import org.voyager.service.utils.MapperUtils;

import java.util.*;

@Service
public class RouteServiceImpl implements RouteService {
    @Autowired
    RouteRepository routeRepository;

    @Autowired
    AirlineRepository airlineRepository;

    @Autowired
    FlightRepository flightRepository;

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
    public List<Route> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airlineOption) {
        if (airlineOption.isEmpty()) {
            if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findAll().stream()
                    .map(MapperUtils::entityToRoute).toList();
            if (origin.isEmpty()) return routeRepository.findByDestination(destination.get()).stream()
                        .map(MapperUtils::entityToRoute).toList();
            if (destination.isEmpty()) return routeRepository.findByOrigin(origin.get()).stream()
                    .map(MapperUtils::entityToRoute).toList();
            return routeRepository.findByOriginAndDestination(origin.get(), destination.get()).stream()
                    .map(MapperUtils::entityToRoute).toList();
        } else {
            List<Integer> airlineRouteIds = flightRepository.selectDistinctRouteIdByAirlineAndIsActive(airlineOption.get(),true);
            if (origin.isEmpty() && destination.isEmpty()) // find by airline -> check active flights
                return routeRepository.findByIdIn(airlineRouteIds).stream().map(MapperUtils::entityToRoute).toList();
            if (origin.isEmpty()) // find by airline with destination
                return routeRepository.findByDestinationAndIdIn(destination.get(),airlineRouteIds).stream()
                        .map(MapperUtils::entityToRoute).toList();
            if (destination.isEmpty()) // find by airline with origin
                return routeRepository.findByOriginAndIdIn(origin.get(),airlineRouteIds).stream()
                        .map(MapperUtils::entityToRoute).toList();
            return routeRepository.findByOriginAndDestinationAndIdIn(origin.get(),destination.get(),airlineRouteIds)
                    .stream().map(MapperUtils::entityToRoute).toList();
        }
    }

    private List<Route> getRoutesByOriginAndAirlineAndIsActive(String origin, Airline airline, boolean isActive) {
        List<RouteEntity> airlineRouteEntities = new ArrayList<>();
        List<Integer> airlineRouteIds = flightRepository.selectDistinctRouteIdByAirlineAndIsActive(airline,isActive);
        routeRepository.findByOrigin(origin).forEach(routeEntity -> {
            if (airlineRouteIds.contains(routeEntity.getId()))
                airlineRouteEntities.add(routeEntity);
        });
        return airlineRouteEntities.stream().map(MapperUtils::entityToRoute).toList();
    }

    private List<Route> getRoutesByDestinationAndAirlineAndIsActive(String destination,
                                                                    Airline airline, boolean isActive) {
        List<RouteEntity> airlineRouteEntities = new ArrayList<>();
        List<Integer> airlineRouteIds = flightRepository.selectDistinctRouteIdByAirlineAndIsActive(airline,isActive);
        routeRepository.findByDestination(destination).forEach(routeEntity -> {
            if (airlineRouteIds.contains(routeEntity.getId()))
                airlineRouteEntities.add(routeEntity);
        });
        return airlineRouteEntities.stream().map(MapperUtils::entityToRoute).toList();
    }

    private List<Route> getRoutesByAirlineAndIsActive(Airline airline, boolean isActive) {
        List<Integer> airlineRouteIds = flightRepository.selectDistinctRouteIdByAirlineAndIsActive(airline,isActive);
        return routeRepository.findByIdIn(airlineRouteIds).stream().map(MapperUtils::entityToRoute).toList();
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
    public Path buildPathWithExclusions(String origin, String destination, Option<Airline> airlineOption, Set<String> excludeAirports, List<Integer> excludeRoutes) {
        List<Integer> routeIds = buildRouteIdListOfShortestPath(origin,destination,airlineOption,excludeAirports,excludeRoutes);
        List<Route> routeList = routeIds.stream().map(id -> routeRepository.findById(id).get()).map(MapperUtils::entityToRoute).toList();
        return Path.builder().routeList(routeList).build();
    }

    @Override
    public List<PathAirline> getAirlinePathList(String origin, String destination,
                                                Option<Airline> airlineOption, Integer limit,
                                                Set<String> excludeAirportCodes,
                                                Set<Integer> excludeRouteIds,
                                                Set<String> excludeFlightNumbers) {
        if (airlineOption.isDefined()) {
            if (notActiveAirlineAirport(origin, airlineOption.get()) || notActiveAirlineAirport(destination, airlineOption.get()))
                return List.of();
            return buildPathAirlineListWithAirline(origin, destination, airlineOption.get(), limit,
                    excludeAirportCodes, excludeRouteIds, excludeFlightNumbers);
        }
        if (airlineRepository.selectAirlinesByIataAndIsActive(origin,true).isEmpty()
                || airlineRepository.selectAirlinesByIataAndIsActive(destination,true).isEmpty())
            return List.of();
        return buildPathAirlineList(origin, destination, limit,
                excludeAirportCodes, excludeRouteIds, excludeFlightNumbers);
    }

    private List<PathAirline> buildPathAirlineListWithAirline(String origin, String destination,
                                                              Airline airline, Integer limit,
                                                              Set<String> excludeAirportCodes,
                                                              Set<Integer> excludeRouteIds,
                                                              Set<String> excludeFlightNumbers) {
        return List.of();
    }

    private List<PathAirline> buildPathAirlineList(String origin, String destination,
                                                   Integer limit,
                                                   Set<String> excludeAirportCodes,
                                                   Set<Integer> excludeRouteIds,
                                                   Set<String> excludeFlightNumbers) {
        List<PathAirline> pathAirlineList = new ArrayList<>();
        Queue<Tuple2<String,PathAirline>> toSearch = new ArrayDeque<>();
        toSearch.add(new Tuple2<>(origin, PathAirline.builder().build()));
        Set<String> visitedAirports = new HashSet<>();
        while (!toSearch.isEmpty()) {
            Tuple2<String,PathAirline> curr = toSearch.poll();
            String start = curr._1();;
            visitedAirports.add(start);
            PathAirline pathAirline = curr._2();
            List<Integer> routeIdList = pathAirline.getRouteIdList();
            List<RouteEntity> routeEntityList = routeRepository.findByOrigin(start);
            routeEntityList.forEach(routeEntity -> {
                Integer routeId = routeEntity.getId();
                routeIdList.add(routeId);
                String routeEnd = routeEntity.getDestination();
                if (!visitedAirports.contains(routeEnd)) {
                    List<Airline> routeAirlines = flightRepository.selectDistinctAirlineByRouteIdAndIsActive(routeEntity.getId(), true);
                    if (pathAirline.getAirline() != null) {
                        if (routeAirlines.contains(pathAirline.getAirline())) {
                            routeIdList.add(routeId);
                            pathAirline.setRouteIdList(routeIdList);
                            if (routeEnd.equals(destination))
                                pathAirlineList.add(pathAirline);
                            else toSearch.add(new Tuple2<>(routeEnd, pathAirline));
                        }
                    } else {
                        routeAirlines.forEach(routeAirline -> {
                            PathAirline newPathAirline = PathAirline.builder()
                                    .airline(routeAirline)
                                    .routeIdList(new ArrayList<>(routeIdList))
                                    .build();
                            if (routeEnd.equals(destination))
                                pathAirlineList.add(newPathAirline);
                            else toSearch.add(new Tuple2<>(routeEnd,newPathAirline));
                        });
                    }
                }
            });
            if (pathAirlineList.size() >= limit) break;
        }
        return pathAirlineList;
    }

    private boolean notActiveAirlineAirport(String iata, Airline airline) {
        List<Airline> activeAirlines = airlineRepository.selectAirlinesByIataAndIsActive(iata,true);
        return !activeAirlines.contains(airline);
    }

    private boolean notActiveAirlineRoute(Integer routeId, Airline airline) {
        List<FlightEntity> flightEntities = flightRepository.findByRouteIdAndAirlineAndIsActive(
                routeId,airline,true);
        return flightEntities.isEmpty();
    }

    private List<Integer> buildRouteIdListOfShortestPath(String origin,
                                                         String destination,
                                                         Option<Airline> airlineOption,
                                                         Set<String> exclusions,
                                                         List<Integer> excludeRoutes) {
        if (airlineOption.isDefined() && (notActiveAirlineAirport(origin, airlineOption.get())
                || notActiveAirlineAirport(destination, airlineOption.get()))) {
            return List.of();
        }
        Queue<Tuple2<String,List<Integer>>> toSearch = new ArrayDeque<>();
        toSearch.add(new Tuple2<>(origin, new ArrayList<>()));
        Set<String> visited = new HashSet<>();
        while (!toSearch.isEmpty()) {
            Tuple2<String,List<Integer>> curr = toSearch.poll();
            visited.add(curr._1());
            List<Integer> routeIds = curr._2();
            List<RouteEntity> routeEntities = routeRepository.findByOrigin(curr._1());
            for (RouteEntity routeEntity : routeEntities) {
                if (excludeRoutes.contains(routeEntity.getId())) continue;
                if (airlineOption.isDefined() && notActiveAirlineRoute(routeEntity.getId(),airlineOption.get())) continue;
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
