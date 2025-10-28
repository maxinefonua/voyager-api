package org.voyager.api.service;

import io.vavr.control.Option;
import org.springframework.validation.annotation.Validated;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.api.model.query.IataQuery;

import java.util.List;


public interface AirportsService {
    Boolean ifIataExists(String iata);
    public List<String> getIata();
    public List<String> getIata(IataQuery iataQuery);
    public List<Airport> getAll(Option<String> countryCode, List<AirportType> airportTypeList, Option<Airline> airline);
    public List<Airport> getByDistance(double latitude, double longitude, int limit, List<AirportType> airportTypeList, List<Airline> airline);
    Airport getByIata(String iata);
    Airport patch(String iata, AirportPatch airportPatch);
    Airport createAirport(@Validated AirportForm airportForm);
}