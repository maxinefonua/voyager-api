package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.location.LocationPatch;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.AirportsService;
import org.voyager.service.RouteService;
import org.voyager.validate.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.voyager.utils.ConstantsUtils.*;
import static org.voyager.utils.ConstantsUtils.ID_PATH_VAR_NAME;


@RestController
public class RoutesController {
    @Autowired
    RouteService routeService;
    @Autowired
    AirportsService airportService;

    @GetMapping("/routes")
    public List<Route> getRoutes(@RequestParam(name = ORIGIN_PARAM_NAME, required = false) String origin,
                                 @RequestParam(name = DESTINATION_PARAM_NAME, required = false) String destination,
                                 @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString) {
        Option<String> originOption = Option.none();
        Option<String> destinationOption = Option.none();
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        if (StringUtils.isNotEmpty(origin)) originOption = Option.of(ValidationUtils.validateIataToUpperCase(origin,airportService,ORIGIN_PARAM_NAME,true));
        if (StringUtils.isNotEmpty(destination)) destinationOption = Option.of(ValidationUtils.validateIataToUpperCase(destination,airportService,DESTINATION_PARAM_NAME,true));
        return routeService.getRoutes(originOption,destinationOption,airlineOption);
    }

    @GetMapping("/route")
    public Route getRoute(@RequestParam(name = ORIGIN_PARAM_NAME) String origin,
                                 @RequestParam(name = DESTINATION_PARAM_NAME) String destination) {
        origin = ValidationUtils.validateIataToUpperCase(origin,airportService,ORIGIN_PARAM_NAME,true);
        destination = ValidationUtils.validateIataToUpperCase(destination,airportService,DESTINATION_PARAM_NAME,true);
        return routeService.getRoute(origin,destination);
    }

    @PostMapping("/routes")
    public Route addRoute(@RequestBody(required = false) @Valid RouteForm routeForm, BindingResult bindingResult) {
        ValidationUtils.validateRouteForm(routeForm, bindingResult);
        return routeService.save(routeForm);
    }

    @GetMapping("/routes/{id}")
    public Route getRoute(@PathVariable(name = ID_PATH_VAR_NAME) String idString) {
        Integer id = ValidationUtils.validateAndGetInteger(ID_PATH_VAR_NAME,idString,false);
        Option<Route> routeOption = routeService.getRouteById(id);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeOption.get();
    }

    @PatchMapping("/routes/{id}")
    public Route patchRouteById(@PathVariable(name = ID_PATH_VAR_NAME) String idString, @RequestBody(required = false) @Valid RoutePatch routePatch, BindingResult bindingResult) {
        Integer id = ValidationUtils.validateAndGetInteger(ID_PATH_VAR_NAME,idString,false);
        ValidationUtils.validateRoutePatch(routePatch,bindingResult);
        Option<Route> routeOption = routeService.getRouteById(id);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeService.patchRoute(id,routePatch);
    }
}
