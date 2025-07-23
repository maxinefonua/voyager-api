package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;

import java.util.List;

public interface FlightService {
    Option<Flight> getById(Integer id);
    Flight save(FlightForm flightForm);
    Flight patch(Flight flight, FlightPatch flightPatch);
    List<Flight> getFlights(List<Integer> routeIdList, Option<String> flightNumberOption, Option<Airline> airlineOption, Option<Boolean> isActiveOption);
}
