package org.voyager.api.service.utils;

import org.voyager.api.model.entity.*;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightPatch;
import org.voyager.commons.model.location.*;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import java.time.*;
import java.util.*;

public class MapperUtils {
    public static Airport entityToAirport(AirportEntity airportEntity) {
        return Airport.builder()
                .iata(airportEntity.getIata())
                .name(airportEntity.getName())
                .city(airportEntity.getCity())
                .subdivision(airportEntity.getSubdivision())
                .countryCode(airportEntity.getCountryCode())
                .latitude(airportEntity.getLatitude())
                .longitude(airportEntity.getLongitude())
                .type(airportEntity.getType())
                .zoneId(airportEntity.getZoneId())
                .build();
    }

    public static Airport entityToAirport(AirportEntity airportEntity, Double distance) {
        return Airport.builder()
                .iata(airportEntity.getIata())
                .name(airportEntity.getName())
                .city(airportEntity.getCity())
                .subdivision(airportEntity.getSubdivision())
                .countryCode(airportEntity.getCountryCode())
                .latitude(airportEntity.getLatitude())
                .longitude(airportEntity.getLongitude())
                .type(airportEntity.getType())
                .distance(distance)
                .zoneId(airportEntity.getZoneId())
                .build();
    }

    public static Location entityToLocation(LocationEntity locationEntity) {
        return Location.builder()
                .name(locationEntity.getName())
                .bbox(locationEntity.getBbox())
                .id(locationEntity.getId())
                .latitude(locationEntity.getLatitude())
                .longitude(locationEntity.getLongitude())
                .countryCode(locationEntity.getCountryCode())
                .subdivision(locationEntity.getSubdivision())
                .status(locationEntity.getStatus())
                .source(locationEntity.getSource())
                .sourceId(locationEntity.getSourceId())
                .airports(new ArrayList<>(Arrays.asList(locationEntity.getAirports())))
                .build();
    }

    public static LocationEntity formToLocationEntity(LocationForm locationForm) {
        return LocationEntity.builder()
                .source(Source.valueOf(locationForm.getSource()))
                .sourceId(locationForm.getSourceId())
                .countryCode(locationForm.getCountryCode())
                .latitude(locationForm.getLatitude())
                .longitude(locationForm.getLongitude())
                .name(locationForm.getName())
                .status(Status.SAVED)
                .subdivision(locationForm.getSubdivision())
                .bbox(new Double[]{locationForm.getWest(),locationForm.getSouth(),locationForm.getEast(),locationForm.getNorth()})
                .airports(locationForm.getAirports().toArray(String[]::new))
                .build();
    }

    public static Route entityToRoute(RouteEntity routeEntity) {
        return Route.builder()
                .id(routeEntity.getId())
                .origin(routeEntity.getOrigin())
                .destination(routeEntity.getDestination())
                .distanceKm(routeEntity.getDistanceKm())
                .build();
    }

    public static RouteEntity formToRouteEntity(RouteForm routeForm) {
        return RouteEntity.builder()
                .origin(routeForm.getOrigin())
                .destination(routeForm.getDestination())
                .distanceKm(routeForm.getDistanceKm())
                .build();
    }

    public static Flight entityToFlight(FlightEntity flightEntity) {
        return Flight.builder()
                .id(flightEntity.getId())
                .flightNumber(flightEntity.getFlightNumber())
                .airline(flightEntity.getAirline())
                .routeId(flightEntity.getRouteId())
                .zonedDateTimeDeparture(flightEntity.getZonedDateTimeDeparture())
                .zonedDateTimeArrival(flightEntity.getZonedDateTimeArrival())
                .isActive(flightEntity.getIsActive())
                .build();
    }

