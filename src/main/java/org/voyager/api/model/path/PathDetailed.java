package org.voyager.api.model.path;

import lombok.Getter;
import lombok.NonNull;
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

    private PathDetailed(String origin, String destination){
        this.iataList = List.of(origin,destination);
        this.flightPathList = List.of();
    }

    public static PathDetailed noDirectFlights(String origin, String destination){
        return new PathDetailed(origin,destination);
    }
}
