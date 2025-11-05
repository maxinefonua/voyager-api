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
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.repository.AirlineAirportRepository;
import org.voyager.api.repository.AirlineRepository;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.utils.MapperUtils;
import org.voyager.commons.model.airline.AirlineQuery;
import org.voyager.commons.model.geoname.fields.SearchOperator;

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
    @Cacheable("airlineCache")
    public List<Airline> getAirlines() {
        return handleJPAExceptions(()-> airlineRepository
                .findAll().stream().map(MapperUtils::entityToAirline).toList());
    }

    @Override
    public List<Airline> getAirlines(@NonNull AirlineQuery airlineQuery) {
        return handleJPAExceptions(()-> {
            switch (airlineQuery.getOperator()) {
                case OR:
                    return airlineAirportRepository
                            .selectDistinctAirlinesByIataInAndIsActive(airlineQuery.getIATAList(), true);
                case AND:
                    return airlineAirportRepository
                            .selectAirlinesWithAllAirports(airlineQuery.getIATAList(),
                                    true,airlineQuery.getIATAList().size());
                default:
                    LOGGER.error("getAirlines SearchOperator {} not yet implemented",airlineQuery.getOperator());
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
        return handleJPAExceptions(()-> {
            List<AirlineAirportEntity> airlineAirportEntityList = airlineAirportRepository.findByAirline(airline);
            airlineAirportRepository.deleteAll(airlineAirportEntityList);
            return airlineAirportEntityList.size();
        });
    }
}
