package org.voyager.api.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.model.path.FlightDetailed;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.*;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.RouteService;
import org.voyager.api.validate.ValidationUtils;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
public class FlightsController {
    @Autowired
    FlightService flightService;
    @Autowired
    RouteService routeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightsController.class);

    @GetMapping(Path.FLIGHTS)
    public PagedResponse<Flight> getFlights(@RequestParam(required = false,name = ParameterNames.ROUTE_ID_PARAM_NAME)
                                                List<String> routeIdStringList,
                                            @RequestParam(required = false, name = ParameterNames.FLIGHT_NUMBER_PARAM_NAME)
                                                String flightNumberString,
                                            @RequestParam(required = false, name = ParameterNames.AIRLINE_PARAM_NAME)
                                                List<String> airlineStringList,
                                            @RequestParam(name = ParameterNames.IS_ACTIVE_PARAM_NAME,
                                                    defaultValue = "true")
                                                String isActiveString,
                                            @RequestParam(name = ParameterNames.START) String startString,
                                            @RequestParam(name = ParameterNames.END) String endString,
                                            @RequestParam(name = ParameterNames.PAGE,
                                                    defaultValue = "0")
                                                String pageString,
                                            @RequestParam(name = ParameterNames.PAGE_SIZE,
                                                    defaultValue = "100")
                                                String pageSizeString) {
        LOGGER.info(String.format("GET /flights with routeIdStringList: '%s', flightNumberString: '%s', " +
                "airlineStringList: '%s', isActiveString: '%s'", routeIdStringList, flightNumberString,
                airlineStringList, isActiveString));

        boolean hasAirlineParam = airlineStringList != null;
        boolean hasFlightNumberParam = flightNumberString != null;

        if (hasAirlineParam && hasFlightNumberParam) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Cannot specifiy both %s and %s paraemeters. " +
                                    "Use %s for airline flights OR %s for flights with given flight number",
                            ParameterNames.AIRLINE_PARAM_NAME, ParameterNames.FLIGHT_NUMBER_PARAM_NAME,
                            ParameterNames.AIRLINE_PARAM_NAME, ParameterNames.FLIGHT_NUMBER_PARAM_NAME));
        }



        List<Integer> routeIdList = null;
        if (routeIdStringList != null && !routeIdStringList.isEmpty()) {
            routeIdList = routeIdStringList.stream().map(routeIdString ->
                    ValidationUtils.validateAndGetRouteId(routeIdString,routeService)).toList();
        }

        ZonedDateTime startTime = ValidationUtils.validateAndGetZDT(startString);
        ZonedDateTime endTime = ValidationUtils.validateAndGetZDT(endString);
        Boolean isActive = ValidationUtils.validateAndGetBoolean(ParameterNames.IS_ACTIVE_PARAM_NAME,isActiveString);
        int page = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE,pageString);
        int pageSize = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE_SIZE,pageSizeString);

        if (hasAirlineParam && !airlineStringList.isEmpty()) {
            List<Airline> airlineList = airlineStringList.stream()
                    .map(ValidationUtils::validateAndGetAirline).toList();

            FlightAirlineQuery flightAirlineQuery = FlightAirlineQuery.builder().page(page).pageSize(pageSize)
                    .startTime(startTime).endTime(endTime)
                    .airlineList(airlineList).routeIdList(routeIdList).isActive(isActive).build();
            return flightService.getPagedFlights(flightAirlineQuery);
        }

        if (hasFlightNumberParam && StringUtils.isNotBlank(flightNumberString)) {
            String flightNumber = ValidationUtils.validateAndGetFlightNumber(flightNumberString,flightService);

            FlightNumberQuery flightNumberQuery = FlightNumberQuery.builder().page(page).pageSize(pageSize)
                    .startTime(startTime).endTime(endTime)
                    .flightNumber(flightNumber).routeIdList(routeIdList).isActive(isActive).build();
            return flightService.getPagedFlights(flightNumberQuery);
        }

        FlightQuery flightQuery = FlightQuery.builder().page(page).pageSize(pageSize)
                .startTime(startTime).endTime(endTime)
                .routeIdList(routeIdList).isActive(isActive).build();
        return flightService.getPagedFlights(flightQuery);
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
                            @RequestParam(name = ParameterNames.FLIGHT_NUMBER_PARAM_NAME) String flightNumber,
                            @RequestParam(name = ParameterNames.DEPARTURE_BEFORE, required = false) String departureBeforeString,
                            @RequestParam(name = ParameterNames.ARRIVAL_AFTER, required = false) String arrivalAfterString) {
        LOGGER.info("GET /flight with routeId: {}, flightNumber: {}, departureBeforeString: {}, arrivalAfterString: {}",
                                routeIdString, flightNumber,departureBeforeString,arrivalAfterString);
        Integer routeId = ValidationUtils.validateAndGetRouteId(routeIdString,routeService);
        ZonedDateTime arrival = null;
        ZonedDateTime departure = null;
        if (StringUtils.isNotBlank(departureBeforeString)) {
            arrival = ValidationUtils.validateAndGetZDT(departureBeforeString);
        }
        if (StringUtils.isNotBlank(arrivalAfterString)) {
            departure = ValidationUtils.validateAndGetZDT(arrivalAfterString);
        }
        if ((arrival == null && departure == null) || (arrival != null && departure != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("requires either '%s' or '%s' parameter, but not both, to return a single flight match",
                            ParameterNames.DEPARTURE_BEFORE,ParameterNames.ARRIVAL_AFTER));
        }
        Option<Flight> flightOption = Option.none();
        if (arrival != null) {
            flightOption = flightService.getFlightWithDepartureBefore(routeId,flightNumber,arrival);
        }
        if (departure != null) {
            flightOption = flightService.getFlightWithArrivalAfter(routeId,flightNumber,departure);
        }
        // TODO: update not found error message
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForMultiParameterMessage(ParameterNames.ROUTE_ID_PARAM_NAME,routeIdString,
                        ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        return flightOption.get();
    }

    @PatchMapping(Path.Admin.FLIGHT_BY_ID)
    public Flight patchFlight(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) Integer id,
                              @Valid @RequestBody(required = false) FlightPatch flightPatch,
                              BindingResult bindingResult) {
        ValidationUtils.validateFlightPatch(flightPatch, bindingResult);
        Option<Flight> flightOption = flightService.getById(id);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return flightService.patch(flightOption.get(),flightPatch);
    }

    @PostMapping(Path.Admin.FLIGHTS)
    public FlightBatchUpsertResult upsertFlights(@Valid @RequestBody(required = false) FlightBatchUpsert flightBatchUpsert,
                                      BindingResult bindingResult) {
        ValidationUtils.validate(flightBatchUpsert,bindingResult);
        return flightService.batchUpsert(flightBatchUpsert);
    }

    @DeleteMapping(Path.Admin.FLIGHTS)
    public Integer batchDeleteFlights(@Valid @RequestBody(required = false) FlightBatchDelete flightBatchDelete,
                                      BindingResult bindingResult) {
        ValidationUtils.validate(flightBatchDelete,bindingResult);
        return flightService.batchDelete(flightBatchDelete);
    }
}