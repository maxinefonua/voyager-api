package org.voyager.api.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.RouteService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.model.route.RouteQuery;

import java.util.List;

@RestController
public class RoutesController {
    @Autowired
    RouteService routeService;
    @Autowired
    AirportsService airportService;

    @GetMapping(Path.ROUTES)
    public List<Route> getRoutes(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME, required = false) List<String> originList,
                                 @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME, required = false) List<String> destinationList) {
        RouteQuery routeQuery = null;
        if (originList != null && !originList.isEmpty()) {
            originList = originList.stream().map(origin->
                    ValidationUtils.validateIataToUpperCase(origin,airportService,
                            ParameterNames.ORIGIN_PARAM_NAME,true)).toList();
            routeQuery = RouteQuery.builder().originList(originList).build();
        }
        if (destinationList != null && !destinationList.isEmpty()) {
            destinationList = destinationList.stream().map(destination->
                    ValidationUtils.validateIataToUpperCase(destination,airportService,
                            ParameterNames.DESTINATION_PARAM_NAME,true)).toList();
            if (routeQuery == null) routeQuery = RouteQuery.builder().build();
            routeQuery.setDestinationList(destinationList);
        }
        if (routeQuery == null) return routeService.getRoutes();
        return routeService.getRoutes(routeQuery);
    }

    @GetMapping(Path.ROUTE)
    public Route getRoute(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME) String origin,
                                 @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME) String destination) {
        origin = ValidationUtils.validateIataToUpperCase(origin,airportService,ParameterNames.ORIGIN_PARAM_NAME,true);
        destination = ValidationUtils.validateIataToUpperCase(destination,airportService,ParameterNames.DESTINATION_PARAM_NAME,true);
        Option<Route> routeOption = routeService.getRoute(origin,destination);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForMultiParameterMessage(ParameterNames.ORIGIN_PARAM_NAME,origin,
                        ParameterNames.DESTINATION_PARAM_NAME,destination));
        return routeOption.get();
    }

    @PostMapping(Path.ROUTES)
    public Route addRoute(@RequestBody(required = false) @Valid RouteForm routeForm, BindingResult bindingResult) {
        ValidationUtils.validateRouteForm(routeForm, bindingResult);
        return routeService.save(routeForm);
    }

    @GetMapping("/routes/{id}")
    public Route getRoute(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) String idString) {
        Integer id = ValidationUtils.validateAndGetInteger(ParameterNames.ID_PATH_VAR_NAME,idString,false);
        Option<Route> routeOption = routeService.getRouteById(id);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeOption.get();
    }

    @PatchMapping(Path.Admin.ROUTE_BY_ID)
    public Route patchRouteById(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) String idString, @RequestBody(required = false) @Valid RoutePatch routePatch, BindingResult bindingResult) {
        Integer id = ValidationUtils.validateAndGetInteger(ParameterNames.ID_PATH_VAR_NAME,idString,false);
        ValidationUtils.validateRoutePatch(routePatch,bindingResult);
        Option<Route> routeOption = routeService.getRouteById(id);
        if (routeOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeService.patchRoute(id,routePatch);
    }
}
