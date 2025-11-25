package org.voyager.api.service.impl;

import io.vavr.control.Option;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.commons.model.airline.Airline;
import org.voyager.api.model.entity.FlightEntity;
import org.voyager.commons.model.flight.*;
import org.voyager.api.repository.FlightRepository;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.utils.MapperUtils;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import static org.voyager.api.service.utils.ServiceUtils.handleJPAExceptions;

@Service
public class FlightServiceImpl implements FlightService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightServiceImpl.class);

    @Autowired
    FlightRepository flightRepository;

    @Override
    public Boolean existsByFlightNumber(String flightNumber) {
        return flightRepository.existsByFlightNumber(flightNumber);
    }

    @Override
    public Boolean existsByAirlineForEachRouteIdIn(Airline airline, List<Integer> routeIdList) {
        int size = routeIdList.size();
        LOGGER.info("flightRepository.hasMatchingDistinctRouteCount({},{},{}",size,routeIdList,airline);
        return flightRepository.hasMatchingDistinctRouteCount(size,routeIdList,airline);
    }

    @Override
    public Boolean existsByAirlineForEachRouteIdInAndZonedDateTimeDepartureBetween(
            Airline airline, List<Integer> routeIdList, ZonedDateTime start, ZonedDateTime end) {
        int size = routeIdList.size();
        LOGGER.info("flightRepository.existsByAirlineForEachRouteIdInAndZonedDateTimeDepartureBetween({},{},{}",
                size,routeIdList,airline);
        return flightRepository.hasMatchingDistinctRouteCountWithDepartureBetween(size,routeIdList,airline,start,end);
    }

    @Override
    @Cacheable("anyValidPathAirlineCache")
    public Boolean existsByAnyAirlineInForEachRouteIdInAndZonedDateTimeDepartureBetween(
            List<Airline> airlineList, List<Integer> routeIdList, ZonedDateTime start, ZonedDateTime end) {
        int size = routeIdList.size();
        LOGGER.info("flightRepository.existsByAnyAirlineInForEachRouteIdInAndZonedDateTimeDepartureBetween({},{},{}",
                size,routeIdList,airlineList);
        return flightRepository.existsAnyAirlineWithMatchingRouteCount(size,routeIdList,airlineList,start,end);
    }

    @Override
    public Option<Flight> getById(Integer id) {
        Optional<FlightEntity> flightEntity = flightRepository.findById(id);
        if (flightEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
    }

    @Override
    public Option<Flight> getFlightWithArrivalAfter(Integer routeId, String flightNumber, ZonedDateTime departure) {
        Optional<FlightEntity> flightEntity = flightRepository
                .findClosestArrivalAfterDeparture(routeId,flightNumber,departure);
        if (flightEntity.isPresent()) return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
        return Option.none();
    }

    @Override
    public Option<Flight> getFlightWithDepartureBefore(Integer routeId, String flightNumber, ZonedDateTime arrival) {
        Optional<FlightEntity> flightEntity = flightRepository
                .findClosestDepartureBeforeArrival(routeId,flightNumber,arrival);
        if (flightEntity.isPresent()) return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
        return Option.none();
    }

    @Override
    public Option<Flight> getFlight(Integer routeId, String flightNumber, Option<ZonedDateTime> departureBeforeOption,
                                    Option<ZonedDateTime> arrivalAfterOption) {
        Optional<FlightEntity> flightEntity = Optional.empty();
        if (departureBeforeOption.isDefined() && arrivalAfterOption.isDefined()) {
            flightEntity = flightRepository.findByRouteIdAndFlightNumberAndZonedDateTimeDepartureAndZonedDateTimeArrival(
                    routeId,flightNumber, departureBeforeOption.get(), arrivalAfterOption.get());
        } else if (departureBeforeOption.isDefined()) {
            flightEntity = flightRepository.findByRouteIdAndFlightNumberAndZonedDateTimeDeparture(
                    routeId,flightNumber, departureBeforeOption.get());
        } else if (arrivalAfterOption.isDefined()) {
            flightEntity = flightRepository.findByRouteIdAndFlightNumberAndZonedDateTimeArrival(
                    routeId,flightNumber, arrivalAfterOption.get());
        }
        if (flightEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
    }

    @Override
    @Cacheable("getAirlineRouteIds")
    public List<Integer> getAirlineRouteIds(Airline airline) {
        return flightRepository.selectDistinctRouteIdByAirlineAndIsActive(airline,true);
    }

    @Override
    public Flight save(FlightForm flightForm) {
        return handleJPAExceptions(()-> MapperUtils
                .entityToFlight(flightRepository.save(MapperUtils.formToFlightEntity(flightForm))));
    }

    @Override
    public Flight patch(Flight flight, FlightPatch flightPatch) {
        return handleJPAExceptions(()-> {
            FlightEntity patched = MapperUtils.patchToFlightEntity(flight, flightPatch);
            try {
                patched = flightRepository.save(patched);
            } catch (Exception e) {
                LOGGER.error(e.getMessage()); // TODO: implement alerting
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.buildRespositoryPatchErrorMessage("flight", flight.getFlightNumber()),
                        e);
            }
            return MapperUtils.entityToFlight(patched);
        });
    }

    @Override
    public List<Flight> getFlights(List<Integer> routeIdList,
                                   Option<String> flightNumberOption,
                                   Option<Airline> airlineOption,
                                   Option<Boolean> isActiveOption) {
        if (routeIdList.isEmpty() && flightNumberOption.isEmpty() && airlineOption.isEmpty()
                && isActiveOption.isEmpty())
            return flightRepository.findAll().stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdList.isEmpty() && flightNumberOption.isEmpty() && airlineOption.isEmpty())
            return flightRepository.findByIsActive(isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdList.isEmpty() && flightNumberOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByAirline(airlineOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdList.isEmpty() && airlineOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByFlightNumber(flightNumberOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (airlineOption.isEmpty() && flightNumberOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByRouteIdIn(routeIdList)
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdList.isEmpty() && flightNumberOption.isEmpty())
            return flightRepository.findByAirlineAndIsActive(airlineOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdList.isEmpty() && airlineOption.isEmpty())
            return flightRepository.findByFlightNumberAndIsActive(flightNumberOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (airlineOption.isEmpty() && flightNumberOption.isEmpty())
            return flightRepository.findByRouteIdInAndIsActive(routeIdList,isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (airlineOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByRouteIdInAndFlightNumber(routeIdList,flightNumberOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (flightNumberOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByRouteIdInAndAirline(routeIdList,airlineOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (flightNumberOption.isEmpty())
            return flightRepository.findByRouteIdInAndAirlineAndIsActive(
                            routeIdList,airlineOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        return flightRepository.findByRouteIdInAndFlightNumberAndIsActive(
                routeIdList,flightNumberOption.get(),isActiveOption.get())
                .stream().map(MapperUtils::entityToFlight).toList();
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
        return PagedResponse.<Flight>builder()
                .content(pagedFlightEntities.get().map(MapperUtils::entityToFlight).toList())
                .page(flightQuery.getPage())
                .size(flightQuery.getPageSize())
                .totalElements(pagedFlightEntities.getTotalElements())
                .totalPages(pagedFlightEntities.getTotalPages())
                .first(pagedFlightEntities.isFirst())
                .last(pagedFlightEntities.isLast())
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
    @Transactional
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
        List<FlightEntity> totalSaves = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger totalCreates = new AtomicInteger(0);
        AtomicInteger totalUpdates = new AtomicInteger(0);
        AtomicInteger totalSkips = new AtomicInteger(0);

        // Process all FlightUpsert objects in parallel
        flightUpsertList.parallelStream().forEach(flightUpsert -> {
            Integer routeId = Integer.parseInt(flightUpsert.getRouteId());
            boolean isArrival = Boolean.parseBoolean(flightUpsert.getIsArrival().toLowerCase());
            Airline airline = Airline.valueOf(flightUpsert.getAirline().toUpperCase());
            String flightNumber = flightUpsert.getFlightNumber();

            // Process date times sequentially within each FlightUpsert to avoid nested parallelism
            flightUpsert.getZonedDateTimeList().forEach(zonedDateTime ->
                    processDateTime(zonedDateTime, flightUpsert, isArrival, routeId, flightNumber,
                            airline, totalCreates, totalUpdates, totalSkips, totalSaves));
        });

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
            Optional<FlightEntity> existingFlightEntity = flightRepository
                    .findClosestDepartureBeforeArrival(routeId, flightNumber, zonedDateTime);
            if (existingFlightEntity.isPresent()) {
                flightEntity = existingFlightEntity.get();
                if (flightEntity.getZonedDateTimeArrival() != null) {
                    totalSkips.getAndIncrement();
                    return;
                }
                flightEntity.setZonedDateTimeArrival(zonedDateTime);
                totalUpdates.incrementAndGet();
            } else {
                Optional<FlightEntity> existingArrival = flightRepository
                        .findByRouteIdAndFlightNumberAndZonedDateTimeArrival(routeId,flightNumber,zonedDateTime);
                if (existingArrival.isPresent()) {
                    return;
                }
                flightEntity = createNewArrivalEntity(routeId, flightUpsert, airline, zonedDateTime);
                totalCreates.incrementAndGet();
            }
        } else {
            Optional<FlightEntity> existingFlightEntity = flightRepository
                    .findClosestArrivalAfterDeparture(routeId, flightNumber, zonedDateTime);
            if (existingFlightEntity.isPresent()) {
                flightEntity = existingFlightEntity.get();
                if (flightEntity.getZonedDateTimeDeparture() != null) {
                    totalSkips.getAndIncrement();
                    return;
                }
                flightEntity.setZonedDateTimeDeparture(zonedDateTime);
                totalUpdates.incrementAndGet();
            } else {
                Optional<FlightEntity> existingDeparture = flightRepository
                        .findByRouteIdAndFlightNumberAndZonedDateTimeDeparture(routeId,flightNumber,zonedDateTime);
                if (existingDeparture.isPresent()) {
                    return;
                }
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
