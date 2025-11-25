package org.voyager.api.service.impl;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.api.service.RouteService;
import org.voyager.commons.model.airline.*;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.repository.AirlineAirportRepository;
import org.voyager.api.repository.AirlineRepository;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.utils.MapperUtils;
import org.voyager.commons.model.route.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class AirlineServiceImpl implements AirlineService {
    @Autowired
    AirlineAirportRepository airlineAirportRepository;
    @Autowired
    AirlineRepository airlineRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineServiceImpl.class);


    @Override
    @Cacheable("activeAirlineAirportCache")
    public boolean isActiveAirport(String iata, Airline airline) {
        return airlineAirportRepository.existsByAirlineAndIata(airline,iata);
    }

    @Override
    @Cacheable("airlineCache")
    public List<Airline> getAirlines() {
        return handleJPAExceptions(()-> airlineRepository
                .findAll().stream().map(MapperUtils::entityToAirline).toList());
    }

    @Override
    @Cacheable("airportAirlinesCache")
    public List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery) {
        return handleJPAExceptions(()-> {
            if (airlineQuery instanceof AirlineAirportQuery airlineAirportQuery) {
                switch (airlineAirportQuery.getOperator()) {
                    case OR:
                        return airlineAirportRepository
                                .selectDistinctAirlinesByIataInAndIsActive(airlineAirportQuery.getIatalist(), true);
                    case AND:
                        return airlineAirportRepository
                                .selectAirlinesWithAllAirports(airlineAirportQuery.getIatalist(),
                                        true, airlineAirportQuery.getIatalist().size());
                    default:
                        LOGGER.error("getAirlines SearchOperator {} not yet implemented",
                                airlineAirportQuery.getOperator());
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
                }
            } else if (airlineQuery instanceof AirlinePathQuery airlinePathQuery) {
                List<Airline> originAirlines = airlineAirportRepository
                        .selectDistinctAirlinesByIataInAndIsActive(airlinePathQuery.getOriginList(), true);
                List<Airline> destinationAirlines = airlineAirportRepository
                        .selectDistinctAirlinesByIataInAndIsActive(airlinePathQuery.getDestinationList(), true);
                return originAirlines.parallelStream().filter(destinationAirlines::contains).sorted().toList();
            } else {
                LOGGER.error("handling of AirlineQuery implementation {} not yet implemented",
                        airlineQuery.getClass().getSimpleName());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
        });
    }

    @Override
    public List<AirlineAirport> batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert) {
        return handleJPAExceptions(()-> {
            Airline airline = Airline.valueOf(airlineBatchUpsert.getAirline());
            Boolean isActive = airlineBatchUpsert.getIsActive();
            List<AirlineAirportEntity> updateList = new ArrayList<>();
            airlineBatchUpsert.getIataList().forEach(iata -> {
                Optional<AirlineAirportEntity> airlineAirportEntityOptional = airlineAirportRepository.findByIataAndAirline(iata, airline);
                if (airlineAirportEntityOptional.isPresent()) {
                    AirlineAirportEntity airlineAirportEntity = airlineAirportEntityOptional.get();
                    if (airlineAirportEntity.getIsActive() == null
                            || !airlineAirportEntity.getIsActive().equals(isActive)) {
                        airlineAirportEntity.setIsActive(isActive);
                        updateList.add(airlineAirportEntity);
                    } else {
                        LOGGER.info(String.format("skipping matching airline airport entity: %s",
                                airlineAirportEntity));
                    }
                } else {
                    AirlineAirportEntity airlineAirportEntity = AirlineAirportEntity.builder()
                            .airline(airline).iata(iata).isActive(isActive).build();
                    LOGGER.info(String.format("upserting new airline airport entity: %s", airlineAirportEntity));
                    updateList.add(airlineAirportEntity);
                }
            });
            LOGGER.info(String.format("saving upsert list of %d total", updateList.size()));
            return airlineAirportRepository.saveAll(updateList).stream()
                    .map(MapperUtils::entityToAirlineAirport).toList();
        });
    }

    @Override
    public int batchDelete(Airline airline) {
        return handleJPAExceptions(()-> airlineAirportRepository.deleteByAirline(airline));
    }

    @Override
    public boolean isActiveAirlineRoute(Route routeEntity, List<Airline> airlineList) {
        return handleJPAExceptions(()->
                airlineAirportRepository.existsByAnyAirlineInAndAllAirportsIn(
                        2,List.of(routeEntity.getOrigin(),routeEntity.getDestination()),
                        true,airlineList));
    }

    @Override
    public boolean hasAnyActiveAirlineForAllAirports(List<Airline> airlineList, List<String> iataList) {
        return handleJPAExceptions(() ->
                airlineAirportRepository.existsByAnyAirlineInAndAllAirportsIn(
                        iataList.size(), iataList,
                        true, airlineList));
    }

    @Override
    public List<Airline> getDistinctAirlinesForAllAirports(List<String> iataList) {
        return handleJPAExceptions(() ->
                airlineAirportRepository.selectDistinctAirlinesWithAllAirportsIn(
                        iataList,
                        true, iataList.size()));
    }
}
