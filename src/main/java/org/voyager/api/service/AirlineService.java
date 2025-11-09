package org.voyager.api.service;

import lombok.NonNull;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airline.AirlineQuery;

import java.util.List;

public interface AirlineService {
    boolean isActiveAirport(String iata, Airline airline);
    List<Airline> getAirlines();
    List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery);
    List<AirlineAirport> batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert);
    int batchDelete(Airline airline);
}
