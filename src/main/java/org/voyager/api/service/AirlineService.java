package org.voyager.api.service;

import lombok.NonNull;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airline.AirlineQuery;
import org.voyager.commons.model.route.Route;

import java.util.List;

public interface AirlineService {
    boolean isActiveAirport(String iata, Airline airline);
    List<Airline> getAirlines();
    List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery);
    List<AirlineAirport> batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert);
    int batchDelete(Airline airline);
    boolean isActiveAirlineRoute(Route route, List<Airline> airlineList);
    boolean hasAnyActiveAirlineForAllAirports(List<Airline> airlineList, List<String> iataList);
    List<Airline> getDistinctAirlinesForAllAirports(List<String> iataList);
}
