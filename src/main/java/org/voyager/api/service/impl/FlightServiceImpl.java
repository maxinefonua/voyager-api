package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.api.repository.admin.AdminFlightRepository;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightAirlineQuery;
import org.voyager.commons.model.flight.FlightQuery;
import org.voyager.commons.model.flight.FlightUpsert;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.flight.FlightBatchUpsertResult;
import org.voyager.commons.model.flight.FlightNumberQuery;
import org.voyager.api.repository.primary.FlightRepository;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.utils.MapperUtils;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class FlightServiceImpl implements FlightService {
    @Autowired
    FlightRepository flightRepository;

    @Autowired
    AdminFlightRepository adminFlightRepository;

    @Override
    public Boolean existsByFlightNumber(String flightNumber) {
        return flightRepository.existsByFlightNumber(flightNumber);
    }

    @Override
    public Option<Flight> getById(Integer id) {
        Optional<FlightEntity> flightEntity = flightRepository.findById(id);
        if (flightEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
    }

    @Override
    public Option<Flight> getFlightOnDay(ZonedDateTime startOfDay, String flightNumber, Integer routeId) {
        ZonedDateTime endOfDay = startOfDay.plusDays(1L);
        Optional<FlightEntity> flightEntity = flightRepository
                .findByRouteIdAndFlightNumberAndZonedDateTimeDepartureBetween(routeId,flightNumber,startOfDay,endOfDay);
        if (flightEntity.isPresent()) return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
        return Option.none();
    }

    @Override
    @Cacheable("getFlightsCache")
    public List<Flight> getFlights(FlightQuery flightQuery) {
        return flightRepository.findFlightsDynamic(
                flightQuery.getRouteIdList(),
                getAirlineList(flightQuery),
                getFlightNumber(flightQuery),
                flightQuery.getIsActive(),
                flightQuery.getStartTime(),
                flightQuery.getEndTime())
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public PagedResponse<Flight> getPagedFlights(@Validated FlightQuery flightQuery) {
        Page<FlightEntity> pagedFlightEntities = flightRepository.findFlightsDynamic(
                flightQuery.getRouteIdList(),
                getAirlineList(flightQuery),
                getFlightNumber(flightQuery),
                flightQuery.getIsActive(),
                flightQuery.getStartTime(),
                flightQuery.getEndTime(),
                Pageable.ofSize(flightQuery.getPageSize()).withPage(flightQuery.getPage())
        );
        List<Flight> content = pagedFlightEntities.get().map(MapperUtils::entityToFlight).toList();
        return PagedResponse.<Flight>builder()
                .content(content)
                .page(flightQuery.getPage())
                .size(flightQuery.getPageSize())
                .totalElements(pagedFlightEntities.getTotalElements())
                .totalPages(pagedFlightEntities.getTotalPages())
                .first(pagedFlightEntities.isFirst())
                .last(pagedFlightEntities.isLast())
                .numberOfElements(content.size())
                .build();
    }

    private List<Airline> getAirlineList(FlightQuery flightQuery) {
        return flightQuery instanceof FlightAirlineQuery airlineQuery ?
                airlineQuery.getAirlineList() : null;
    }

    private String getFlightNumber(FlightQuery flightQuery) {
        return flightQuery instanceof FlightNumberQuery numberQuery ?
                numberQuery.getFlightNumber() : null;
    }

    @Override
    @Transactional("adminTransactionManager")
    public Integer batchDelete(FlightBatchDelete flightBatchDelete) {
        return handleJPAExceptions(()-> {
            if (StringUtils.isNotBlank(flightBatchDelete.getDaysPast())
                    && StringUtils.isNotBlank(flightBatchDelete.getAirline())
                    && StringUtils.isNotBlank(flightBatchDelete.getIsActive())) {
                ZonedDateTime cutoff = Instant.now().atZone(ZoneOffset.UTC)
                        .minusDays(Integer.parseInt(flightBatchDelete.getDaysPast()));
                Airline airline = Airline.valueOf(flightBatchDelete.getAirline().toUpperCase());
                boolean isActive = Boolean.parseBoolean(flightBatchDelete.getIsActive().toLowerCase());
                return flightRepository.deleteByZonedDateTimeArrivalBeforeAndAirlineAndIsActive(
                        cutoff,airline,isActive);
            }
            if (StringUtils.isNotBlank(flightBatchDelete.getDaysPast())
                    && StringUtils.isNotBlank(flightBatchDelete.getAirline())) {
                ZonedDateTime cutoff = Instant.now().atZone(ZoneOffset.UTC)
                        .minusDays(Integer.parseInt(flightBatchDelete.getDaysPast()));
                Airline airline = Airline.valueOf(flightBatchDelete.getAirline().toUpperCase());
                return flightRepository.deleteByZonedDateTimeArrivalBeforeAndAirline(cutoff,airline);
            }
            if (StringUtils.isNotBlank(flightBatchDelete.getDaysPast())
                    && StringUtils.isNotBlank(flightBatchDelete.getIsActive())) {
                ZonedDateTime cutoff = Instant.now().atZone(ZoneOffset.UTC)
                        .minusDays(Integer.parseInt(flightBatchDelete.getDaysPast()));
                boolean isActive = Boolean.parseBoolean(flightBatchDelete.getIsActive().toLowerCase());
                return flightRepository.deleteByZonedDateTimeArrivalBeforeAndIsActive(cutoff,isActive);
            }
            if (StringUtils.isNotBlank(flightBatchDelete.getAirline())
                    && StringUtils.isNotBlank(flightBatchDelete.getIsActive()))
                return flightRepository.deleteByAirlineAndIsActive(
                        Airline.valueOf(flightBatchDelete.getAirline().toUpperCase()),
                        Boolean.valueOf(flightBatchDelete.getIsActive().toLowerCase()));
            if (StringUtils.isNotBlank(flightBatchDelete.getAirline())) {
                return flightRepository.deleteByAirline(
                        Airline.valueOf(flightBatchDelete.getAirline().toUpperCase()));
            }
            if (StringUtils.isNotBlank(flightBatchDelete.getIsActive())) {
                return flightRepository.deleteByIsActive(
                        Boolean.valueOf(flightBatchDelete.getIsActive().toLowerCase()));
            }
            if (StringUtils.isNotBlank(flightBatchDelete.getDaysPast())) {
                ZonedDateTime cutoff = Instant.now().atZone(ZoneOffset.UTC)
                        .minusDays(Integer.parseInt(flightBatchDelete.getDaysPast()));
                return flightRepository.deleteByZonedDateTimeArrivalBefore(cutoff);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        });
    }

    @Override
    @Transactional("adminTransactionManager")
    public FlightBatchUpsertResult batchUpsert(@Validated FlightBatchUpsert flightBatchUpsert) {
        List<FlightUpsert> flightUpsertList = flightBatchUpsert.getFlightUpsertList();
        AtomicInteger totalCreates = new AtomicInteger(0);
        AtomicInteger totalUpdates = new AtomicInteger(0);
        AtomicInteger totalSkips = new AtomicInteger(0);
        List<FlightEntity> totalSaves = new ArrayList<>();

        for (FlightUpsert flightUpsert : flightUpsertList) {
            Integer routeId = Integer.parseInt(flightUpsert.getRouteId());
            boolean isArrival = Boolean.parseBoolean(flightUpsert.getIsArrival().toLowerCase());
            String flightNumber = flightUpsert.getFlightNumber();
            Map<ZonedDateTime,FlightEntity> arrivalMap = new HashMap<>();
            Map<ZonedDateTime,FlightEntity> departureMap = new HashMap<>();
            adminFlightRepository.findByRouteIdAndFlightNumber(routeId,flightNumber).forEach(flightEntity -> {
                ZonedDateTime arrival = flightEntity.getZonedDateTimeArrival();
                ZonedDateTime departure = flightEntity.getZonedDateTimeDeparture();
                if (arrival != null) {
                    arrivalMap.put(arrival,flightEntity);
                }
                if (departure != null) {
                    departureMap.put(departure,flightEntity);
                }
            });
            List<FlightEntity> arrivalsOldestFirst = new ArrayList<>(arrivalMap.values().stream().toList());
            arrivalsOldestFirst.sort(Comparator.comparing(FlightEntity::getZonedDateTimeArrival));
            List<FlightEntity> departuresNewestFirst = new ArrayList<>(departureMap.values().stream().toList());
            departuresNewestFirst.sort(Comparator.comparing(FlightEntity::getZonedDateTimeDeparture).reversed());
            for (ZonedDateTime zonedDateTime : flightUpsert.getZonedDateTimeList()) {
                processDateTime(zonedDateTime, flightUpsert, isArrival, totalCreates, totalUpdates, totalSkips,
                        totalSaves,arrivalMap,departureMap,arrivalsOldestFirst,departuresNewestFirst);
            }
        }

        return handleJPAExceptions(()->{
            adminFlightRepository.saveAll(totalSaves);
            return FlightBatchUpsertResult.builder()
                    .skippedCount(totalSkips.get())
                    .createdCount(totalCreates.get())
                    .updatedCount(totalUpdates.get())
                    .build();
        });
    }

    private void processDateTime(
            ZonedDateTime zonedDateTime, FlightUpsert flightUpsert, boolean isArrival, AtomicInteger totalCreates,
            AtomicInteger totalUpdates, AtomicInteger totalSkips,
            List<FlightEntity> totalSaves, Map<ZonedDateTime, FlightEntity> arrivalMap,
            Map<ZonedDateTime, FlightEntity> departureMap, List<FlightEntity> arrivalsOldestFirst,
            List<FlightEntity> departuresNewestFirst) {
        FlightEntity toSave;
        if (isArrival) {
            FlightEntity existingArrival = arrivalMap.get(zonedDateTime);
            if (existingArrival != null) {
                totalSkips.incrementAndGet();
                return;
            }
            FlightEntity existingDeparture = departuresNewestFirst.stream()
                    .filter(flightEntity ->
                            flightEntity.getZonedDateTimeDeparture().isBefore(zonedDateTime))
                    .findFirst()
                    .orElse(null);
            if (existingDeparture != null && existingDeparture.getZonedDateTimeArrival() == null) {
                toSave = existingDeparture;
                toSave.setZonedDateTimeArrival(zonedDateTime);
                totalUpdates.incrementAndGet();
            } else {
                totalCreates.incrementAndGet();
                toSave = createNewArrivalEntity(flightUpsert, zonedDateTime);
            }
        } else {
            FlightEntity existingDeparture = departureMap.get(zonedDateTime);
            if (existingDeparture != null) {
                totalSkips.incrementAndGet();
                return;
            }
            FlightEntity existingArrival = arrivalsOldestFirst.stream()
                    .filter(flightEntity ->
                            flightEntity.getZonedDateTimeArrival().isAfter(zonedDateTime))
                    .findFirst()
                    .orElse(null);
            if (existingArrival != null && existingArrival.getZonedDateTimeDeparture() == null) {
                toSave = existingArrival;
                toSave.setZonedDateTimeDeparture(zonedDateTime);
                totalUpdates.incrementAndGet();
            } else {
                toSave = createNewDepartureEntity(flightUpsert, zonedDateTime);
                totalCreates.incrementAndGet();
            }
        }
        toSave.setIsActive(toSave.getZonedDateTimeArrival() != null
                && toSave.getZonedDateTimeDeparture() != null);
        totalSaves.add(toSave);
    }

    private FlightEntity createNewArrivalEntity(FlightUpsert flightUpsert, ZonedDateTime zonedDateTime) {
        return FlightEntity.builder()
                .routeId(Integer.valueOf(flightUpsert.getRouteId()))
                .flightNumber(flightUpsert.getFlightNumber())
                .airline(Airline.valueOf(flightUpsert.getAirline().toUpperCase()))
                .zonedDateTimeArrival(zonedDateTime)
                .build();
    }

    private FlightEntity createNewDepartureEntity(FlightUpsert flightUpsert, ZonedDateTime zonedDateTime) {
        return FlightEntity.builder()
                .routeId(Integer.valueOf(flightUpsert.getRouteId()))
                .flightNumber(flightUpsert.getFlightNumber())
                .airline(Airline.valueOf(flightUpsert.getAirline().toUpperCase()))
                .zonedDateTimeDeparture(zonedDateTime)
                .build();
    }
}