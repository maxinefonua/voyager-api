package org.voyager.service.utils;

import org.voyager.entity.Airport;
import org.voyager.model.AirportDisplay;

public class MapperUtils {
    public static AirportDisplay airportToDisplay(Airport airport) {
        return AirportDisplay.builder().iata(airport.getIata()).name(airport.getName())
                .city(airport.getCity()).subdivision(airport.getSubdivision()).countryCode(airport.getCountryCode())
                .latitude(airport.getLatitude()).longitude(airport.getLongitude()).type(airport.getType()).build();
    }

    public static AirportDisplay airportToDisplay(Airport airport, Double distance) {
        return AirportDisplay.builder().iata(airport.getIata()).name(airport.getName())
                .city(airport.getCity()).subdivision(airport.getSubdivision()).countryCode(airport.getCountryCode())
                .latitude(airport.getLatitude()).longitude(airport.getLongitude()).type(airport.getType())
                .distance(distance).build();
    }
}
