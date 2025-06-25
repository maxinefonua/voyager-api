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
    public List<Flight> getFlights(Airline airline) {
        return flightRepository.findByAirline(airline)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(Boolean isActive) {
        return flightRepository.findByIsActive(isActive)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(Integer routeId) {
        return flightRepository.findByRouteId(routeId)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(Integer routeId, String flightNumber) {
        return flightRepository.findByRouteIdAndFlightNumber(routeId,flightNumber)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(Integer routeId, Boolean isActive) {
        return flightRepository.findByRouteIdAndIsActive(routeId,isActive)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(Integer routeId, String flightNumber, Boolean isActive) {
        return flightRepository.findByRouteIdAndFlightNumberAndIsActive(routeId,flightNumber,isActive)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getFlights(String flightNumber, Boolean isActive) {
        return flightRepository.findByFlightNumberAndIsActive(flightNumber,isActive)
                .stream().map(MapperUtils::entityToFlight).toList();
    }

    @Override
    public List<Flight> getAll() {
        return flightRepository.findAll().stream().map(MapperUtils::entityToFlight).toList();
    }

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
}
