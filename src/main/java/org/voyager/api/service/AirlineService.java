package org.voyager.api.service;

import lombok.NonNull;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineQuery;
import java.util.List;

public interface AirlineService {
    List<Airline> getAirlines();
    List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery);
}
