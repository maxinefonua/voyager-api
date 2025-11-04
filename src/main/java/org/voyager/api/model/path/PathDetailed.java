package org.voyager.api.model.path;

import lombok.*;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.airline.Airline;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PathDetailed {
    private String pathOrigin;
    private String pathDestination;
    private Airline airline;
    private ZonedDateTime zonedDateTimeDeparture;
    private ZonedDateTime zonedDateTimeArrival;
    private Duration totalDuration;
    private Double totalDistanceKm;
    private Integer flightCount;
    private List<FlightDetailed> flightDetailedList;

    private PathDetailed(@NonNull List<FlightDetailed> flightDetailedList){
        if (flightDetailedList.isEmpty()) {
            throw new IllegalArgumentException("flightDetailedList cannot be empty");
        }
        FlightDetailed firstFlight = flightDetailedList.get(0);
        FlightDetailed lastFlight = flightDetailedList.get(flightDetailedList.size()-1);
        this.pathOrigin = firstFlight.getOrigin();
        this.pathDestination = lastFlight.getDestination();
        this.airline = firstFlight.getAirline();
        this.flightCount = flightDetailedList.size();
        this.zonedDateTimeDeparture = firstFlight.getZonedDateTimeDeparture();
        this.zonedDateTimeArrival = lastFlight.getZonedDateTimeArrival();
        this.totalDuration = Duration.between(zonedDateTimeDeparture,
                zonedDateTimeArrival);
        this.totalDistanceKm = flightDetailedList.stream().mapToDouble(FlightDetailed::getDistanceKm).sum();
        this.flightDetailedList = flightDetailedList;
    }

    public static PathDetailed create(FlightEntity flightEntity, RouteEntity routeEntity) {
        FlightDetailed flightDetailed = FlightDetailed.create(flightEntity,routeEntity);
        return new PathDetailed(List.of(flightDetailed));
    }

    public static PathDetailed createDeepCopy(PathDetailed pathDetailed,
                                              FlightEntity flightEntity,
                                              RouteEntity routeEntity) {
        FlightDetailed flightDetailed = FlightDetailed.create(flightEntity,routeEntity);
        List<FlightDetailed> copyList = new ArrayList<>(pathDetailed.getFlightDetailedList());
        copyList.add(flightDetailed);
        return new PathDetailed(copyList);
    }
}
