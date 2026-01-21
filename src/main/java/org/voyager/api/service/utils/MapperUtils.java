package org.voyager.api.service.utils;

import org.voyager.api.model.entity.AirlineEntity;
import org.voyager.api.model.entity.RouteSyncEntity;
import org.voyager.api.model.entity.AirportEntity;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.model.entity.CountryEntity;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RouteSync;
import java.time.ZoneId;

public class MapperUtils {
    public static RouteSync entityToRouteSync(RouteSyncEntity routeSync) {
        return RouteSync.builder()
                .id(routeSync.getRouteId())
                .updated(routeSync.getUpdatedAt())
                .status(routeSync.getStatus())
                .lastProcessed(routeSync.getLastProcessedAt())
                .error(routeSync.getErrorMessage())
                .attempts(routeSync.getAttempts())
                .created(routeSync.getCreatedAt())
                .build();
    }

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

    public static Country entityToCountry(CountryEntity countryEntity) {
        return Country.builder()
                .code(countryEntity.getCode())
                .name(countryEntity.getName())
                .currencyCode(countryEntity.getCurrencyCode())
                .languages(countryEntity.getLanguages())
                .capitalCity(countryEntity.getCapitalCity())
                .population(countryEntity.getPopulation())
                .areaInSqKm(countryEntity.getAreaSqKm())
                .continent(countryEntity.getContinent())
                .bounds(countryEntity.getBounds())
                .build();
    }

    public static CountryEntity formToCountryEntity(CountryForm countryForm) {
        return CountryEntity.builder()
                .code(countryForm.getCode())
                .name(countryForm.getName())
                .languages(countryForm.getLanguages().toArray(new String[0]))
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
