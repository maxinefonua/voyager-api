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
    private List<String> iataList;
    private List<List<FlightDetailed>> flightPathList;

    public PathDetailed(@NonNull List<List<FlightDetailed>> flightPathList){
        if (flightPathList.isEmpty()) {
            throw new IllegalArgumentException("flightDetailedList cannot be empty");
        }
        List<FlightDetailed> firstPath = flightPathList.get(0);
        this.iataList = new ArrayList<>(){};
        iataList.add(firstPath.get(0).getOrigin());
        iataList.addAll(firstPath.stream().map(FlightDetailed::getDestination).toList());
        this.flightPathList = flightPathList;
    }
}
