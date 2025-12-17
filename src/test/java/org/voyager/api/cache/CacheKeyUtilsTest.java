package org.voyager.api.cache;

import org.junit.jupiter.api.Test;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.commons.model.airline.Airline;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CacheKeyUtilsTest {
    @Test
    public void testKey() {
        PathSearchRequest request1 = PathSearchRequest.builder()
                .origins(Set.of("LAX", "SFO"))
                .destinations(Set.of("JFK", "LGA"))
                .excludeDestinations(Set.of("SLC", "ATL"))
                .excludeFlightNumbers(Set.of("DL1255","DL902"))
                .airlines(List.of(Airline.DELTA))
                .excludeRouteIds(Set.of(1234,6,7))
                .skip(0)
                .size(10)
                .build();
        String cacheKey1 = CacheKeyUtils.generateSearchKey(request1);

        PathSearchRequest request2 = PathSearchRequest.builder()
                .origins(Set.of("SFO", "LAX"))
                .destinations(Set.of("LGA", "JFK"))
                .excludeDestinations(Set.of("ATL", "SLC"))
                .excludeFlightNumbers(Set.of("DL902","DL1255"))
                .airlines(List.of(Airline.DELTA))
                .excludeRouteIds(Set.of(7,1234,6))
                .skip(20)
                .size(10)
                .build();
        String cacheKey2 = CacheKeyUtils.generateSearchKey(request2);
        assertEquals(cacheKey1,cacheKey2);
    }
}