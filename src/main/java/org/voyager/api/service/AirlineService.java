package org.voyager.api.service;

import lombok.NonNull;
import org.voyager.commons.model.airline.*;

import java.util.List;

public interface AirlineService {
    List<Airline> getAirlines();
    List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery);
    AirlineBatchUpsertResult batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert);
    int batchDelete(Airline airline);
    boolean hasAnyActiveAirlineForAllAirports(List<Airline> airlineList, List<String> iataList);
}
