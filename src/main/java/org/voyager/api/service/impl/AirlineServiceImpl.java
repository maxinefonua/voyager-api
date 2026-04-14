package org.voyager.api.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.entity.AirlineEntity;
import org.voyager.api.repository.primary.FlightRepository;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineQuery;
import org.voyager.commons.model.airline.AirlinePathQuery;
import org.voyager.commons.model.airline.AirlineAirportQuery;
import org.voyager.api.repository.admin.AdminAirlineRepository;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.utils.MapperUtils;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class AirlineServiceImpl implements AirlineService {
    @Autowired
    FlightRepository flightRepository;

    @Autowired
    AdminAirlineRepository adminAirlineRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineServiceImpl.class);

    @PostConstruct
    @Transactional("adminTransactionManager")
    public void initAirlines() {
        Set<Airline> dbAirlines = adminAirlineRepository.findAll().stream()
                .map(MapperUtils::entityToAirline).collect(Collectors.toSet());
        handleJPAExceptions(()-> Arrays.stream(Airline.values()).forEach(airline -> {
            if (!dbAirlines.contains(airline)) {
                AirlineEntity entity = AirlineEntity.builder()
                        .airline(airline)
                        .build();
                adminAirlineRepository.save(entity);
                LOGGER.info("Saved missing airline {} into db",airline);
            }
        }));
    }

    @Override
    @Cacheable("airlineCache")
    @Transactional("adminTransactionManager")
    public List<Airline> getAirlines() {
        return adminAirlineRepository.findAll().stream()
                .map(MapperUtils::entityToAirline).sorted(Comparator.comparing(Airline::name)).toList();
    }

    @Override
    public List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery) {
        if (airlineQuery instanceof AirlineAirportQuery airlineAirportQuery) {
            return switch (airlineAirportQuery.getOperator()) {
                case OR -> flightRepository
                        .findDistinctAirlinesForAnyIataIn(airlineAirportQuery.getIatalist());
                case AND -> flightRepository
                        .findDistinctAirlinesForAllIataIn(
                                airlineAirportQuery.getIatalist(),airlineAirportQuery.getIatalist().size());
            };
        } else if (airlineQuery instanceof AirlinePathQuery airlinePathQuery) {
            return flightRepository.findDistinctAirlinesWithOriginInAndDestinationIn(
                    airlinePathQuery.getOriginList(),airlinePathQuery.getDestinationList());
        } else {
            LOGGER.error("handling of AirlineQuery implementation {} not yet implemented",
                    airlineQuery.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
    }
}
