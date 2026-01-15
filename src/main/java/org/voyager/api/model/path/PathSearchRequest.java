package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Getter;
import org.voyager.commons.model.airline.Airline;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class PathSearchRequest {
    private Set<String> origins;
    private Set<String> destinations;
    private Set<String> excludeDestinations;
    private Set<String> excludeFlightNumbers;
    private Set<Integer> excludeRouteIds;
    private List<Airline> airlines;
    private ZonedDateTime startTime;
    private int skip;
    private int size;
}