package org.voyager.service;

import org.voyager.model.Airline;
import org.voyager.model.AirportType;

import java.util.List;
import java.util.Optional;

public interface AirportsService<T> {
    public List<String> getIata();
    public List<String> getIataByType(AirportType type);
    public List<T> getAll(Optional<String> countryCode, Optional<AirportType> type, Optional<Airline> airline);
    public List<T> getByDistance(double latitude, double longitude, int limit, Optional<AirportType> type, Optional<Airline> airline);
    public Optional<T> getByIata(String iata);
}