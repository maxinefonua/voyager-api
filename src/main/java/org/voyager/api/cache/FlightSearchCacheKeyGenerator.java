package org.voyager.api.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

@Component("flightCacheKeyGenerator")
public class FlightSearchCacheKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return "flights::" +
                method.getName() + "::" +
                generateSetKey((Set<String>) params[0]) + "::" +
                generateSetKey((Set<String>) params[1]);
    }

    private String generateSetKey(Set<String> airportCodes) {
        if (airportCodes == null) return "null";
        // Sort and join to ensure consistent key for same sets regardless of order
        return airportCodes.stream()
                .sorted()
                .collect(Collectors.joining(","));
    }
}
