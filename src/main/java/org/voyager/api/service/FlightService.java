package org.voyager.api.service;

import io.vavr.control.Option;
import org.springframework.validation.annotation.Validated;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightQuery;
import org.voyager.commons.model.flight.FlightBatchUpsertResult;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.response.PagedResponse;

import java.time.ZonedDateTime;
import java.util.List;

public interface FlightService {
    Boolean existsByFlightNumber(String flightNumber);
    Option<Flight> getById(Integer id);
    List<Flight> getFlights(FlightQuery flightQuery);
    PagedResponse<Flight> getPagedFlights(@Validated FlightQuery flightQuery);
    Integer batchDelete(FlightBatchDelete flightBatchDelete);
    FlightBatchUpsertResult batchUpsert(@Validated FlightBatchUpsert flightBatchUpsert);
    Option<Flight> getFlightOnDay(ZonedDateTime startOfDay, String flightNumber, Integer routeId);
}
