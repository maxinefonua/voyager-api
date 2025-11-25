package org.voyager.api.service.impl;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.model.query.IataQuery;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.FlightService;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.airline.AirlineAirportQuery;
import org.voyager.commons.model.airline.AirlineQuery;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.model.route.*;
import org.voyager.commons.model.path.airline.*;
import org.voyager.commons.model.path.route.*;
import org.voyager.commons.model.path.PathResponse;
import org.voyager.api.repository.AirlineAirportRepository;
import org.voyager.api.repository.FlightRepository;
import org.voyager.api.repository.RouteRepository;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.utils.MapperUtils;

import java.util.*;

@Service
public class RouteServiceImpl implements RouteService {
    @Autowired
    RouteRepository routeRepository;

    @Autowired
    AirlineAirportRepository airlineAirportRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    AirportsService airportsService;

    @Autowired
    AirlineService airlineService;

    @Autowired
    FlightService flightService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteServiceImpl.class);

    @Override
    public Boolean originExists(String origin) {
        return routeRepository.existsByOrigin(origin);
    }

    @Override
    public Boolean destinationExists(String destination) {
        return routeRepository.existsByDestination(destination);
    }

    @Override
    public Route patchRoute(Integer id, RoutePatch routePatch) {
        RouteEntity routeEntity = routeRepository.findById(id).get();
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
    public List<RoutePath> getRoutePathList(Set<String> originSet, Set<String> destinationSet, Integer limit, Set<String> excludeAirportCodes, Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers) {
        if (airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(originSet),true).isEmpty()
                || airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(destinationSet),true).isEmpty())
            return List.of();

        Queue<RoutePath> toSearch = new PriorityQueue<>(Comparator.comparingDouble(RoutePath::getTotalDistanceKm));
        Set<String> visitedAirports = new HashSet<>(excludeAirportCodes);
        visitedAirports.addAll(originSet);
        List<RoutePath> pathList = new ArrayList<>();
        originSet.forEach(origin -> {
            List<RouteEntity> routeEntityList = routeRepository.findByOrigin(origin);
            routeEntityList.forEach(routeEntity -> {
                String routeEnd = routeEntity.getDestination();
                if (excludeRouteIds.contains(routeEntity.getId()) || visitedAirports.contains(routeEnd)
                        || routeEntity.getDistanceKm() == null) return;
                List<Airline> routeAirlines = flightRepository.selectDistinctAirlineByRouteIdAndIsActive(
                        routeEntity.getId(),true);
                if (routeAirlines.isEmpty()) return;
                RoutePath path = RoutePath.builder()
                        .totalDistanceKm(routeEntity.getDistanceKm())
                        .routeAirlineList(List.of(RouteAirline.builder()
                                .routeId(routeEntity.getId())
                                .distanceKm(routeEntity.getDistanceKm())
                                .origin(routeEntity.getOrigin())
                                .destination(routeEntity.getDestination())
                                .airlines(routeAirlines)
                                .build()))
                        .build();
                if (destinationSet.contains(routeEnd))
                    pathList.add(path);
                else toSearch.add(path);
            });
        });
        return processQueue(toSearch,pathList,visitedAirports,destinationSet, limit,
                excludeRouteIds,excludeFlightNumbers);
    }

    @Override
    public Option<Route> getRoute(String origin, String destination) {
        Optional<RouteEntity> routeEntity = routeRepository.findByOriginAndDestination(origin,destination);
        if (routeEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToRoute(routeEntity.get()));
    }

    @Override
    public boolean existsById(Integer routeId) {
        return routeRepository.existsById(routeId);
    }

    private List<RoutePath> processQueue(Queue<RoutePath> toSearch, List<RoutePath> pathList,
                                    Set<String> visitedAirports,
                                    Set<String> destinationSet, Integer limit,
                                    Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers) {
        while (!toSearch.isEmpty()) {
            // TODO: process entire level first?
            Queue<RoutePath> nextLevel = new PriorityQueue<>(Comparator.comparingDouble(RoutePath::getTotalDistanceKm));
            while (!toSearch.isEmpty()) {
                RoutePath currPath = toSearch.poll();
                List<RouteAirline> routesSoFar = currPath.getRouteAirlineList();
                String start = routesSoFar.get(routesSoFar.size() - 1).getDestination();
                visitedAirports.add(start);
                List<RouteEntity> routeEntityList = routeRepository.findByOrigin(start);
                for (RouteEntity routeEntity : routeEntityList) {
                    String routeEnd = routeEntity.getDestination();
                    Integer routeId = routeEntity.getId();
                    Double routeDistance = routeEntity.getDistanceKm();
                    if (visitedAirports.contains(routeEnd) || excludeRouteIds.contains(routeId) || routeDistance == null)
                        continue;
                    List<Airline> airlineList = flightRepository.selectDistinctAirlineByRouteIdAndIsActive(routeId, true);
                    if (airlineList.isEmpty()) continue;
                    List<RouteAirline> routeAirlineList = new ArrayList<>(routesSoFar);
                    routeAirlineList.add(RouteAirline.builder()
                            .routeId(routeId)
                            .origin(start)
                            .destination(routeEnd)
                            .distanceKm(routeDistance)
                            .airlines(airlineList)
                            .build());
                    RoutePath newPath = RoutePath.builder()
                            .totalDistanceKm(currPath.getTotalDistanceKm() + routeDistance)
                            .routeAirlineList(routeAirlineList)
                            .build();
                    if (destinationSet.contains(routeEnd)) {
                        pathList.add(newPath);
                        if (pathList.size() >= limit) return pathList;
                    } else {
                        nextLevel.add(newPath);
                    }
                }
            }
            toSearch = nextLevel;
        }
        return pathList;
    }

    private PathResponse<AirlinePath> processQueue(Queue<AirlinePath> toSearch,
                                           List<AirlinePath> pathAirlineList,
                                           Set<String> visitedAirports,
                                           Set<String> destinationSet, Integer limit,
                                           Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers,
                                           Set<Airline> includeAirlines) {
        while (!toSearch.isEmpty()) {
            Queue<AirlinePath> nextLevel = new PriorityQueue<>(Comparator.comparingDouble(AirlinePath::getTotalDistanceKm));
            while (!toSearch.isEmpty()) {
                AirlinePath pathAirline = toSearch.poll();
                List<Route> routeListSoFar = pathAirline.getRouteList();
                String start = routeListSoFar.get(routeListSoFar.size() - 1).getDestination();
                Airline airline = pathAirline.getAirline();
                visitedAirports.add(start);
                List<RouteEntity> routeEntityList = routeRepository.findByOrigin(start);
                for (RouteEntity routeEntity : routeEntityList) {
                    String routeEnd = routeEntity.getDestination();
                    if (excludeRouteIds.contains(routeEntity.getId()) || visitedAirports.contains(routeEnd)
                            || routeEntity.getDistanceKm() == null) continue;
                    List<Airline> routeAirlines = flightRepository.selectDistinctAirlineByRouteIdAndIsActive(routeEntity.getId(), true);
                    if (!routeAirlines.contains(airline)) continue;
                    List<Route> copyRouteList = new ArrayList<>(pathAirline.getRouteList());
                    copyRouteList.add(MapperUtils.entityToRoute(routeEntity));
                    AirlinePath newPathAirline = AirlinePath.builder()
                            .airline(airline)
                            .totalDistanceKm(pathAirline.getTotalDistanceKm() + routeEntity.getDistanceKm())
                            .routeList(copyRouteList)
                            .build();
                    if (destinationSet.contains(routeEnd)) {
                        pathAirlineList.add(newPathAirline);
                        if (pathAirlineList.size() >= limit)
                            return PathResponse.<AirlinePath>builder()
                                    .count(pathAirlineList.size())
                                    .airlines(includeAirlines.stream().toList())
                                    .responseList(pathAirlineList)
                                    .build();
                    } else nextLevel.add(newPathAirline);
                }
            }
            toSearch = nextLevel;
        }
        return PathResponse.<AirlinePath>builder()
                .count(pathAirlineList.size())
                .airlines(includeAirlines.stream().toList())
                .responseList(pathAirlineList)
                .build();
    }
}
