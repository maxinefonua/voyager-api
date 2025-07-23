package org.voyager.service.impl;

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
import org.voyager.model.route.*;
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
    public List<Path> getPathList(Set<String> originSet, Set<String> destinationSet, Integer limit, Set<String> excludeAirportCodes, Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers) {
        if (airlineRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(originSet),true).isEmpty()
                || airlineRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(destinationSet),true).isEmpty())
            return List.of();

        Queue<Path> toSearch = new PriorityQueue<>(Comparator.comparingDouble(Path::getTotalDistanceKm));
        Set<String> visitedAirports = new HashSet<>(excludeAirportCodes);
        visitedAirports.addAll(originSet);
        List<Path> pathList = new ArrayList<>();
        originSet.forEach(origin -> {
            List<RouteEntity> routeEntityList = routeRepository.findByOrigin(origin);
            routeEntityList.forEach(routeEntity -> {
                String routeEnd = routeEntity.getDestination();
                if (excludeRouteIds.contains(routeEntity.getId()) || visitedAirports.contains(routeEnd)
                        || routeEntity.getDistanceKm() == null) return;
                List<Airline> routeAirlines = flightRepository.selectDistinctAirlineByRouteIdAndIsActive(
                        routeEntity.getId(),true);
                if (routeAirlines.isEmpty()) return;
                Path path = Path.builder()
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
    public PathResponse<PathAirline> getAirlinePathList(Set<String> originSet, Set<String> destinationSet, Option<Airline> airlineOption, Integer limit, Set<String> excludeAirportCodes, Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers) {
        List<Airline> originAirlines = airlineRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(originSet),true);
        List<Airline> destinationAirlines = airlineRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(destinationSet),true);
        Set<Airline> includeAirlines = new HashSet<>();
        if (airlineOption.isDefined()) {
            Airline airline = airlineOption.get();
            if (!originAirlines.contains(airline) || !destinationAirlines.contains(airline)) return PathResponse.<PathAirline>builder().build();
            includeAirlines.add(airline);
        } else {
            includeAirlines.addAll(originAirlines);
            for (Airline airline : originAirlines) {
                if (!destinationAirlines.contains(airline))
                    includeAirlines.remove(airline);
            }
            if (includeAirlines.isEmpty()) return PathResponse.<PathAirline>builder().build();
        }

        Queue<PathAirline> toSearch = new PriorityQueue<>(Comparator.comparingDouble(PathAirline::getTotalDistanceKm));
        Set<String> visitedAirports = new HashSet<>(excludeAirportCodes);
        visitedAirports.addAll(originSet);
        List<PathAirline> pathAirlineList = new ArrayList<>();
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
                    PathAirline pathAirline = PathAirline.builder()
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

    private List<Path> processQueue(Queue<Path> toSearch, List<Path> pathList,
                                    Set<String> visitedAirports,
                                    Set<String> destinationSet, Integer limit,
                                    Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers) {
        while (!toSearch.isEmpty()) {
            // TODO: process entire level first?
            Queue<Path> nextLevel = new PriorityQueue<>(Comparator.comparingDouble(Path::getTotalDistanceKm));
            while (!toSearch.isEmpty()) {
                Path currPath = toSearch.poll();
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
                    Path newPath = Path.builder()
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

    private PathResponse<PathAirline> processQueue(Queue<PathAirline> toSearch,
                                           List<PathAirline> pathAirlineList,
                                           Set<String> visitedAirports,
                                           Set<String> destinationSet, Integer limit,
                                           Set<Integer> excludeRouteIds, Set<String> excludeFlightNumbers,
                                           Set<Airline> includeAirlines) {
        while (!toSearch.isEmpty()) {
            Queue<PathAirline> nextLevel = new PriorityQueue<>(Comparator.comparingDouble(PathAirline::getTotalDistanceKm));
            while (!toSearch.isEmpty()) {
                PathAirline pathAirline = toSearch.poll();
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
                    PathAirline newPathAirline = PathAirline.builder()
                            .airline(airline)
                            .totalDistanceKm(pathAirline.getTotalDistanceKm() + routeEntity.getDistanceKm())
                            .routeList(copyRouteList)
                            .build();
                    if (destinationSet.contains(routeEnd)) {
                        pathAirlineList.add(newPathAirline);
                        if (pathAirlineList.size() >= limit)
                            return PathResponse.<PathAirline>builder()
                                    .count(pathAirlineList.size())
                                    .airlines(includeAirlines.stream().toList())
                                    .responseList(pathAirlineList)
                                    .build();
                    } else nextLevel.add(newPathAirline);
                }
            }
            toSearch = nextLevel;
        }
        return PathResponse.<PathAirline>builder()
                .count(pathAirlineList.size())
                .airlines(includeAirlines.stream().toList())
                .responseList(pathAirlineList)
                .build();
    }
}
