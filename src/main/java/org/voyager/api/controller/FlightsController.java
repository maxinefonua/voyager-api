package org.voyager.api.controller;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.FlightNumberQuery;
import org.voyager.commons.model.flight.FlightQuery;
import org.voyager.commons.model.flight.FlightAirlineQuery;
import org.voyager.commons.model.flight.Flight;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.RouteService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.model.response.PagedResponse;

import java.time.LocalDate;
import java.time.ZoneId;
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
    public PagedResponse<Flight> getFlights(
            @RequestParam(required = false,name = ParameterNames.ROUTE_ID_PARAM_NAME)
                List<String> routeIdStringList,
            @RequestParam(required = false, name = ParameterNames.FLIGHT_NUMBER_PARAM_NAME)
                String flightNumberString,
            @RequestParam(required = false, name = ParameterNames.AIRLINE_PARAM_NAME)
                List<String> airlineStringList,
            @RequestParam(name = ParameterNames.IS_ACTIVE_PARAM_NAME, defaultValue = "true")
                String isActiveString,
            @RequestParam(name = ParameterNames.START, defaultValue = "now")
                String startString,
            @RequestParam(name = ParameterNames.END, defaultValue = "+1day")
                String endString,
            @RequestParam(name = ParameterNames.PAGE, defaultValue = "0")
                String pageString,
            @RequestParam(name = ParameterNames.SIZE, defaultValue = "100")
                String pageSizeString) {
        LOGGER.info("GET {} with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}", Path.FLIGHTS,
                ParameterNames.ROUTE_ID_PARAM_NAME, routeIdStringList,
                ParameterNames.FLIGHT_NUMBER_PARAM_NAME, flightNumberString,
                ParameterNames.AIRLINE_PARAM_NAME, airlineStringList,
                ParameterNames.IS_ACTIVE_PARAM_NAME,isActiveString,
                ParameterNames.START,startString, ParameterNames.END,endString,
                ParameterNames.PAGE,pageString,ParameterNames.SIZE,pageSizeString);

        boolean hasAirlineParam = airlineStringList != null;
        boolean hasFlightNumberParam = flightNumberString != null;

        if (hasAirlineParam && hasFlightNumberParam) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Cannot specify both %s and %s parameters. " +
                                    "Use %s for airline flights OR %s for flights with given flight number",
                            ParameterNames.AIRLINE_PARAM_NAME, ParameterNames.FLIGHT_NUMBER_PARAM_NAME,
                            ParameterNames.AIRLINE_PARAM_NAME, ParameterNames.FLIGHT_NUMBER_PARAM_NAME));
        }

        List<Integer> routeIdList = null;
        if (routeIdStringList != null && !routeIdStringList.isEmpty()) {
            routeIdList = routeIdStringList.stream().map(routeIdString ->
                    ValidationUtils.validateAndGetRouteId(routeIdString,routeService)).toList();
        }
        ZonedDateTime startTime;
        if (StringUtils.isNotBlank(startString) && startString.equalsIgnoreCase("now")) {
            startTime = ZonedDateTime.now();
        } else {
            startTime = ValidationUtils.validateAndGetZDT(ParameterNames.START, startString);
        }
        ZonedDateTime endTime;
        if (StringUtils.isNotBlank(endString) && endString.equalsIgnoreCase("+1day")) {
            endTime = startTime.plusDays(1);
        } else {
            endTime = ValidationUtils.validateAndGetZDT(ParameterNames.END,endString);
        }
        Boolean isActive = ValidationUtils.validateAndGetBoolean(ParameterNames.IS_ACTIVE_PARAM_NAME,isActiveString);
        int page = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE,pageString);
        int pageSize = ValidationUtils.validateAndGetInteger(ParameterNames.SIZE,pageSizeString);

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
    public Flight getFlight(@PathVariable(name = ParameterNames.ID_PATH_VAR_NAME) String idString) {
        LOGGER.info("GET {} with {}:{}",Path.FLIGHT_BY_ID,ParameterNames.ID_PATH_VAR_NAME,idString);
        Integer id = ValidationUtils.validateAndGetInteger(ParameterNames.ID_PATH_VAR_NAME,idString);
        Option<Flight> flightOption = flightService.getById(id);
        if (flightOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(
                        ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        return flightOption.get();
    }

    @GetMapping(Path.FLIGHT)
    public Flight getFlight(@RequestParam(name = ParameterNames.ROUTE_ID_PARAM_NAME) String routeIdString,
                            @RequestParam(name = ParameterNames.FLIGHT_NUMBER_PARAM_NAME) String flightNumber,
                            @RequestParam(name = ParameterNames.ON_DAY) String onDayString,
                            @RequestParam(name = ParameterNames.ZONE_ID) String zoneIdString) {
        LOGGER.info("GET {} with {}:{}, {}:{}, {}:{}, {}:{}",Path.FLIGHT,
                ParameterNames.ROUTE_ID_PARAM_NAME,routeIdString,
                ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber,
                ParameterNames.ON_DAY,onDayString,
                ParameterNames.ZONE_ID,zoneIdString);

        ZoneId zoneId = ValidationUtils.validateAndGetZoneId(zoneIdString);
        LocalDate localDate = ValidationUtils.validateAndGetLocalDate(onDayString);
        ZonedDateTime startOfDay = localDate.atStartOfDay(zoneId);
        Integer routeId = ValidationUtils.validateAndGetRouteId(routeIdString,routeService);
        Option<Flight> flightOption = flightService.getFlightOnDay(startOfDay,flightNumber,routeId);
        if (flightOption.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForMultiParameterMessage(
                            ParameterNames.ROUTE_ID_PARAM_NAME,routeIdString,
                            ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        }
        return flightOption.get();
    }
}