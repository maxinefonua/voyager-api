package org.voyager.service.utils;

import org.voyager.model.airport.Airport;
import org.voyager.model.entity.AirportEntity;
import org.voyager.model.Airline;
import org.voyager.model.delta.Delta;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.entity.DeltaEntity;
import org.voyager.model.entity.LocationEntity;
import org.voyager.model.entity.RouteEntity;
import org.voyager.model.location.*;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class MapperUtils {
    public static Airport entityToAirport(AirportEntity airportEntity) {
        return Airport.builder().iata(airportEntity.getIata()).name(airportEntity.getName())
                .city(airportEntity.getCity()).subdivision(airportEntity.getSubdivision()).countryCode(airportEntity.getCountryCode())
                .latitude(airportEntity.getLatitude()).longitude(airportEntity.getLongitude()).type(airportEntity.getType()).build();
    }

    public static Airport entityToAirport(AirportEntity airportEntity, Double distance) {
        return Airport.builder().iata(airportEntity.getIata()).name(airportEntity.getName())
                .city(airportEntity.getCity()).subdivision(airportEntity.getSubdivision()).countryCode(airportEntity.getCountryCode())
                .latitude(airportEntity.getLatitude()).longitude(airportEntity.getLongitude()).type(airportEntity.getType())
                .distance(distance).build();
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
                .airports(new HashSet<>(Set.of(locationEntity.getAirports())))
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
                .airline(routeEntity.getAirline())
                .isActive(routeEntity.getIsActive())
                .build();
    }

    public static RouteEntity formToRouteEntity(RouteForm routeForm) {
        return RouteEntity.builder()
                .origin(routeForm.getOrigin())
                .destination(routeForm.getDestination())
                .airline(Airline.valueOf(routeForm.getAirline()))
                .isActive(routeForm.getIsActive())
                .build();
    }

    public static RouteEntity patchToRouteEntity(Route route, RoutePatch routePatch) {
        return RouteEntity.builder()
                .id(route.getId())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .airline(route.getAirline())
                .isActive(routePatch.getIsActive())
                .build();
    }

    public static Delta entityToDelta(DeltaEntity deltaEntity) {
        return Delta.builder()
                .iata(deltaEntity.getIata())
                .status(deltaEntity.getStatus())
                .isHub(deltaEntity.getIsHub())
                .build();
    }

    public static DeltaEntity formToDeltaEntity(DeltaForm deltaForm) {
        return DeltaEntity.builder()
                .iata(deltaForm.getIata())
                .status(DeltaStatus.valueOf(deltaForm.getStatus()))
                .isHub(deltaForm.getIsHub())
                .build();
    }

    public static DeltaEntity patchToDeltaEntity(Delta delta, DeltaPatch deltaPatch) {
        return DeltaEntity.builder()
                .iata(delta.getIata())
                .status(DeltaStatus.valueOf(deltaPatch.getStatus()))
                .isHub(deltaPatch.getIsHub())
                .build();
    }
}
