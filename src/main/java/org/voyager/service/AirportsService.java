package org.voyager.service;

import io.vavr.control.Option;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;

import java.util.List;


public interface AirportsService {
    Boolean ifIataExists(String iata);
    public List<String> getIata();
    public List<String> getIataByType(AirportType type);
    public List<String> getIataByTypeIn(List<AirportType> typeList);
    public List<Airport> getAll(Option<String> countryCode, List<AirportType> airportTypeList, Option<Airline> airline);
    public List<Airport> getByDistance(double latitude, double longitude, int limit, List<AirportType> airportTypeList, List<Airline> airline);
    Airport getByIata(String iata);
    Airport patch(String iata, AirportPatch airportPatch);
}