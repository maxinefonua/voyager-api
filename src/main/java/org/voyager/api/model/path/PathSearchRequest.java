package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.voyager.commons.model.airline.Airline;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Data @Builder
public class PathSearchRequest {
    private Set<String> origins;
    private Set<String> destinations;
    private Set<String> excludeDestinations;
    private Set<String> excludeFlightNumbers;
    private Set<Integer> excludeRouteIds;
    private List<Airline> airlines;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    @Builder.Default
    private int size = 10;
    private String cursor; // For pagination within a search session
    @Builder.Default
    private String sortBy = "duration"; // duration, price, stops
}