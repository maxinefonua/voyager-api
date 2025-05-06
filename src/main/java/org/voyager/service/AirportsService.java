package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.AirportDisplay;
import org.voyager.model.AirportType;

import java.util.List;


public interface AirportsService {
    public List<String> getIata();
    public List<String> getIataByType(AirportType type);
    public List<AirportDisplay> getAll(Option<String> countryCode, Option<AirportType> type, Option<Airline> airline);
    public List<AirportDisplay> getByDistance(double latitude, double longitude, int limit, Option<AirportType> type, Option<Airline> airline);
    public Option<AirportDisplay> getByIata(String iata);
}