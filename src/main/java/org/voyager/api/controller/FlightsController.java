package org.voyager.api.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightPatch;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.RouteService;
import org.voyager.api.validate.ValidationUtils;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FlightsController {
    @Autowired
    FlightService flightService;
    @Autowired
    RouteService routeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightsController.class);

    // TODO: add pagination
    @GetMapping(Path.FLIGHTS)
    public List<Flight> getFlights(@RequestParam(required = false,name = ParameterNames.ROUTE_ID_PARAM_NAME) List<String> routeIdStringList,
                                   @RequestParam(required = false, name = ParameterNames.FLIGHT_NUMBER_PARAM_NAME) String flightNumberString,
                                   @RequestParam(required = false, name = ParameterNames.AIRLINE_PARAM_NAME) String airlineString,
                                   @RequestParam(name = ParameterNames.IS_ACTIVE_PARAM_NAME,defaultValue = "true") Boolean isActiveString) {
        LOGGER.info(String.format("GET /flights with routeIdStringList: '%s', flightNumberString: '%s', " +
                "airlineString: '%s', isActiveString: '%s'", routeIdStringList, flightNumberString,
                airlineString, isActiveString));
        List<Integer> routeIdList = new ArrayList<>();
        if (routeIdStringList != null) {
            routeIdStringList.forEach(routeIdString ->
                    routeIdList.add(ValidationUtils.resolveRouteId(routeIdString,routeService))
            );
        }
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        Option<String> flightNumberOption = Option.of(flightNumberString);
        Option<Boolean> isActiveOption = Option.of(isActiveString);
        List<Flight> response = flightService.getFlights(routeIdList,flightNumberOption,airlineOption,isActiveOption);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.FLIGHT_BY_ID)
    public Flight getFlight(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) Integer id) {
        Option<Flight> flightOption = flightService.getById(id);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return flightOption.get();
    }

    @GetMapping(Path.FLIGHT)
    public Flight getFlight(@RequestParam(name = ParameterNames.ROUTE_ID_PARAM_NAME) String routeIdString,
                            @RequestParam(name = ParameterNames.FLIGHT_NUMBER_PARAM_NAME) String flightNumber) {
        LOGGER.info(String.format("GET /flight with routeId '%s', flightNumber '%s'",
                                routeIdString, flightNumber));
        Integer routeId = ValidationUtils.resolveRouteId(routeIdString,routeService);
        Option<Flight> flightOption = flightService.getFlight(routeId,flightNumber);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForMultiParameterMessage(ParameterNames.ROUTE_ID_PARAM_NAME,routeIdString,
                        ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        return flightOption.get();
    }

    @PatchMapping(Path.Admin.FLIGHT_BY_ID)
    public Flight addFlight(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) Integer id,
                            @Valid @RequestBody(required = false) FlightPatch flightPatch,
                            BindingResult bindingResult) {
        ValidationUtils.validateFlightPatch(flightPatch, bindingResult);
        Option<Flight> flightOption = flightService.getById(id);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return flightService.patch(flightOption.get(),flightPatch);
    }

    @PostMapping(Path.Admin.FLIGHTS)
    public Flight addFlight(@Valid @RequestBody(required = false) FlightForm flightForm,
                            BindingResult bindingResult) {
        ValidationUtils.validateFlightForm(flightForm, bindingResult);
        return flightService.save(flightForm);
    }

    @DeleteMapping(Path.Admin.FLIGHTS)
    public Integer batchDeleteFlights(@Valid @RequestBody(required = false) FlightBatchDelete flightBatchDelete,
                                      BindingResult bindingResult) {
        ValidationUtils.validate(flightBatchDelete,bindingResult);
        return flightService.batchDelete(flightBatchDelete);
    }
}