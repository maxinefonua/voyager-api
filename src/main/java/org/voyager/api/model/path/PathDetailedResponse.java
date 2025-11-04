package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder @Data
public class PathDetailedResponse {
    private String flightSearchId;
    private List<PathDetailed> pathDetailedList;
    private Integer page;
    private Integer pageSize;
    private Integer totalPathCount;
    private FlightSearchStatus flightSearchStatus;
}
