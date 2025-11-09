package org.voyager.api.service;

import io.vavr.control.Option;
import org.springframework.validation.annotation.Validated;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.*;

import java.util.List;

public interface FlightService {
    Boolean existsByFlightNumber(String flightNumber);
    Boolean existsByAirlineForEachRouteIdIn(Airline airline, List<Integer> routeIdList);
    Option<Flight> getById(Integer id);
    Option<Flight> getFlight(Integer routeId, String flightNumber);
    List<Integer> getAirlineRouteIds(Airline airline);
    Flight save(FlightForm flightForm);
    Flight patch(Flight flight, FlightPatch flightPatch);
    List<Flight> getFlights(List<Integer> routeIdList, Option<String> flightNumberOption, Option<Airline> airlineOption, Option<Boolean> isActiveOption);
    PagedResponse<Flight> getPagedFlights(@Validated FlightQuery flightQuery);
    Integer batchDelete(FlightBatchDelete flightBatchDelete);
}
