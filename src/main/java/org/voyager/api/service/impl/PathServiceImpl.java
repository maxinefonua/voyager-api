package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.*;
import org.voyager.api.model.query.PathQuery;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.api.service.*;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirportQuery;
import org.voyager.commons.model.airline.AirlinePathQuery;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.path.airline.AirlinePath;
import org.voyager.commons.model.path.airline.PathAirlineQuery;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteQuery;

import java.util.*;

@Service @Primary
public class PathServiceImpl implements PathService {
    @Autowired
    AirlineService airlineService;

    @Autowired
    RouteService routeService;

    @Autowired
    AirportsService airportsService;

    @Autowired
    FlightService flightService;

    @Override
    public PathDetailedResponse getPathDetailedList(PathQuery pathQuery) {
//       List<Airline> validAirlines = new ArrayList<>();
//        if (pathQuery.getAirlineOption().isDefined()) {
//            validAirlines.add(pathQuery.getAirlineOption().get());
//        } else { // fetch valid airlines to pursue
//            List<Airline> originAirlineList = airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(
//                    pathQuery.getOriginSet()),true);
//
//            List<Airline> destinationAirlineList = airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(
//                    pathQuery.getDestinationSet()),true);
//
//
//            originAirlineList.forEach(airline -> {
//                if (destinationAirlineList.contains(airline)) {
//                    validAirlines.add(airline);
//                }
//            });
//        }
//
//        // fetch routes where origin in origin set
//        //  for each route, find flights with routeId and airline in validAirlines
//        List<PathDetailed> results = new ArrayList<>();
//        Set<String> secondLeg = new HashSet<>();
//
//        // TODO: convert queue results into a Future with a search status that updates and returns with PathResponse
//        Queue<InternalAirlinePath> queue = new PriorityQueue<>(Comparator.comparing(InternalAirlinePath::getTotalDistanceKm));
//        routeRepository.findByDestinationIn(new ArrayList<>(pathQuery.getDestinationSet())).forEach(routeEntity -> {
//            if (pathQuery.getOriginSet().contains(routeEntity.getOrigin())) {
//                results.addAll(fetchFlightsBuildDetailed(routeEntity));
//            } else if (airlineAirportRepository.existsByAirlineInAndIata(validAirlines,routeEntity.getOrigin())) {
//                secondLeg.add(routeEntity.getOrigin());
//            }
//        });
//
//        routeRepository.findByOriginIn(new ArrayList<>(pathQuery.getOriginSet())).forEach(routeEntity -> {
//            // already added to results
//            if (pathQuery.getDestinationSet().contains(routeEntity.getDestination())) return;
//
//            if (secondLeg.contains(routeEntity.getDestination())) {
//                List<Airline> secondLegAirlines = airlineAirportRepository
//                        .selectDistinctAirlinesByIataAndIsActive(routeEntity.getDestination(),true)
//                        .stream().filter(validAirlines::contains).toList();
//                queue.addAll(fetchAirlinesBuildPaths(routeEntity,secondLegAirlines));
//            }
//        });
//        return processQueue(queue,results,pathQuery.getDestinationSet(),pathQuery.getPage(),pathQuery.getPageSize());
        return null;
    }

