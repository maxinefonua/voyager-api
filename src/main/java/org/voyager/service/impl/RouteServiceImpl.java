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
import org.voyager.model.entity.Delta;
import org.voyager.model.entity.Location;
import org.voyager.model.entity.Route;
import org.voyager.model.entity.Status;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
import org.voyager.repository.DeltaRepository;
import org.voyager.repository.RouteRepository;
import org.voyager.service.RouteService;
import org.voyager.service.utils.MapperUtils;

import java.util.List;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {
    @Autowired
    RouteRepository routeRepository;

    @Autowired
    DeltaRepository deltaRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteServiceImpl.class);

    @Override
    public Option<RouteDisplay> getRouteById(Integer id) {
        Optional<Route> route = routeRepository.findById(id);
        if (route.isEmpty()) return Option.none();
        return Option.of(MapperUtils.routeToDisplay(route.get()));
    }

    @Override
    public Option<RouteDisplay> getByOriginAndDestinationAndAirline(String origin, String destination, Airline airline) {
        Optional<Route> route = routeRepository.findByOriginAndDestinationAndAirline(origin,destination,airline);
        if (route.isEmpty()) return Option.none();
        return Option.of(MapperUtils.routeToDisplay(route.get()));
    }

    @Override
    public List<RouteDisplay> getRoutes(Option<String> origin, Option<String> destination, Option<Airline> airline) {
        if (origin.isEmpty() && destination.isEmpty() && airline.isEmpty()) return routeRepository.findAll().stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty() && destination.isEmpty()) return routeRepository.findByAirline(airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty() && airline.isEmpty()) return routeRepository.findByDestination(destination.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (destination.isEmpty() && airline.isEmpty()) return routeRepository.findByOrigin(origin.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (airline.isEmpty()) return routeRepository.findByOriginAndDestination(origin.get(), destination.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (origin.isEmpty()) return routeRepository.findByDestinationAndAirline(destination.get(),airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
        if (destination.isEmpty()) return routeRepository.findByOriginAndAirline(origin.get(),airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
        return routeRepository.findByOriginAndDestinationAndAirline(origin.get(), destination.get(),airline.get()).stream().map(MapperUtils::routeToDisplay).toList();
    }

    @Override
    public RouteDisplay save(RouteForm routeForm) {
        Route route = MapperUtils.formToRoute(routeForm);
        try {
            route = routeRepository.save(route);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.buildRespositorySaveErrorMessage("route"));
        }
        return MapperUtils.routeToDisplay(route);
    }
}
