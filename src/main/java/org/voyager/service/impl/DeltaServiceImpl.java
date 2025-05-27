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
import org.voyager.model.delta.Delta;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.entity.DeltaEntity;
import org.voyager.model.route.Route;
import org.voyager.repository.DeltaRepository;
import org.voyager.service.DeltaService;
import org.voyager.service.RouteService;
import org.voyager.service.utils.MapperUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeltaServiceImpl implements DeltaService {
    @Autowired
    DeltaRepository deltaRepository;

    @Autowired
    RouteService routeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaServiceImpl.class);

    @Override
    public List<String> getActiveCodes() {
        Set<String> originIata = routeService.getActiveRoutes(Option.none(),Option.none(),Option.of(Airline.DELTA),
                        true).stream().map(Route::getOrigin).collect(Collectors.toSet());
        return originIata.stream().toList();
    }

    @Override
    public Boolean exists(String iata) {
        Set<String> originIata = routeService.getActiveRoutes(Option.none(),Option.none(),Option.of(Airline.DELTA),
                        true).stream().map(Route::getOrigin).collect(Collectors.toSet());
        return originIata.contains(iata);
    }
}
