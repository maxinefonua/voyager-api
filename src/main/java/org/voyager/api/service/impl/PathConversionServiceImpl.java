package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.voyager.api.model.path.FlightDetailed;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.PathConversionService;
import org.voyager.api.service.RouteService;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightAirlineQuery;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.route.Route;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class PathConversionServiceImpl implements PathConversionService {
    @Autowired
    AirportsService airportsService;
    @Autowired
    FlightService flightService;
    @Autowired
    RouteService routeService;

    @Override
    public void convertStreaming(List<Path> pathList, PathSearchRequest pathSearchRequest, Consumer<PathDetailed> pathConsumer) {
        if (pathList == null || pathList.isEmpty()) {
            return;
        }
        pathList.forEach(path ->
                buildAllFlightPathsForPath(path, pathSearchRequest, pathConsumer));
    }

    private void buildAllFlightPathsForPath(Path path, PathSearchRequest request, Consumer<PathDetailed> pathConsumer) {
        List<Route> routeList = path.getRouteList();
        Route firstRoute = routeList.get(0);
        if (firstRoute.getId() == null) {
            pathConsumer.accept(PathDetailed.noDirectFlights(firstRoute.getOrigin(), firstRoute.getDestination()));
            return;
        }
        ZonedDateTime startTime = request.getStartTime();
        ZonedDateTime endTime = startTime.plusDays(1L);
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