    @Override
    public PagedResponse<AirlinePath> getAirlinePathList(PathAirlineQuery pathAirlineQuery) {
        Set<String> originSet = pathAirlineQuery.getOriginSet();
        Set<String> destinationSet = pathAirlineQuery.getDestinationSet();
        Queue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::getTotalDistanceKm));
        List<AirlinePath> results = new ArrayList<>();
        Set<String> visited = new HashSet<>(originSet);
        if (pathAirlineQuery.getExcludeSet() != null) {
            visited.addAll(pathAirlineQuery.getExcludeSet());
        }

        Set<Integer> excludeRouteIds = new HashSet<>();
        if (pathAirlineQuery.getExcludeRouteIdSet() != null) {
            excludeRouteIds.addAll(pathAirlineQuery.getExcludeRouteIdSet());
        }

        List<Airline> validAirlines = new ArrayList<>();
        if (pathAirlineQuery.getAirline() != null) validAirlines.add(pathAirlineQuery.getAirline());
        else validAirlines.addAll(airlineService.getAirlines(
                AirlinePathQuery.builder()
                        .originList(new ArrayList<>(originSet))
                        .destinationList(new ArrayList<>(destinationSet))
                        .build()));
        // TODO: do we add exclude flights here? flights aren't checked here?
        originSet.forEach(origin -> {
            List<Route> routeList = routeService.getRoutes(RouteQuery.builder()
                    .origin(origin)
                    .excludeDestinationSet(visited)
                    .excludeRouteIdSet(excludeRouteIds)
                    .build());
            routeList.forEach(route -> {
                if (route.getDistanceKm() == null) return; // TODO: validate data
                Path path = Path.builder()
                        .routeList(List.of(route))
                        .totalDistanceKm(route.getDistanceKm())
                        .build();
                if (destinationSet.contains(route.getDestination())) {
                    results.addAll(buildAllAirlinePaths(path,validAirlines));
                } else {
                    queue.add(path);
                }
            }); // forEach route
        }); // forEach origin
        return processQueue(queue,destinationSet,visited,excludeRouteIds,results,validAirlines,
                pathAirlineQuery.getPage(),pathAirlineQuery.getSize());
    }

    @Override
    public PagedResponse<Path> getPathList(PathAirlineQuery pathAirlineQuery) {
        Set<String> originSet = pathAirlineQuery.getOriginSet();
        Set<String> destinationSet = pathAirlineQuery.getDestinationSet();
        Queue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::getTotalDistanceKm));
        List<Path> results = new ArrayList<>();
        Set<String> visited = new HashSet<>(originSet);
        if (pathAirlineQuery.getExcludeSet() != null) {
            visited.addAll(pathAirlineQuery.getExcludeSet());
        }

        Set<Integer> excludeRouteIds = new HashSet<>();
        if (pathAirlineQuery.getExcludeRouteIdSet() != null) {
            excludeRouteIds.addAll(pathAirlineQuery.getExcludeRouteIdSet());
        }

        // TODO: do we add exclude flights here? flights aren't checked here?
        originSet.forEach(origin -> {
            List<Route> routeList = routeService.getRoutes(RouteQuery.builder()
                    .origin(origin)
                    .excludeDestinationSet(visited)
                    .excludeRouteIdSet(excludeRouteIds)
                    .build());
            routeList.forEach(route -> {
                if (route.getDistanceKm() == null) return; // TODO: validate data
                Path path = Path.builder()
                        .routeList(List.of(route))
                        .totalDistanceKm(route.getDistanceKm())
                        .build();
                if (destinationSet.contains(route.getDestination())) {
                    results.add(path);
                } else {
                    queue.add(path);
                }
            }); // forEach route
        }); // forEach origin
        return processQueuePath(queue,destinationSet,visited,excludeRouteIds,results,
                pathAirlineQuery.getPage(),pathAirlineQuery.getSize());
    }

    private List<AirlinePath> buildAllAirlinePaths(Path path, List<Airline> validAirlines) {
        Set<String> pathAirports = new HashSet<>();
        List<Route> routeList = path.getRouteList();
        routeList.forEach(route -> {
            pathAirports.add(route.getOrigin());
            pathAirports.add(route.getDestination());
        });
        return airlineService.getAirlines(
                AirlineAirportQuery.builder()
                        .iatalist(new ArrayList<>(pathAirports))
                        .operator(SearchOperator.AND)
                        .build())
                .stream().filter(airline ->
                        validAirlines.contains(airline) && flightService.existsByAirlineForEachRouteIdIn(
                                airline, routeList.stream().map(Route::getId).toList()))
                .map(airline -> AirlinePath.builder()
                        .routeList(routeList)
                        .totalDistanceKm(path.getTotalDistanceKm())
                        .airline(airline)
                        .build()
        ).toList();
    }

    private PagedResponse<Path> processQueuePath(Queue<Path> queue,
                                                    Set<String> destinationSet,
                                                    Set<String> visited,
                                                    Set<Integer> excludeRouteIds,
                                                    List<Path> results,
                                                    int page, int size) {
        while (!queue.isEmpty()) {
            Queue<Path> nextQueue = new PriorityQueue<>(Comparator
                    .comparing(Path::getRouteList,Comparator.comparing(List::size))
                    .thenComparing(Path::getTotalDistanceKm));
            while (!queue.isEmpty()) {
                Path pathSoFar = queue.poll();
                List<Route> soFarRouteList = pathSoFar.getRouteList();
                String nextOrigin = soFarRouteList.get(soFarRouteList.size()-1).getDestination();
                visited.add(nextOrigin);
                for (Route route : routeService.getRoutes(RouteQuery.builder()
                        .origin(nextOrigin)
                        .excludeDestinationSet(visited)
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
                        results.add(nextPath);
                        if (results.size() >= (page+1)*size) {
                            return buildResponse(results,page,size);
                        }
                    } else {
                        nextQueue.add(nextPath);
                    }
                }
            }
            queue = nextQueue;
        }
        return buildResponse(results,page,size);
    }

    private PagedResponse<AirlinePath> processQueue(Queue<Path> queue,
                                                    Set<String> destinationSet,
                                                    Set<String> visited,
                                                    Set<Integer> excludeRouteIds,
                                                    List<AirlinePath> results,
                                                    List<Airline> validAirlines,
                                                    int page, int size) {
        while (!queue.isEmpty()) {
            Queue<Path> nextQueue = new PriorityQueue<>(Comparator
                    .comparing(Path::getRouteList,Comparator.comparing(List::size))
                    .thenComparing(Path::getTotalDistanceKm));
            while (!queue.isEmpty()) {
                Path pathSoFar = queue.poll();
                List<Route> soFarRouteList = pathSoFar.getRouteList();
                String nextOrigin = soFarRouteList.get(soFarRouteList.size()-1).getDestination();
                visited.add(nextOrigin);
                for (Route route : routeService.getRoutes(RouteQuery.builder()
                        .origin(nextOrigin)
                        .excludeDestinationSet(visited)
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
                        results.addAll(buildAllAirlinePaths(nextPath,validAirlines));
                        if (results.size() >= (page+1)*size) {
                            return buildResponse(results,page,size);
                        }
                    } else {
                        nextQueue.add(nextPath);
                    }
                }
            }
            queue = nextQueue;
        }
        return buildResponse(results,page,size);
    }

    private <T> PagedResponse<T> buildResponse(List<T> results, int page, int size) {
        List<T> pagedResults = paginateResults(results,page,size);
        return PagedResponse.<T>builder()
                .first(page == 0)
                .last((page+1)*size >= results.size())
                .totalPages(results.size()/size + results.size()%size)
                .totalElements(results.size())
                .content(pagedResults)
                .page(page)
                .size(size)
                .numberOfElements(pagedResults.size())
                .build();
    }

    private <T> List<T> paginateResults(List<T> results, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= results.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + size, results.size());
        return results.subList(fromIndex, toIndex);
    }


