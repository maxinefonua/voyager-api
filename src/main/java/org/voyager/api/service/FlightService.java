package org.voyager.api.service;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.voyager.api.model.path.FlightDetailed;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.*;
import org.voyager.commons.model.path.Path;

import java.time.ZonedDateTime;
import java.util.List;

public interface FlightService {
    Boolean existsByFlightNumber(String flightNumber);
    Boolean existsByAirlineForEachRouteIdIn(Airline airline, List<Integer> routeIdList);
    Boolean existsByAirlineForEachRouteIdInAndZonedDateTimeDepartureBetween(
            Airline airline, List<Integer> routeIdList, ZonedDateTime start, ZonedDateTime end);
    Boolean existsByAnyAirlineInForEachRouteIdInAndZonedDateTimeDepartureBetween(
            List<Airline> airlineList, List<Integer> routeIdList, ZonedDateTime start, ZonedDateTime end);

    Option<Flight> getById(Integer id);
    Option<Flight> getFlightWithArrivalAfter(Integer routeId, String flightNumber, ZonedDateTime departure);
    Option<Flight> getFlightWithDepartureBefore(Integer routeId, String flightNumber, ZonedDateTime arrival);
    Option<Flight> getFlight(Integer routeId, String flightNumber,
                             Option<ZonedDateTime> departureBeforeOption, Option<ZonedDateTime> arrivalAfterOption);
    List<Integer> getAirlineRouteIds(Airline airline);
    Flight save(FlightForm flightForm);
    Flight patch(Flight flight, FlightPatch flightPatch);
    List<Flight> getFlights(List<Integer> routeIdList, Option<String> flightNumberOption, Option<Airline> airlineOption, Option<Boolean> isActiveOption);
    List<Flight> getFlights(FlightQuery flightQuery);
    PagedResponse<Flight> getPagedFlights(@Validated FlightQuery flightQuery);
    Integer batchDelete(FlightBatchDelete flightBatchDelete);
    FlightBatchUpsertResult batchUpsert(@Validated FlightBatchUpsert flightBatchUpsert);
}
