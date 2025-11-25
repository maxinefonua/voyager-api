package org.voyager.api.model.path;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.route.Route;

import java.time.Duration;
import java.time.ZonedDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class FlightDetailed {
    private String flightNumber;
    private String origin;
    private String destination;
    private ZonedDateTime zonedDateTimeDeparture;
    private ZonedDateTime zonedDateTimeArrival;
    private Airline airline;
    private Duration duration;
    private Double distanceKm;

    public static FlightDetailed create(@NonNull FlightEntity flightEntity, @NonNull RouteEntity routeEntity) {
        if (flightEntity.getAirline() == null) throw new IllegalArgumentException("flight airline cannot be null");
        if (flightEntity.getFlightNumber() == null) throw new IllegalArgumentException("flight number cannot be null");
        if (flightEntity.getZonedDateTimeDeparture() == null) throw new IllegalArgumentException("flight zonedDateTimeDeparture cannot be null");
        if (flightEntity.getZonedDateTimeArrival() == null) throw new IllegalArgumentException("flight zonedDateTimeArrival cannot be null");

        if (routeEntity.getOrigin() == null) throw new IllegalArgumentException("route origin cannot be null");
        if (routeEntity.getDestination() == null) throw new IllegalArgumentException("route destination cannot be null");
        if (routeEntity.getDistanceKm() == null) throw new IllegalArgumentException("route distance cannot be null");

        Duration duration = Duration.between(flightEntity.getZonedDateTimeDeparture(),flightEntity.getZonedDateTimeArrival());

        return new FlightDetailed(flightEntity.getFlightNumber(),routeEntity.getOrigin(),routeEntity.getDestination(),
                flightEntity.getZonedDateTimeDeparture(),flightEntity.getZonedDateTimeArrival(),flightEntity.getAirline(),
                duration,routeEntity.getDistanceKm());
    }

    public static FlightDetailed create(@NonNull Flight flight, @NonNull Route route) {
        if (flight.getAirline() == null) throw new IllegalArgumentException("flight airline cannot be null");
        if (flight.getFlightNumber() == null) throw new IllegalArgumentException("flight number cannot be null");
        if (flight.getZonedDateTimeDeparture() == null) throw new IllegalArgumentException("flight zonedDateTimeDeparture cannot be null");
        if (flight.getZonedDateTimeArrival() == null) throw new IllegalArgumentException("flight zonedDateTimeArrival cannot be null");

        if (route.getOrigin() == null) throw new IllegalArgumentException("route origin cannot be null");
        if (route.getDestination() == null) throw new IllegalArgumentException("route destination cannot be null");
        if (route.getDistanceKm() == null) throw new IllegalArgumentException("route distance cannot be null");

        Duration duration = Duration.between(flight.getZonedDateTimeDeparture(),flight.getZonedDateTimeArrival());

        return new FlightDetailed(flight.getFlightNumber(),route.getOrigin(),route.getDestination(),
                flight.getZonedDateTimeDeparture(),flight.getZonedDateTimeArrival(),flight.getAirline(),
                duration,route.getDistanceKm());
    }
}
