package org.voyager.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.entity.FlightEntity;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.repository.FlightRepository;
import org.voyager.service.FlightService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;
import java.util.Optional;

@Service
public class FlightServiceImpl implements FlightService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightServiceImpl.class);

    @Autowired
    FlightRepository flightRepository;

    @Override
    public Option<Flight> getById(Integer id) {
        Optional<FlightEntity> flightEntity = flightRepository.findById(id);
        if (flightEntity.isEmpty()) return Option.none();
        return Option.of(MapperUtils.entityToFlight(flightEntity.get()));
    }

    @Override
    public Flight save(FlightForm flightForm) {
        return MapperUtils.entityToFlight(flightRepository.save(MapperUtils.formToFlightEntity(flightForm)));
    }

    @Override
    public Flight patch(Flight flight, FlightPatch flightPatch) {
        FlightEntity patched = MapperUtils.patchToFlightEntity(flight,flightPatch);
        try {
            patched = flightRepository.save(patched);
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: implement alerting
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.buildRespositoryPatchErrorMessage("flight",flight.getFlightNumber()),
                    e);
        }
        return MapperUtils.entityToFlight(patched);
    }

    @Override
    public List<Flight> getFlights(Option<Integer> routeIdOption,
                                   Option<String> flightNumberOption,
                                   Option<Airline> airlineOption,
                                   Option<Boolean> isActiveOption) {
        if (routeIdOption.isEmpty() && flightNumberOption.isEmpty() && airlineOption.isEmpty()
                && isActiveOption.isEmpty())
            return flightRepository.findAll().stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdOption.isEmpty() && flightNumberOption.isEmpty() && airlineOption.isEmpty())
            return flightRepository.findByIsActive(isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdOption.isEmpty() && flightNumberOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByAirline(airlineOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdOption.isEmpty() && airlineOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByFlightNumber(flightNumberOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (airlineOption.isEmpty() && flightNumberOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByRouteId(routeIdOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdOption.isEmpty() && flightNumberOption.isEmpty())
            return flightRepository.findByAirlineAndIsActive(airlineOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (routeIdOption.isEmpty() && airlineOption.isEmpty())
            return flightRepository.findByFlightNumberAndIsActive(flightNumberOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (airlineOption.isEmpty() && flightNumberOption.isEmpty())
            return flightRepository.findByRouteIdAndIsActive(routeIdOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (airlineOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByRouteIdAndFlightNumber(routeIdOption.get(),flightNumberOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (flightNumberOption.isEmpty() && isActiveOption.isEmpty())
            return flightRepository.findByRouteIdAndAirline(routeIdOption.get(),airlineOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        if (flightNumberOption.isEmpty())
            return flightRepository.findByRouteIdAndAirlineAndIsActive(
                            routeIdOption.get(),airlineOption.get(),isActiveOption.get())
                    .stream().map(MapperUtils::entityToFlight).toList();

        return flightRepository.findByRouteIdAndFlightNumberAndIsActive(
                routeIdOption.get(),flightNumberOption.get(),isActiveOption.get())
                .stream().map(MapperUtils::entityToFlight).toList();
    }
}
