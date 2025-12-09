package org.voyager.api.cache;

import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.commons.model.airline.Airline;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CacheKeyUtils {
    public static String generatePathSearchKey(PathSearchRequest request) {
        return String.format("search:%s:%s:%s:%s:%s:%s:%s",
                sortedJoinString(request.getOrigins()),
                sortedJoinString(request.getDestinations()),
                sortedJoinString(request.getExcludeDestinations()),
                sortedJoinString(request.getExcludeFlightNumbers()),
                sortedJoinString(request.getAirlines().stream()
                        .map(Airline::name).collect(Collectors.toSet())),
                sortedJoinString(request.getExcludeRouteIds().stream()
                        .map(String::valueOf).collect(Collectors.toSet())),
                request.getStartTime() != null ? request.getStartTime().toEpochSecond() : "null"
        );
    }

    private static String sortedJoinString(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "all";
        }
        return set.stream().sorted().collect(Collectors.joining(","));
    }
}
