package org.voyager.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.repository.AirlineAirportRepository;
import org.voyager.api.repository.FlightRepository;
import org.voyager.api.repository.RouteRepository;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.FlightSearchService;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.RouteService;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlinePathQuery;
import org.voyager.commons.model.airline.AirlineQuery;
import org.voyager.commons.validate.annotations.ValidAirportCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlightSearchServiceImpl implements FlightSearchService {
    @Autowired
    RouteService routeService;

    @Autowired
    FlightService flightService;

    @Autowired
    AirlineService airlineService;

    @Override
    @Cacheable(value = "flights", keyGenerator = "flightCacheKeyGenerator")
    public List<PathDetailed> findAllFlights(Set<@ValidAirportCode String> originSet,
                                             Set<@ValidAirportCode String> destinationSet) {
        List<Airline> validAirlines = airlineService.getAirlines(
                AirlinePathQuery.builder().originList(new ArrayList<>(originSet))
                        .destinationList(new ArrayList<>(destinationSet)).build());

        return List.of();
    }

    @Override
    public List<PathDetailed> findDirectFlights(Set<@ValidAirportCode String> originSet, Set<@ValidAirportCode String> destinationSet) {
        return List.of();
    }
}
