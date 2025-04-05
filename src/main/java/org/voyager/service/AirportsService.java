package org.voyager.service;

import org.voyager.model.AirportType;

import java.util.List;
import java.util.Optional;

public interface AirportsService<T> {
    public List<String> getIata();
    public List<String> getIataByType(AirportType airportType);
    public List<T> getAll();
    public List<T> getByTypeSortedByDistance(double latitude, double longitude, AirportType type, int limit);
    public List<T> getByCountryCode(String countryCode, int limit);
    public List<T> getByCountryCode(String countryCode);
    public Optional<T> getByIata(String iata);
    public List<T> get(String countryCode, AirportType type);
}