package org.voyager.api.cache;

import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.commons.model.airline.Airline;

import java.util.Set;
import java.util.stream.Collectors;

public class CacheKeyUtils {
    public static String generateSearchKey(PathSearchRequest request) {
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

    public static String generateResponseKey(PathSearchRequest request) {
        return String.format("response:%s:%s:%s:%s:%s:%s:%s",
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

    public static String generateConversionKey(PathSearchRequest request) {
        return String.format("response:%s:%s:%s:%s:%s:%s:%s",
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

    public static String generatePathKey(PathSearchRequest request) {
        return String.format("search:%s:%s",
                sortedJoinString(request.getOrigins()),
                sortedJoinString(request.getDestinations())
        );
    }

    private static String sortedJoinString(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "all";
        }
        return set.stream().sorted().collect(Collectors.joining(","));
    }
}
