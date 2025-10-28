package org.voyager.api.service;

import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.api.model.query.AirlineQuery;

import java.util.List;

public interface AirlineService {
    List<Airline> getAirlines();
    List<Airline> getAirlines(AirlineQuery airlineQuery);
    List<AirlineAirport> batchUpsert(AirlineBatchUpsert airlineBatchUpsert);
    int batchDelete(Airline airline);
}
