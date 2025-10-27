package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.route.*;
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
    public List<Route> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airlineOption) {
        if (airlineOption.isEmpty()) {
            if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findAll().stream()
                    .map(MapperUtils::entityToRoute).toList();
            if (origin.isEmpty()) return routeRepository.findByDestination(destination.get()).stream()
                        .map(MapperUtils::entityToRoute).toList();
            if (destination.isEmpty()) return routeRepository.findByOrigin(origin.get()).stream()
                    .map(MapperUtils::entityToRoute).toList();
            Optional<RouteEntity> optionalRouteEntity = routeRepository.findByOriginAndDestination(origin.get(),
                    destination.get());
            return optionalRouteEntity.map(routeEntity -> List.of(MapperUtils.entityToRoute(routeEntity)))
                    .orElseGet(List::of);
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
    public PathResponse<AirlinePath> getAirlinePathList(Set<String> originSet, Set<String> destinationSet, Option<Airline> airlineOption, Integer limit, Set<String> excludeAirportCodes, Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers) {
        List<Airline> originAirlines = airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(originSet),true);
        List<Airline> destinationAirlines = airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(destinationSet),true);
        Set<Airline> includeAirlines = new HashSet<>();
        if (airlineOption.isDefined()) {
            Airline airline = airlineOption.get();
            if (!originAirlines.contains(airline) || !destinationAirlines.contains(airline)) return PathResponse.<AirlinePath>builder().build();
            includeAirlines.add(airline);
        } else {
            includeAirlines.addAll(originAirlines);
            for (Airline airline : originAirlines) {
                if (!destinationAirlines.contains(airline))
                    includeAirlines.remove(airline);
            }
            if (includeAirlines.isEmpty()) return PathResponse.<AirlinePath>builder().build();
        }

        Queue<AirlinePath> toSearch = new PriorityQueue<>(Comparator.comparingDouble(AirlinePath::getTotalDistanceKm));
        Set<String> visitedAirports = new HashSet<>(excludeAirportCodes);
        visitedAirports.addAll(originSet);
        List<AirlinePath> pathAirlineList = new ArrayList<>();
        originSet.forEach(origin -> {
            List<RouteEntity> routeEntityList = routeRepository.findByOrigin(origin);
            routeEntityList.forEach(routeEntity -> {
                String routeEnd = routeEntity.getDestination();
                if (excludeRouteIds.contains(routeEntity.getId()) || visitedAirports.contains(routeEnd)
                        || routeEntity.getDistanceKm() == null) return;
                List<Airline> routeAirlines = flightRepository.selectDistinctAirlineByRouteIdAndIsActive(
                        routeEntity.getId(),true);
                routeAirlines.forEach(airline -> {
                    if (!includeAirlines.contains(airline)) return;
                    AirlinePath pathAirline = AirlinePath.builder()
                            .airline(airline)
                            .routeList(List.of(MapperUtils.entityToRoute(routeEntity)))
                            .totalDistanceKm(routeEntity.getDistanceKm())
                            .build();
                    if (destinationSet.contains(routeEnd))
                        pathAirlineList.add(pathAirline);
                    else toSearch.add(pathAirline);
                });
            });
        });
        return processQueue(toSearch,pathAirlineList,visitedAirports,destinationSet,limit,
                excludeRouteIds,excludeFlightNumbers,includeAirlines);
    }

    @Override
    public Option<Route> getRoute(String origin, String destination) {
        Optional<RouteEntity> routeEntity = routeRepository.findByOriginAndDestination(origin,destination);
        if (routeEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToRoute(routeEntity.get()));
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
