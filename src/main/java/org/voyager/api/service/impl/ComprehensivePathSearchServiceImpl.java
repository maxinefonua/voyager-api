package org.voyager.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.FlightDetailed;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.ComprehensivePathSearchService;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightAirlineQuery;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteQuery;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ComprehensivePathSearchServiceImpl implements ComprehensivePathSearchService {
    @Autowired
    RouteService routeService;

    @Autowired
    FlightService flightService;

    @Autowired
    AirportsService airportsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensivePathSearchServiceImpl.class);

    @Override
    public void streamPaths(PathSearchRequest request, Consumer<PathDetailed> pathConsumer) {
        Set<String> excludeCodes = new HashSet<>(request.getExcludeDestinations());
        excludeCodes.addAll(request.getDestinations());

        List<Route> routeList = routeService.getRoutes(
                        RouteQuery.builder()
                                .originList(new ArrayList<>(request.getOrigins()))
                                .excludeRouteIdSet(request.getExcludeRouteIds())
                                .excludeDestinationSet(excludeCodes)
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
        processQueue(queue, request, pathConsumer);
    }

    private void processQueue(Queue<Path> queue,
                              PathSearchRequest request,
                              Consumer<PathDetailed> pathConsumer) {
        Set<String> visited = new HashSet<>(request.getExcludeDestinations());
        visited.addAll(request.getOrigins());
        Set<Integer> excludeRouteIds = request.getExcludeRouteIds();
        Set<String> destinationSet = request.getDestinations();

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
                        .originList(List.of(nextOrigin))
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
                        buildAllFlightPathsForPath(nextPath,request,pathConsumer);
                    } else if (nextPath.getRouteList().size() < 3) {
                        nextQueue.add(nextPath);
                    }
                }
            }
            queue = nextQueue;
        }
        LOGGER.info("completed processing queue for {}:{} for {}",
                request.getOrigins().stream().sorted().collect(Collectors.toList()),
                request.getDestinations().stream().sorted().collect(Collectors.toList()),
                request.getStartTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    private void buildAllFlightPathsForPath(Path path, PathSearchRequest request, Consumer<PathDetailed> pathConsumer) {
        ZonedDateTime startTime = request.getStartTime();
        ZonedDateTime endTime = startTime.plusDays(1L);
        List<Route> routeList = path.getRouteList();
        Route firstRoute = routeList.get(0);
        List<Airline> airlineList = request.getAirlines();

        // Add maximum search window to prevent infinite loops
        ZonedDateTime absoluteMaxTime = startTime.plusDays(7); // Max 1 week search

        List<Flight> firstFlightOptions = flightService.getFlights(
                FlightAirlineQuery.builder()
                        .airlineList(airlineList)
                        .isActive(true)
                        .routeIdList(List.of(firstRoute.getId()))
                        .startTime(startTime)
                        .endTime(endTime)
                        .build()
        );

        if (firstFlightOptions.isEmpty()) return;

        if (request.getExcludeFlightNumbers() != null) {
            firstFlightOptions = firstFlightOptions.stream().filter(flight ->
                    !request.getExcludeFlightNumbers().contains(flight.getFlightNumber())).toList();
        }

        List<List<FlightDetailed>> flightPathList = firstFlightOptions.stream()
                .map(firstFlight -> buildPathFromFirstFlight(
                        firstFlight, routeList, airlineList, endTime, absoluteMaxTime))
                .filter(list -> !list.isEmpty())
                .sorted(Comparator.comparing(list->list.get(0).getZonedDateTimeDeparture()))
                .toList();
        if (flightPathList.isEmpty()) return;
        pathConsumer.accept(new PathDetailed(flightPathList));
    }

    private List<FlightDetailed> buildPathFromFirstFlight(
            Flight firstFlight, List<Route> routeList, List<Airline> airlineList,
            ZonedDateTime originalEndTime, ZonedDateTime absoluteMaxTime) {
        List<FlightDetailed> flightDetailedList = new ArrayList<>();
        flightDetailedList.add(FlightDetailed.create(firstFlight, routeList.get(0)));

        Flight currentFlight = firstFlight;
        ZonedDateTime currentSearchEnd = originalEndTime;

        // Process remaining routes (skip first one)
        for (int i = 1; i < routeList.size(); i++) {
            Route nextRoute = routeList.get(i);

            Optional<Flight> nextFlightOpt = selectNextFlight(
                    airlineList, nextRoute.getId(), currentFlight, currentSearchEnd, absoluteMaxTime);

            if (nextFlightOpt.isEmpty()) return List.of();

            Flight nextFlight = nextFlightOpt.get();
            flightDetailedList.add(FlightDetailed.create(nextFlight, nextRoute));
            currentFlight = nextFlight;

            // Update search end time if we moved to next day
            if (nextFlight.getZonedDateTimeDeparture().toLocalDate().isAfter(
                    currentFlight.getZonedDateTimeDeparture().toLocalDate())) {
                currentSearchEnd = currentSearchEnd.plusDays(1);
            }

            // Check if we've exceeded absolute maximum time
            if (currentFlight.getZonedDateTimeDeparture().isAfter(absoluteMaxTime)) return List.of();
        }
        return flightDetailedList;
    }

    private Optional<Flight> selectNextFlight(List<Airline> airlineList, Integer routeId,
                                              Flight prevFlight, ZonedDateTime currentEndTime,
                                              ZonedDateTime absoluteMaxTime) {
        ZonedDateTime minDepartureTime = prevFlight.getZonedDateTimeArrival().plusMinutes(45);
        Airport connectingAirport = airportsService.getByIata(routeService.getRouteById(routeId).get().getOrigin());
        ZonedDateTime maxDepartureTime = prevFlight.getZonedDateTimeArrival()
                .withZoneSameInstant(connectingAirport.getZoneId())
                .with(LocalTime.MAX)
                .withZoneSameInstant(ZoneOffset.UTC); // Convert max to UTC

        ZonedDateTime searchEndTime = currentEndTime.isBefore(maxDepartureTime) ?
                currentEndTime : maxDepartureTime;

        // Don't search beyond absolute maximum
        if (minDepartureTime.isAfter(absoluteMaxTime)) {
            return Optional.empty();
        }
        if (searchEndTime.isAfter(absoluteMaxTime)) {
            searchEndTime = absoluteMaxTime;
        }

        List<Flight> nextFlights = flightService.getFlights(
                FlightAirlineQuery.builder()
                        .airlineList(airlineList)
                        .isActive(true)
                        .routeIdList(List.of(routeId))
                        .startTime(minDepartureTime)
                        .endTime(searchEndTime)
                        .build());

        // Priority 1: Same airline
        Optional<Flight> sameAirline = nextFlights.stream()
                .filter(nextFlight -> nextFlight.getAirline().equals(prevFlight.getAirline()))
                .filter(nextFlight -> nextFlight.getZonedDateTimeDeparture().isAfter(minDepartureTime))
                .min(Comparator.comparing(Flight::getZonedDateTimeDeparture));

        if (sameAirline.isPresent()) {
            return sameAirline;
        }

        // Priority 2: Any airline
        Optional<Flight> anyAirline = nextFlights.stream()
                .filter(nextFlight -> nextFlight.getZonedDateTimeDeparture().isAfter(minDepartureTime))
                .min(Comparator.comparing(Flight::getZonedDateTimeDeparture));

        if (anyAirline.isPresent()) {
            return anyAirline;
        }

        // Priority 3: Next day (within limits)
        return findNextDayFlight(airlineList, routeId, minDepartureTime, absoluteMaxTime,connectingAirport.getZoneId());
    }

    private Optional<Flight> findNextDayFlight(List<Airline> airlineList, Integer routeId,
                                               ZonedDateTime afterTime,
                                               ZonedDateTime absoluteMaxTime,
                                               ZoneId connectingZoneId) {
        // Get start of next day in connecting airport's timezone
        ZonedDateTime nextDayStart = afterTime.withZoneSameInstant(connectingZoneId)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(connectingZoneId)
                .withZoneSameInstant(ZoneOffset.UTC);

// Get end of next day in connecting airport's timezone
        ZonedDateTime nextDayEnd = afterTime.withZoneSameInstant(connectingZoneId)
                .toLocalDate()
                .plusDays(1)
                .atTime(LocalTime.MAX)
                .atZone(connectingZoneId)
                .withZoneSameInstant(ZoneOffset.UTC);

        // Don't search beyond absolute maximum
        if (nextDayStart.isAfter(absoluteMaxTime)) {
            return Optional.empty();
        }
        if (nextDayEnd.isAfter(absoluteMaxTime)) {
            nextDayEnd = absoluteMaxTime;
        }

        List<Flight> nextDayFlights = flightService.getFlights(
                FlightAirlineQuery.builder()
                        .airlineList(airlineList)
                        .isActive(true)
                        .routeIdList(List.of(routeId))
                        .startTime(nextDayStart)
                        .endTime(nextDayEnd)
                        .build());

        return nextDayFlights.stream()
                .min(Comparator.comparing(Flight::getZonedDateTimeDeparture));
    }
}