    public static FlightEntity formToFlightEntity(FlightForm flightForm) {
        ZonedDateTime zonedDateTimeDeparture = null;
        ZonedDateTime zonedDateTimeArrival = null;

        if (flightForm.getDepartureTimestamp() != null) {
            zonedDateTimeDeparture = Instant.ofEpochSecond(flightForm.getDepartureTimestamp()).atOffset(
                    ZoneOffset.ofTotalSeconds(flightForm.getDepartureOffset().intValue())).toZonedDateTime();
        }
        if (flightForm.getArrivalTimestamp() != null) {
            zonedDateTimeArrival = Instant.ofEpochSecond(flightForm.getArrivalTimestamp()).atOffset(
                    ZoneOffset.ofTotalSeconds(flightForm.getArrivalOffset().intValue())).toZonedDateTime();
        }

        return FlightEntity.builder()
                .flightNumber(flightForm.getFlightNumber())
                .airline(Airline.valueOf(flightForm.getAirline()))
                .routeId(flightForm.getRouteId())
                .zonedDateTimeDeparture(zonedDateTimeDeparture)
                .zonedDateTimeArrival(zonedDateTimeArrival)
                .isActive(Boolean.valueOf(flightForm.getIsActive()))
                .build();
    }

    public static FlightEntity patchToFlightEntity(Flight flight, FlightPatch flightPatch) {
        ZonedDateTime zonedDateTimeDeparture = flight.getZonedDateTimeDeparture();
        ZonedDateTime zonedDateTimeArrival = flight.getZonedDateTimeArrival();
        if (flightPatch.getDepartureTimestamp() != null)
            zonedDateTimeDeparture = Instant.ofEpochSecond(flightPatch.getDepartureTimestamp()).atOffset(
                    ZoneOffset.ofTotalSeconds(flightPatch.getDepartureOffset().intValue())).toZonedDateTime();
        if (flightPatch.getArrivalTimestamp() != null)
            zonedDateTimeArrival = Instant.ofEpochSecond(flightPatch.getArrivalTimestamp()).atOffset(
                    ZoneOffset.ofTotalSeconds(flightPatch.getArrivalOffset().intValue())).toZonedDateTime();
        return FlightEntity.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .routeId(flight.getRouteId())
                .zonedDateTimeArrival(zonedDateTimeArrival)
                .zonedDateTimeDeparture(zonedDateTimeDeparture)
                .isActive(flightPatch.getIsActive() != null ? Boolean.valueOf(flightPatch.getIsActive()) : flight.getIsActive())
                .build();
    }

    public static Country entityToCountry(CountryEntity countryEntity) {
        return Country.builder()
                .code(countryEntity.getCode())
                .name(countryEntity.getName())
                .capitalCity(countryEntity.getCapitalCity())
                .population(countryEntity.getPopulation())
                .areaInSqKm(countryEntity.getAreaSqKm())
                .continent(countryEntity.getContinent())
                .bounds(countryEntity.getBounds())
                .build();
    }

    public static CountryEntity formToCountryEntity(CountryForm countryForm) {
        return CountryEntity.builder()
                .code(countryForm.getCountryCode())
                .name(countryForm.getCountryName())
                .capitalCity(countryForm.getCapitalCity())
                .population(countryForm.getPopulation())
                .areaSqKm(countryForm.getAreaInSqKm())
                .continent(Continent.valueOf(countryForm.getContinent()))
                .currencyCode(countryForm.getCurrencyCode())
                .bounds(new Double[]{
                        countryForm.getWest(),
                        countryForm.getSouth(),
                        countryForm.getEast(),
                        countryForm.getNorth()
                }).build();
    }

    public static Airline entityToAirline(AirlineEntity airlineEntity) {
        return airlineEntity.getAirline();
    }

    public static AirlineAirport entityToAirlineAirport(AirlineAirportEntity airlineAirportEntity) {
        return AirlineAirport.builder().airline(airlineAirportEntity.getAirline())
                .iata(airlineAirportEntity.getIata())
                .isActive(airlineAirportEntity.getIsActive()).build();
    }

    public static AirportEntity formToAirportEntity(AirportForm airportForm) {
        return AirportEntity.builder()
                .iata(airportForm.getIata())
                .name(airportForm.getName())
                .city(airportForm.getCity())
                .type(AirportType.valueOf(airportForm.getAirportType()))
                .countryCode(airportForm.getCountryCode())
                .latitude(Double.valueOf(airportForm.getLatitude()))
                .longitude(Double.valueOf(airportForm.getLongitude()))
                .subdivision(airportForm.getSubdivision())
                .zoneId(ZoneId.of(airportForm.getZoneId()))
                .build();
    }
}
