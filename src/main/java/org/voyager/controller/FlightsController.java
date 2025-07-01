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
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.service.FlightService;
import org.voyager.service.RouteService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

@RestController
public class FlightsController {
    @Autowired
    FlightService flightService;
    @Autowired
    RouteService routeService;

    @GetMapping("/flights")
    public List<Flight> getFlights(@RequestParam(required = false,name = ROUTE_ID_PARAM_NAME) String routeIdString,
                                   @RequestParam(required = false, name = FLIGHT_NUMBER_PARAM_NAME) String flightNumberString,
                                   @RequestParam(required = false, name = AIRLINE_PARAM_NAME) String airlineString,
                                   @RequestParam(required = false,name = IS_ACTIVE_PARAM_NAME) Boolean isActiveString) {
        Option<Integer> routeIdOption = ValidationUtils.resolveRouteId(routeIdString,routeService);
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        Option<String> flightNumberOption = Option.of(flightNumberString);
        Option<Boolean> isActiveOption = Option.of(isActiveString);
        return flightService.getFlights(routeIdOption,flightNumberOption,airlineOption,isActiveOption);
    }

    @PatchMapping("/flights/{id}")
    public Flight addFlight(@PathVariable(name = ID_PATH_VAR_NAME) Integer id,
                            @Valid @RequestBody(required = false) FlightPatch flightPatch,
                            BindingResult bindingResult) {
        ValidationUtils.validateFlightPatch(flightPatch, bindingResult);
        Option<Flight> flightOption = flightService.getById(id);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return flightService.patch(flightOption.get(),flightPatch);
    }

    @GetMapping("/flights/{id}")
    public Flight getFlight(@PathVariable(name = ID_PATH_VAR_NAME) Integer id) {
        Option<Flight> flightOption = flightService.getById(id);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return flightOption.get();
    }

    @PostMapping("/flights")
    public Flight addFlight(@Valid @RequestBody(required = false) FlightForm flightForm,
                            BindingResult bindingResult) {
        ValidationUtils.validateFlightForm(flightForm, bindingResult);
        return flightService.save(flightForm);
    }
}
