package org.voyager.service.utils;

import org.voyager.entity.Airport;
import org.voyager.model.AirportDisplay;
import org.voyager.model.LocationDisplay;
import org.voyager.model.entity.Location;
import org.voyager.model.location.LocationForm;

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

    public static LocationDisplay locationToDisplay(Location location) {
        return LocationDisplay.builder()
                .name(location.getName())
                .bbox(location.getBbox())
                .id(location.getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .countryCode(location.getCountryCode())
                .status(location.getStatus())
                .build();
    }

    public static Location formToLocation(LocationForm locationForm) {
        return Location.builder()
                .source(locationForm.getSource())
                .sourceId(locationForm.getSourceId())
                .countryCode(locationForm.getCountryCode())
                .latitude(locationForm.getLatitude())
                .longitude(locationForm.getLongitude())
                .name(locationForm.getName())
                .status(Location.Status.ACTIVE)
                .subdivision(locationForm.getSubdivision())
                .bbox(new Double[]{locationForm.getWest(),locationForm.getSouth(),locationForm.getEast(),locationForm.getNorth()})
                .build();
    }
}
