package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.route.Path;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.AirportsService;
import org.voyager.service.RouteService;
import org.voyager.validate.ValidationUtils;

import java.util.ArrayList;
import java.util.HashSet;
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
    public List<Route> getRoutes(@RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString, @RequestParam(name = ORIGIN_PARAM_NAME, required = false) String origin, @RequestParam(name = DESTINATION_PARAM_NAME, required = false) String destination, @RequestParam(name = IS_ACTIVE_PARAM_NAME, required = false) Boolean isActive) {
        Option<String> originOption = Option.none();
        Option<String> destinationOption = Option.none();
        if (StringUtils.isNotEmpty(origin)) originOption = Option.of(ValidationUtils.validateIataToUpperCase(origin,airportService,ORIGIN_PARAM_NAME,true));
        if (StringUtils.isNotEmpty(destination)) destinationOption = Option.of(ValidationUtils.validateIataToUpperCase(destination,airportService,DESTINATION_PARAM_NAME,true));
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        if (isActive == null) return routeService.getRoutes(originOption,destinationOption,airlineOption);
        return routeService.getActiveRoutes(originOption,destinationOption,airlineOption,isActive);
    }

    @PostMapping("/routes")
    public Route addRoute(@RequestBody(required = false) @Valid RouteForm routeForm, BindingResult bindingResult) {
        ValidationUtils.validateRouteForm(routeForm, bindingResult);
        return routeService.save(routeForm);
    }

    @GetMapping("/routes/{id}")
    public Route getRouteById(@PathVariable(name = "id") String idString) {
        Integer id = ValidationUtils.validateAndGetInteger(ID_PATH_VAR_NAME,idString,false);
        Option<Route> routeOption = routeService.getRouteById(id);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeOption.get();
    }

    @PatchMapping("/routes/{id}")
    public Route patchRouteById(@PathVariable(name = "id") String idString, @RequestBody(required = false) @Valid RoutePatch routePatch, BindingResult bindingResult) {
        ValidationUtils.validateRoutePatch(routePatch,bindingResult);
        Integer id = ValidationUtils.validateAndGetInteger(ID_PATH_VAR_NAME,idString,false);
        Option<Route> routeOption = routeService.getRouteById(id);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeService.patch(routeOption.get(),routePatch);
    }

    @GetMapping("/path/{origin}/to/{destination}")
    public Path getRoutes(@PathVariable(name = ORIGIN_PARAM_NAME) String origin,
                          @PathVariable(name = DESTINATION_PARAM_NAME) String destination,
                          @RequestParam(name = EXCLUDE_PARAM_NAME, required = false) List<String> exclusionList,
                          @RequestParam(name = EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteList) {
        origin = ValidationUtils.validateIataToUpperCase(origin,airportService,ORIGIN_PARAM_NAME,false);
        destination = ValidationUtils.validateIataToUpperCase(destination,airportService,DESTINATION_PARAM_NAME,false);
        Set<String> exclusionSet = Set.of();
        List<Integer> excludeRouteIds = new ArrayList<>();
        if (exclusionList != null) {
            exclusionList.replaceAll(iata -> ValidationUtils.validateIataToUpperCase(iata,airportService,EXCLUDE_PARAM_NAME, true));
            exclusionSet = Set.copyOf(exclusionList);
        }
        if (excludeRouteList != null) {
            excludeRouteList.forEach(routeIdString -> {
                Integer routeId = Integer.parseInt(routeIdString);
                if (!excludeRouteIds.contains(routeId)) excludeRouteIds.add(routeId);
            });
        }
        return routeService.buildPathWithExclusions(origin,destination,exclusionSet,excludeRouteIds);
    }
}
