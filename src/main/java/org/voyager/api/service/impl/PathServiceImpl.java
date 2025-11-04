package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.api.model.path.*;
import org.voyager.api.model.query.PathQuery;
import org.voyager.api.repository.AirlineAirportRepository;
import org.voyager.api.repository.FlightRepository;
import org.voyager.api.repository.RouteRepository;
import org.voyager.api.service.PathService;
import org.voyager.commons.model.airline.Airline;

import java.util.*;

@Service @Primary
public class PathServiceImpl implements PathService {
    @Autowired
    FlightRepository flightRepository;
    @Autowired
    RouteRepository routeRepository;
    @Autowired
    AirlineAirportRepository airlineAirportRepository;

    @Override
    public PathDetailedResponse getPathDetailedList(PathQuery pathQuery) {
       List<Airline> validAirlines = new ArrayList<>();
        if (pathQuery.getAirlineOption().isDefined()) {
            validAirlines.add(pathQuery.getAirlineOption().get());
        } else { // fetch valid airlines to pursue
            List<Airline> originAirlineList = airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(
                    pathQuery.getOriginSet()),true);

            List<Airline> destinationAirlineList = airlineAirportRepository.selectDistinctAirlinesByIataInAndIsActive(new ArrayList<>(
                    pathQuery.getDestinationSet()),true);


            originAirlineList.forEach(airline -> {
                if (destinationAirlineList.contains(airline)) {
                    validAirlines.add(airline);
                }
            });
        }

        // fetch routes where origin in origin set
        //  for each route, find flights with routeId and airline in validAirlines
        List<PathDetailed> results = new ArrayList<>();
        Set<String> secondLeg = new HashSet<>();

        // TODO: convert queue results into a Future with a search status that updates and returns with PathResponse
        Queue<InternalAirlinePath> queue = new PriorityQueue<>(Comparator.comparing(InternalAirlinePath::getTotalDistanceKm));
        routeRepository.findByDestinationIn(new ArrayList<>(pathQuery.getDestinationSet())).forEach(routeEntity -> {
            if (pathQuery.getOriginSet().contains(routeEntity.getOrigin())) {
                results.addAll(fetchFlightsBuildDetailed(routeEntity));
            } else if (airlineAirportRepository.existsByAirlineInAndIata(validAirlines,routeEntity.getOrigin())) {
                secondLeg.add(routeEntity.getOrigin());
            }
        });

        routeRepository.findByOriginIn(new ArrayList<>(pathQuery.getOriginSet())).forEach(routeEntity -> {
            // already added to results
            if (pathQuery.getDestinationSet().contains(routeEntity.getDestination())) return;

            if (secondLeg.contains(routeEntity.getDestination())) {
                List<Airline> secondLegAirlines = airlineAirportRepository
                        .selectDistinctAirlinesByIataAndIsActive(routeEntity.getDestination(),true)
                        .stream().filter(validAirlines::contains).toList();
                queue.addAll(fetchAirlinesBuildPaths(routeEntity,secondLegAirlines));
            }
        });
        return processQueue(queue,results,pathQuery.getDestinationSet(),pathQuery.getPage(),pathQuery.getPageSize());
    }

    private PathDetailedResponse processQueue(Queue<InternalAirlinePath> queue, List<PathDetailed> results,
                                              Set<String> destinationSet, int page, int pageSize) {
        while(!queue.isEmpty() & results.size() < page * pageSize) {
            InternalAirlinePath airlinePath = queue.poll();
            List<RouteEntity> routeEntityList = airlinePath.getRouteList();
            String nextOrigin = routeEntityList.get(routeEntityList.size()-1).getDestination();
            Airline airline = airlinePath.getAirline();
            routeRepository.findByOriginAndDestinationIn(nextOrigin,new ArrayList<>(destinationSet))
                    .forEach(routeEntity -> {
                        List<RouteEntity> totalRoutes = new ArrayList<>(routeEntityList);
                        totalRoutes.add(routeEntity);
                        results.addAll(fetchMutliFlightBuildDetailedFor(totalRoutes,airline));
            });
        }
        return PathDetailedResponse.builder()
                .flightSearchId("direct flights")
                .pathDetailedList(paginateResults(results,page,pageSize))
                .page(page)
                .pageSize(pageSize)
                .totalPathCount(results.size())
                .flightSearchStatus(FlightSearchStatus.COMPLETED)
                .build();
    }

    private List<PathDetailed> paginateResults(List<PathDetailed> results, int page, int pageSize) {
        int fromIndex = page * pageSize;
        if (fromIndex >= results.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, results.size());
        return results.subList(fromIndex, toIndex);
    }

    private List<InternalAirlinePath> fetchAirlinesBuildPaths(RouteEntity routeEntity,List<Airline> validAirlines) {
        List<Airline> destinationAirlines = airlineAirportRepository.selectDistinctAirlinesByIataAndIsActive(
                routeEntity.getDestination(),true);
        List<InternalAirlinePath> toQueue = new ArrayList<>();
        destinationAirlines.forEach(airline -> {
            if (validAirlines.contains(airline)) {
                toQueue.add(InternalAirlinePath.builder().airline(airline).totalDistanceKm(routeEntity.getDistanceKm())
                        .routeList(List.of(routeEntity)).build());
            }
        });
        return toQueue;
    }

    private List<PathDetailed> fetchFlightsBuildDetailed(RouteEntity routeEntity) {
        List<FlightEntity> flightEntityList = flightRepository.findByRouteIdAndIsActive(routeEntity.getId(),true);
        return flightEntityList.stream().map(flightEntity->
                PathDetailed.create(flightEntity,routeEntity)).toList();
    }

    private List<PathDetailed> fetchMutliFlightBuildDetailedFor(List<RouteEntity> routeEntityList, Airline airline) {
        Queue<PathDetailed> pathDetailedQueue = new PriorityQueue<>(Comparator.comparing(PathDetailed::getZonedDateTimeDeparture));
        List<FlightEntity> flightEntityList = flightRepository.findByRouteIdAndAirlineAndIsActive(
                routeEntityList.get(0).getId(),airline,true);
        flightEntityList.forEach(flightEntity->
                pathDetailedQueue.add(PathDetailed.create(flightEntity,routeEntityList.get(0))));

        int currIndex = 0;
        while (++currIndex < routeEntityList.size()) {
            RouteEntity routeEntity = routeEntityList.get(currIndex);
            List<PathDetailed> pathDetailedList = new ArrayList<>();
            while (!pathDetailedQueue.isEmpty()) {
                PathDetailed pathDetailed = pathDetailedQueue.poll();
                flightRepository.findByRouteIdAndAirlineAndIsActive(routeEntityList.get(currIndex).getId(), airline, true)
                        .forEach(flightEntity -> {
                                if (flightEntity.getZonedDateTimeDeparture().isAfter(pathDetailed.getZonedDateTimeArrival())) {
                                    pathDetailedList.add(PathDetailed.createDeepCopy(pathDetailed, flightEntity, routeEntity));
                                }
                            });
            }
            pathDetailedQueue.addAll(pathDetailedList);
        }
        return pathDetailedQueue.stream().sorted(Comparator.comparingInt(PathDetailed::getFlightCount)
                .thenComparingDouble(PathDetailed::getTotalDistanceKm)).toList();
    }
}
