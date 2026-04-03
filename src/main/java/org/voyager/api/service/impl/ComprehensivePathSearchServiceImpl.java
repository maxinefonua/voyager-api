package org.voyager.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.ComprehensivePathSearchService;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteQuery;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ComprehensivePathSearchServiceImpl implements ComprehensivePathSearchService {
    @Autowired
    RouteService routeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensivePathSearchServiceImpl.class);

    @Override
    public void streamPaths(PathSearchRequest request, Consumer<Path> pathConsumer) {
        List<String> excludeCodes = new ArrayList<>(request.getExcludeAirports());
        excludeCodes.addAll(request.getDestinations());

        List<Route> routeList = routeService.getRoutes(
                        RouteQuery.builder()
                                .originList(new ArrayList<>(request.getOrigins()))
                                .excludeRouteIdSet(request.getExcludeRouteIds())
                                .excludeAirportList(excludeCodes)
                                .build());

        Queue<Path> queue = routeList.stream()
                .filter(route -> route.getDistanceKm() != null)
                .map(route -> Path.builder()
                        .routeList(List.of(route))
                        .totalDistanceKm(route.getDistanceKm())
                        .build())
                .collect(Collectors.toCollection(
                        () -> new PriorityQueue<>(Comparator.comparing(Path::getTotalDistanceKm))
                ));


        List<String> excludeBackCodes = new ArrayList<>(request.getExcludeAirports());
        excludeBackCodes.addAll(request.getOrigins());

        List<Route> backRoutes = routeService.getRoutes(
                RouteQuery.builder()
                        .destinationList(new ArrayList<>(request.getDestinations()))
                        .excludeRouteIdSet(request.getExcludeRouteIds())
                        .excludeAirportList(excludeBackCodes)
                        .build());

        Queue<Path> backQueue = backRoutes.stream()
                .filter(route -> route.getDistanceKm() != null)
                .map(route -> Path.builder()
                        .routeList(List.of(route))
                        .totalDistanceKm(route.getDistanceKm())
                        .build())
                .collect(Collectors.toCollection(
                        () -> new PriorityQueue<>(Comparator.comparing(Path::getTotalDistanceKm))
                ));


        processQueue(queue, request, pathConsumer, backQueue);
    }

    private void processQueue(
            Queue<Path> forwardQueue, PathSearchRequest request, Consumer<Path> pathConsumer, Queue<Path> backwardQueue) {

        Set<String> forwardVisited = new HashSet<>(request.getExcludeAirports());
        forwardVisited.addAll(request.getOrigins());

        Set<String> backwardVisited = new HashSet<>(request.getExcludeAirports());
        backwardVisited.addAll(request.getDestinations());

        Set<String> addedPathKeys = new HashSet<>();

        Set<Integer> excludeRouteIds = request.getExcludeRouteIds();
        Set<String> destinationSet = request.getDestinations();
        Set<String> originSet = request.getOrigins();

        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            Queue<Path> nextForwardQueue = new PriorityQueue<>(Comparator
                    .comparing(Path::getRouteList,Comparator.comparing(List::size))
                    .thenComparing(Path::getTotalDistanceKm));
            while (!forwardQueue.isEmpty()) {
                Path pathSoFar = forwardQueue.poll();
                List<Route> soFarRouteList = pathSoFar.getRouteList();
                String nextOrigin = soFarRouteList.get(soFarRouteList.size()-1).getDestination();
                for (Route route : routeService.getRoutes(RouteQuery.builder()
                        .originList(List.of(nextOrigin))
                        .excludeAirportList(new ArrayList<>(forwardVisited))
                        .excludeRouteIdSet(excludeRouteIds)
                        .build())) {
                    if (route.getDistanceKm() == null) continue; // TODO: validate route data
                    List<Route> routeList = new ArrayList<>(soFarRouteList);
                    routeList.add(route);
                    Path nextPath = Path.builder()
                            .routeList(routeList)
                            .totalDistanceKm(route.getDistanceKm() + pathSoFar.getTotalDistanceKm())
                            .build();
                    if (destinationSet.contains(route.getDestination())) {
                        String pathKey = generatePathKey(nextPath);
                        if (!addedPathKeys.contains(pathKey)) {
                            pathConsumer.accept(nextPath);
                            addedPathKeys.add(pathKey);
                        }
                    } else if (backwardVisited.contains(route.getDestination())) {
                        List<Path> pathsToAdd = new ArrayList<>();
                        backwardQueue.removeIf(backwardPath -> {
                            List<Route> backwardRouteList = backwardPath.getRouteList();
                            if (backwardRouteList.get(0).getId().equals(route.getId())) {
                                List<Route> combined = new ArrayList<>(nextPath.getRouteList());
                                combined.addAll(backwardRouteList.subList(1,backwardRouteList.size()));
                                if (combined.size() < 4) {
                                    Path combinedPath = Path.builder()
                                            .routeList(combined)
                                            .totalDistanceKm(nextPath.getTotalDistanceKm() +
                                                    backwardPath.getTotalDistanceKm() - route.getDistanceKm()).build();
                                    pathsToAdd.add(combinedPath);
                                }
                                return true;
                            }
                            return false;
                        });
                        pathsToAdd.forEach(pathConsumer);
                    } else if (nextPath.getRouteList().size() < 3) {
                        nextForwardQueue.add(nextPath);
                    }
                }
                forwardVisited.add(nextOrigin);
            }
            forwardQueue = nextForwardQueue;

            Queue<Path> nextBackwardQueue = new PriorityQueue<>(Comparator
                    .comparing(Path::getRouteList,Comparator.comparing(List::size))
                    .thenComparing(Path::getTotalDistanceKm));
            while (!backwardQueue.isEmpty()) {
                Path pathSoFar = backwardQueue.poll();
                List<Route> soFarRouteList = pathSoFar.getRouteList();
                String nextDestination = soFarRouteList.get(0).getOrigin();
                for (Route route : routeService.getRoutes(RouteQuery.builder()
                        .destinationList(List.of(nextDestination))
                        .excludeAirportList(new ArrayList<>(backwardVisited))
                        .excludeRouteIdSet(excludeRouteIds)
                        .build())) {
                    if (route.getDistanceKm() == null) continue; // TODO: validate route data
                    List<Route> routeList = new ArrayList<>(soFarRouteList);
                    routeList.add(0,route);
                    Path nextPath = Path.builder()
                            .routeList(routeList)
                            .totalDistanceKm(route.getDistanceKm() + pathSoFar.getTotalDistanceKm())
                            .build();
                    if (originSet.contains(route.getOrigin())) {
                        String pathKey = generatePathKey(nextPath);
                        if (!addedPathKeys.contains(pathKey)) {
                            pathConsumer.accept(nextPath);
                            addedPathKeys.add(pathKey);
                        }
                    } else if (forwardVisited.contains(route.getOrigin())) {
                        List<Path> pathsToAdd = new ArrayList<>();
                        forwardQueue.removeIf(forwardPath -> {
                            List<Route> forwardRouteList = forwardPath.getRouteList();
                            if (forwardRouteList.get(forwardRouteList.size()-1).getId().equals(route.getId())) {
                                List<Route> combined = new ArrayList<>(forwardPath.getRouteList()
                                        .subList(0,forwardRouteList.size()-1));
                                combined.addAll(nextPath.getRouteList());
                                if (combined.size() < 4) {
                                    Path combinedPath = Path.builder().routeList(combined)
                                            .totalDistanceKm(forwardPath.getTotalDistanceKm() +
                                                    nextPath.getTotalDistanceKm() - route.getDistanceKm())
                                            .build();
                                    pathsToAdd.add(combinedPath);
                                }
                                return true;
                            }
                            return false;
                        });
                        pathsToAdd.forEach(pathConsumer);
                    } else if (nextPath.getRouteList().size() < 3) {
                        nextBackwardQueue.add(nextPath);
                    }
                }
                backwardVisited.add(nextDestination);
            }
            backwardQueue = nextBackwardQueue;
        }
        LOGGER.info("completed processing queue for {}:{} for {}",
                request.getOrigins().stream().sorted().collect(Collectors.toList()),
                request.getDestinations().stream().sorted().collect(Collectors.toList()),
                request.getStartTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    private String generatePathKey(Path path) {
        return path.getRouteList().stream()
                .map(Route::getId)
                .map(String::valueOf)
                .collect(Collectors.joining("->"));
    }

    private List<Path> combineAndDequeueBackwardPathMatches(
            Path forwardPath, Queue<Path> backwardQueue, String forwardDestination, Set<String> addedPathKeys) {
        List<Path> removedPaths = new ArrayList<>();

        backwardQueue.removeIf(backPath -> {
            List<Route> routeList = backPath.getRouteList();
            if (routeList.get(0).getOrigin().equals(forwardDestination)) {
                List<Route> combined = new ArrayList<>(forwardPath.getRouteList());
                combined.addAll(routeList);
                Path combinedPath = Path.builder()
                        .totalDistanceKm(forwardPath.getTotalDistanceKm()+backPath.getTotalDistanceKm())
                        .routeList(combined)
                        .build();
                String pathKey = generatePathKey(combinedPath);
                if (!addedPathKeys.contains(pathKey)) {
                    removedPaths.add(combinedPath);
                    addedPathKeys.add(pathKey);
                }
                return true;
            }
            return false;
        });

        return removedPaths;
    }

    private List<Path> combineAndDequeueForwardPathMatches(
            Path backwardPath, Queue<Path> forwardQueue, String backwardOrigin, Set<String> addedPathKeys) {
        List<Path> removedPaths = new ArrayList<>();

        forwardQueue.removeIf(forwardPath -> {
            List<Route> routeList = forwardPath.getRouteList();
            if (routeList.get(routeList.size()-1).getId().equals(backwardPath.getRouteList().get(0).getId())) {
                List<Route> combined = new ArrayList<>(forwardPath.getRouteList());
                combined.addAll(backwardPath.getRouteList());
                if (combined.size() < 4) {
                    Path combinedPath = Path.builder()
                            .totalDistanceKm(forwardPath.getTotalDistanceKm() + backwardPath.getTotalDistanceKm())
                            .routeList(combined)
                            .build();
                    String pathKey = generatePathKey(combinedPath);
                    if (!addedPathKeys.contains(pathKey)) {
                        removedPaths.add(combinedPath);
                        addedPathKeys.add(pathKey);
                    }
                }
                return true;
            }
            return false;
        });

        return removedPaths;
    }
}
