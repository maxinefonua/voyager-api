package org.voyager.api.service.impl;

import io.vavr.control.Option;
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
import java.util.List;
import java.util.Optional;

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
    public Option<Flight> getById(Integer id) {
        Optional<FlightEntity> flightEntity = flightRepository.findById(id);
        if (flightEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
    }

    @Override
    public Option<Flight> getFlight(Integer routeId, String flightNumber) {
        Optional<FlightEntity> flightEntity = flightRepository.findByRouteIdAndFlightNumber(routeId,flightNumber);
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
    public PagedResponse<Flight> getPagedFlights(@Validated FlightQuery flightQuery) {
        Page<FlightEntity> pagedFlightEntities;
        if (flightQuery instanceof FlightNumberQuery flightNumberQuery) {
            pagedFlightEntities = fetchPagedFlightEntitiesFrom(flightNumberQuery);
        } else if (flightQuery instanceof FlightAirlineQuery flightAirlineQuery) {
            pagedFlightEntities = fetchPagedFlightEntitiesFrom(flightAirlineQuery);
        } else {
            pagedFlightEntities = fetchPagedFlightEntitiesDefault(flightQuery);
        }
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

    private Page<FlightEntity> fetchPagedFlightEntitiesDefault(FlightQuery flightQuery) {
        List<Integer> routeIdList = flightQuery.getRouteIdList();
        Boolean isActive = flightQuery.getIsActive();
        Pageable pageable = Pageable.ofSize(flightQuery.getPageSize()).withPage(flightQuery.getPage());


        if (routeIdList != null && isActive != null) {
            if (!routeIdList.isEmpty())
                return flightRepository.findByRouteIdInAndIsActive(routeIdList,isActive,pageable);
            return flightRepository.findByIsActive(isActive,pageable);
        }

        if (isActive != null) {
            return flightRepository.findByIsActive(isActive,pageable);
        }
        return flightRepository.findAll(pageable);
    }

    private Page<FlightEntity> fetchPagedFlightEntitiesFrom(FlightAirlineQuery flightAirlineQuery) {
        List<Integer> routeIdList = flightAirlineQuery.getRouteIdList();
        Boolean isActive = flightAirlineQuery.getIsActive();
        Pageable pageable = Pageable.ofSize(flightAirlineQuery.getPageSize()).withPage(flightAirlineQuery.getPage());
        Airline airline = flightAirlineQuery.getAirline();

        if (routeIdList != null && isActive != null) {
            if (!routeIdList.isEmpty())
                return flightRepository.findByRouteIdInAndAirlineAndIsActive(routeIdList,airline,isActive,pageable);
            return flightRepository.findByAirlineAndIsActive(airline,isActive,pageable);
        }

        if (isActive != null) {
            return flightRepository.findByAirlineAndIsActive(airline,isActive,pageable);
        }
        return flightRepository.findByAirline(airline,pageable);
    }

    private Page<FlightEntity> fetchPagedFlightEntitiesFrom(FlightNumberQuery flightNumberQuery) {
        List<Integer> routeIdList = flightNumberQuery.getRouteIdList();
        Boolean isActive = flightNumberQuery.getIsActive();
        Pageable pageable = Pageable.ofSize(flightNumberQuery.getPageSize()).withPage(flightNumberQuery.getPage());
        String flightNumber = flightNumberQuery.getFlightNumber();

        if (routeIdList != null && isActive != null) {
            if (!routeIdList.isEmpty())
                return flightRepository.findByRouteIdInAndFlightNumberAndIsActive(
                        routeIdList,flightNumber,isActive,pageable);
            return flightRepository.findByFlightNumberAndIsActive(flightNumber,isActive,pageable);
        }

        if (isActive != null) {
            return flightRepository.findByFlightNumberAndIsActive(flightNumber,isActive,pageable);
        }
        return flightRepository.findByFlightNumber(flightNumber,pageable);
    }

    @Override
    public Integer batchDelete(FlightBatchDelete flightBatchDelete) {
        return handleJPAExceptions(()-> {
            List<FlightEntity> flightEntityList;
            if (flightBatchDelete.getIsActive() != null && flightBatchDelete.getAirline() != null) {
                flightEntityList = flightRepository.findByAirlineAndIsActive(Airline.valueOf(
                        flightBatchDelete.getAirline()),Boolean.valueOf(flightBatchDelete.getIsActive()));
            } else if (flightBatchDelete.getIsActive() != null) {
                flightEntityList = flightRepository.findByIsActive(Boolean.valueOf(flightBatchDelete.getIsActive()));
            } else if (flightBatchDelete.getAirline() != null) {
                flightEntityList = flightRepository.findByAirline(Airline.valueOf(flightBatchDelete.getAirline()));
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
            flightRepository.deleteAll(flightEntityList);
            return flightEntityList.size();
        });
    }
}
