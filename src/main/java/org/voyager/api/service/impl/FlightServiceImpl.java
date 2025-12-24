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
import org.voyager.api.repository.FlightRepository;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.utils.MapperUtils;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class FlightServiceImpl implements FlightService {
    @Autowired
    FlightRepository flightRepository;

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
    @Transactional(timeout = 30)
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
    public FlightBatchUpsertResult batchUpsert(@Validated FlightBatchUpsert flightBatchUpsert) {
        List<FlightUpsert> flightUpsertList = flightBatchUpsert.getFlightUpsertList();
        AtomicInteger totalCreates = new AtomicInteger(0);
        AtomicInteger totalUpdates = new AtomicInteger(0);
        AtomicInteger totalSkips = new AtomicInteger(0);
        List<FlightEntity> totalSaves = new ArrayList<>();

        for (FlightUpsert flightUpsert : flightUpsertList) {
            Integer routeId = Integer.parseInt(flightUpsert.getRouteId());
            boolean isArrival = Boolean.parseBoolean(flightUpsert.getIsArrival().toLowerCase());
            Airline airline = Airline.valueOf(flightUpsert.getAirline().toUpperCase());
            String flightNumber = flightUpsert.getFlightNumber();

            for (ZonedDateTime zonedDateTime : flightUpsert.getZonedDateTimeList()) {
                processDateTime(zonedDateTime, flightUpsert, isArrival, routeId,
                        flightNumber, airline, totalCreates, totalUpdates, totalSkips, totalSaves);
            }
        }

        flightRepository.saveAll(totalSaves);

        return FlightBatchUpsertResult.builder()
                .skippedCount(totalSkips.get())
                .createdCount(totalCreates.get())
                .updatedCount(totalUpdates.get())
                .build();
    }

    private void processDateTime(ZonedDateTime zonedDateTime, FlightUpsert flightUpsert,
                                 boolean isArrival, Integer routeId, String flightNumber,
                                 Airline airline, AtomicInteger totalCreates,
                                 AtomicInteger totalUpdates, AtomicInteger totalSkips,
                                 List<FlightEntity> totalSaves) {

        FlightEntity flightEntity;
        if (isArrival) {
            Optional<FlightEntity> existingArrival = flightRepository
                    .findByRouteIdAndFlightNumberAndZonedDateTimeArrival(routeId,flightNumber,zonedDateTime);
            if (existingArrival.isPresent()) {
                totalSkips.incrementAndGet();
                return;
            }
            Optional<FlightEntity> existingFlightEntity = flightRepository
                    .findClosestDepartureBeforeArrival(routeId, flightNumber, zonedDateTime);
            if (existingFlightEntity.isPresent() && existingFlightEntity.get().getZonedDateTimeArrival() == null) {
                flightEntity = existingFlightEntity.get();
                flightEntity.setZonedDateTimeArrival(zonedDateTime);
                totalUpdates.incrementAndGet();
            } else {
                flightEntity = createNewArrivalEntity(routeId, flightUpsert, airline, zonedDateTime);
                totalCreates.incrementAndGet();
            }
        } else {
            Optional<FlightEntity> existingDeparture = flightRepository
                    .findByRouteIdAndFlightNumberAndZonedDateTimeDeparture(routeId,flightNumber,zonedDateTime);
            if (existingDeparture.isPresent()) {
                totalSkips.incrementAndGet();
                return;
            }
            Optional<FlightEntity> existingFlightEntity = flightRepository
                    .findClosestArrivalAfterDeparture(routeId, flightNumber, zonedDateTime);
            if (existingFlightEntity.isPresent() && existingFlightEntity.get().getZonedDateTimeDeparture() == null) {
                flightEntity = existingFlightEntity.get();
                flightEntity.setZonedDateTimeDeparture(zonedDateTime);
                totalUpdates.incrementAndGet();
            } else {
                flightEntity = createNewDepartureEntity(routeId, flightUpsert, airline, zonedDateTime);
                totalCreates.incrementAndGet();
            }
        }
        flightEntity.setIsActive(flightEntity.getZonedDateTimeArrival() != null
                && flightEntity.getZonedDateTimeDeparture() != null);
        totalSaves.add(flightEntity);
    }

    private FlightEntity createNewArrivalEntity(Integer routeId, FlightUpsert flightUpsert,
                                                Airline airline, ZonedDateTime zonedDateTime) {
        return FlightEntity.builder()
                .routeId(routeId)
                .flightNumber(flightUpsert.getFlightNumber())
                .airline(airline)
                .zonedDateTimeArrival(zonedDateTime)
                .build();
    }

    private FlightEntity createNewDepartureEntity(Integer routeId, FlightUpsert flightUpsert,
                                                  Airline airline, ZonedDateTime zonedDateTime) {
        return FlightEntity.builder()
                .routeId(routeId)
                .flightNumber(flightUpsert.getFlightNumber())
                .airline(airline)
                .zonedDateTimeDeparture(zonedDateTime)
                .build();
    }

}
