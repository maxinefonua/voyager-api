package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;

import java.util.List;

public interface FlightService {
    List<Flight> getFlights(Airline airline);
    List<Flight> getFlights(Boolean isActive);
    List<Flight> getFlights(Integer routeId);
    List<Flight> getFlights(String flightNumber);
    List<Flight> getFlights(Integer routeId,String flightNumber);
    List<Flight> getFlights(Integer routeId,Boolean isActive);
    List<Flight> getFlights(Integer routeId,String flightNumber,Boolean isActive);
    List<Flight> getFlights(String flightNumber,Boolean isActive);
    List<Flight> getAll();
    Option<Flight> getById(Integer id);
    Flight save(FlightForm flightForm);
    Flight patch(Flight flight, FlightPatch flightPatch);
}
