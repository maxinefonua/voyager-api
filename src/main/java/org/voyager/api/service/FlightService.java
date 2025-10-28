package org.voyager.api.service;

import io.vavr.control.Option;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightPatch;

import java.util.List;

public interface FlightService {
    Boolean existsByFlightNumber(String flightNumber);
    Option<Flight> getById(Integer id);
    Option<Flight> getFlight(Integer routeId, String flightNumber);
    Flight save(FlightForm flightForm);
    Flight patch(Flight flight, FlightPatch flightPatch);
    List<Flight> getFlights(List<Integer> routeIdList, Option<String> flightNumberOption, Option<Airline> airlineOption, Option<Boolean> isActiveOption);
    Integer batchDelete(FlightBatchDelete flightBatchDelete);
}
