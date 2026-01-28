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
import org.voyager.api.repository.admin.AdminAirlineAirportRepository;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airline.AirlineBatchUpsertResult;
import org.voyager.commons.model.airline.AirlineQuery;
import org.voyager.commons.model.airline.AirlinePathQuery;
import org.voyager.commons.model.airline.AirlineAirportQuery;
import org.voyager.api.model.entity.AirlineAirportEntity;
import org.voyager.api.repository.primary.AirlineAirportRepository;
import org.voyager.api.repository.admin.AdminAirlineRepository;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.utils.MapperUtils;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class AirlineServiceImpl implements AirlineService {
    @Autowired
    AirlineAirportRepository airlineAirportRepository;

    @Autowired
    AdminAirlineAirportRepository adminAirlineAirportRepository;

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
                case OR -> airlineAirportRepository
                        .selectDistinctAirlinesByIataInAndIsActive(airlineAirportQuery.getIatalist(), true);
                case AND -> airlineAirportRepository
                        .selectAirlinesWithAllAirports(airlineAirportQuery.getIatalist(),
                                true, airlineAirportQuery.getIatalist().size());
            };
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
    }

    @Override
    @Transactional("adminTransactionManager")
    public AirlineBatchUpsertResult batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert) {
        Airline airline = Airline.valueOf(airlineBatchUpsert.getAirline());
        Boolean isActive = airlineBatchUpsert.getIsActive();
        List<String> iataList = airlineBatchUpsert.getIataList();

        List<AirlineAirportEntity> existingEntities =
                adminAirlineAirportRepository.findAllByAirlineAndIataIn(airline, iataList);

        Map<String, AirlineAirportEntity> existingMap = existingEntities.stream()
                .collect(Collectors.toMap(AirlineAirportEntity::getIata, Function.identity()));

        List<AirlineAirportEntity> toSave = new ArrayList<>();
        int creates = 0;
        int updates = 0;
        int skips = 0;
        for (String iata : iataList) {
            AirlineAirportEntity entity = existingMap.get(iata);
            if (entity != null) {
                if (entity.getIsActive() == null || !entity.getIsActive().equals(isActive)) {
                    entity.setIsActive(isActive);
                    toSave.add(entity);
                    LOGGER.debug("Updating existing entity for iata: {}", iata);
                    updates++;
                } else {
                    LOGGER.debug("Skipping unchanged entity for iata: {}", iata);
                    skips++;
                }
            } else {
                AirlineAirportEntity newEntity = AirlineAirportEntity.builder()
                        .airline(airline)
                        .iata(iata)
                        .isActive(isActive)
                        .build();
                toSave.add(newEntity);
                LOGGER.debug("Creating new entity for iata: {}", iata);
                creates++;
            }
        }

        if (!toSave.isEmpty()) {
            LOGGER.info("Batch saving {} airline-airport records", toSave.size());
            adminAirlineAirportRepository.saveAll(toSave);
        } else {
            LOGGER.info("No changes to save");
        }
        return AirlineBatchUpsertResult.builder()
                .createdCount(creates)
                .skippedCount(skips)
                .updatedCount(updates)
                .build();
    }

    @Transactional("adminTransactionManager")
    public int batchDelete(Airline airline) {
        return handleJPAExceptions(()-> adminAirlineAirportRepository.deleteByAirline(airline));
    }

    @Override
    public boolean hasAnyActiveAirlineForAllAirports(List<Airline> airlineList, List<String> iataList) {
        return handleJPAExceptions(() ->
                airlineAirportRepository.existsByAnyAirlineInAndAllAirportsIn(
                        iataList.size(), iataList,
                        true, airlineList));
    }
}