//    private PathDetailedResponse processQueue(Queue<InternalAirlinePath> queue, List<PathDetailed> results,
//                                              Set<String> destinationSet, int page, int pageSize) {
//        while(!queue.isEmpty() & results.size() < page * pageSize + pageSize) {
//            InternalAirlinePath airlinePath = queue.poll();
//            List<RouteEntity> routeEntityList = airlinePath.getRouteList();
//            String nextOrigin = routeEntityList.get(routeEntityList.size()-1).getDestination();
//            Airline airline = airlinePath.getAirline();
//            routeRepository.findByOriginAndDestinationIn(nextOrigin,new ArrayList<>(destinationSet))
//                    .forEach(routeEntity -> {
//                        List<RouteEntity> totalRoutes = new ArrayList<>(routeEntityList);
//                        totalRoutes.add(routeEntity);
//                        results.addAll(fetchMutliFlightBuildDetailedFor(totalRoutes,airline));
//            });
//        }
//        return PathDetailedResponse.builder()
//                .flightSearchId("direct flights")
//                .pathDetailedList(paginateResults(results,page,pageSize))
//                .page(page)
//                .pageSize(pageSize)
//                .totalPathCount(results.size())
//                .flightSearchStatus(FlightSearchStatus.COMPLETED)
//                .build();
//    }
//
//    private List<PathDetailed> paginateResults(List<PathDetailed> results, int page, int pageSize) {
//        int fromIndex = page * pageSize;
//        if (fromIndex >= results.size()) {
//            return Collections.emptyList();
//        }
//        int toIndex = Math.min(fromIndex + pageSize, results.size());
//        return results.subList(fromIndex, toIndex);
//    }
//
//    private List<InternalAirlinePath> fetchAirlinesBuildPaths(RouteEntity routeEntity,List<Airline> validAirlines) {
//        List<Airline> destinationAirlines = airlineAirportRepository.selectDistinctAirlinesByIataAndIsActive(
//                routeEntity.getDestination(),true);
//        List<InternalAirlinePath> toQueue = new ArrayList<>();
//        destinationAirlines.forEach(airline -> {
//            if (validAirlines.contains(airline)) {
//                toQueue.add(InternalAirlinePath.builder().airline(airline).totalDistanceKm(routeEntity.getDistanceKm())
//                        .routeList(List.of(routeEntity)).build());
//            }
//        });
//        return toQueue;
//    }
//
//    private List<PathDetailed> fetchFlightsBuildDetailed(RouteEntity routeEntity) {
//        List<FlightEntity> flightEntityList = flightRepository.findByRouteIdAndIsActive(routeEntity.getId(),true);
//        return flightEntityList.stream().map(flightEntity->
//                PathDetailed.create(flightEntity,routeEntity)).toList();
//    }
//
//    private List<PathDetailed> fetchMutliFlightBuildDetailedFor(List<RouteEntity> routeEntityList, Airline airline) {
//        Queue<PathDetailed> pathDetailedQueue = new PriorityQueue<>(Comparator.comparing(PathDetailed::getZonedDateTimeDeparture));
//        List<FlightEntity> flightEntityList = flightRepository.findByRouteIdAndAirlineAndIsActive(
//                routeEntityList.get(0).getId(),airline,true);
//        flightEntityList.forEach(flightEntity->
//                pathDetailedQueue.add(PathDetailed.create(flightEntity,routeEntityList.get(0))));
//
//        int currIndex = 0;
//        while (++currIndex < routeEntityList.size()) {
//            RouteEntity routeEntity = routeEntityList.get(currIndex);
//            List<PathDetailed> pathDetailedList = new ArrayList<>();
//            while (!pathDetailedQueue.isEmpty()) {
//                PathDetailed pathDetailed = pathDetailedQueue.poll();
//                flightRepository.findByRouteIdAndAirlineAndIsActive(routeEntityList.get(currIndex).getId(), airline, true)
//                        .forEach(flightEntity -> {
//                                if (flightEntity.getZonedDateTimeDeparture().isAfter(pathDetailed.getZonedDateTimeArrival())) {
//                                    pathDetailedList.add(PathDetailed.createDeepCopy(pathDetailed, flightEntity, routeEntity));
//                                }
//                            });
//            }
//            pathDetailedQueue.addAll(pathDetailedList);
//        }
//        return pathDetailedQueue.stream().sorted(Comparator.comparingInt(PathDetailed::getFlightCount)
//                .thenComparingDouble(PathDetailed::getTotalDistanceKm)).toList();
//    }
}
