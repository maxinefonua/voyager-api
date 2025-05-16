package org.voyager.service.utils;

import org.voyager.entity.Airport;
import org.voyager.model.Airline;
import org.voyager.model.AirportDisplay;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.entity.Delta;
import org.voyager.model.entity.Route;
import org.voyager.model.location.LocationDisplay;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.entity.Location;
import org.voyager.model.location.LocationForm;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

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
                .subdivision(location.getSubdivision())
                .status(location.getStatus())
                .source(location.getSource())
                .sourceId(location.getSourceId())
                .build();
    }

    public static Location formToLocation(LocationForm locationForm) {
        return Location.builder()
                .source(Source.valueOf(locationForm.getSource()))
                .sourceId(locationForm.getSourceId())
                .countryCode(locationForm.getCountryCode())
                .latitude(locationForm.getLatitude())
                .longitude(locationForm.getLongitude())
                .name(locationForm.getName())
                .status(Status.SAVED)
                .subdivision(locationForm.getSubdivision())
                .bbox(new Double[]{locationForm.getWest(),locationForm.getSouth(),locationForm.getEast(),locationForm.getNorth()})
                .build();
    }

    public static RouteDisplay routeToDisplay(Route route) {
        return RouteDisplay.builder()
                .id(route.getId())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .airline(route.getAirline())
                .isActive(route.getIsActive())
                .build();
    }

    public static Route formToRoute(RouteForm routeForm) {
        return Route.builder()
                .origin(routeForm.getOrigin())
                .destination(routeForm.getDestination())
                .airline(Airline.valueOf(routeForm.getAirline()))
                .isActive(routeForm.getIsActive())
                .build();
    }

    public static Route patchDisplayToRoute(RouteDisplay routeDisplay, RoutePatch routePatch) {
        return Route.builder()
                .id(routeDisplay.getId())
                .origin(routeDisplay.getOrigin())
                .destination(routeDisplay.getDestination())
                .airline(routeDisplay.getAirline())
                .isActive(routePatch.getIsActive())
                .build();
    }

    public static DeltaDisplay deltaToDisplay(Delta delta) {
        return DeltaDisplay.builder()
                .iata(delta.getIata())
                .status(delta.getStatus())
                .isHub(delta.getIsHub())
                .build();
    }

    public static Delta formToDelta(DeltaForm deltaForm) {
        return Delta.builder()
                .iata(deltaForm.getIata())
                .status(DeltaStatus.valueOf(deltaForm.getStatus()))
                .isHub(deltaForm.getIsHub())
                .build();
    }

    public static Delta patchDisplayToDelta(DeltaDisplay deltaDisplay, DeltaPatch deltaPatch) {
        return Delta.builder()
                .iata(deltaDisplay.getIata())
                .status(DeltaStatus.valueOf(deltaPatch.getStatus()))
                .isHub(deltaPatch.getIsHub())
                .build();
    }
}
