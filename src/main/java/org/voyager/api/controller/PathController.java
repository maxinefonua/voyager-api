package org.voyager.api.controller;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.route.AirlinePath;
import org.voyager.commons.model.route.PathResponse;
import org.voyager.commons.model.route.RoutePath;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.RouteService;
import org.voyager.api.validate.ValidationUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class PathController {
    @Autowired
    RouteService routeService;
    @Autowired
    AirportsService airportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    // TODO: for PATH SERVICE, order routes to search by DISTANCE!!

    @GetMapping(Path.AIRLINE_PATH)
    @Cacheable("pathAirlineCache")
    public PathResponse<AirlinePath> getPath(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME) List<String> originList,
                                             @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME) List<String> destinationList,
                                             @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false) String airlineString,
                                             @RequestParam(name = ParameterNames.EXCLUDE_PARAM_NAME, required = false) List<String> excludeAirportCodeList,
                                             @RequestParam(name = ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteIdList,
                                             @RequestParam(name = ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,required = false) List<String> excludeFlightNumberList,
                                             @RequestParam(name = ParameterNames.LIMIT_PARAM_NAME, defaultValue = "5") String limitString) {
        LOGGER.info(String.format("GET /path-airline called with originList: '%s', destinationList: '%s', " +
                "airlineString: '%s', excludeAirportCodeList: '%s', excludeRouteIdList: '%s', " +
                "excludeFlightNumberList: '%s', limitString: '%s", originList,destinationList,airlineString,
                excludeAirportCodeList,excludeRouteIdList,excludeFlightNumberList,limitString));
        Integer limit = ValidationUtils.validateAndGetInteger(ParameterNames.LIMIT_PARAM_NAME,limitString,true);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ParameterNames.ORIGIN_PARAM_NAME,
                Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(ParameterNames.DESTINATION_PARAM_NAME,
                Set.copyOf(destinationList),airportService);
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        Set<String> excludeAirportCodes = Set.of();
        if (excludeAirportCodeList != null) {
            excludeAirportCodeList.replaceAll(iata -> ValidationUtils.validateIataToUpperCase(
                    iata,airportService,ParameterNames.EXCLUDE_PARAM_NAME, true));
            excludeAirportCodes = Set.copyOf(excludeAirportCodeList);
        }

        Set<Integer> excludeRouteIds = Set.of();
        if (excludeRouteIdList != null) {
            excludeRouteIds = excludeRouteIdList.stream()
                    .map(routeIdString -> ValidationUtils.validateAndGetInteger(
                            ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdString, true))
                    .collect(Collectors.toSet());
        }

        Set<String> excludeFlightNumbers = Set.of();
        if (excludeFlightNumberList != null) {
            excludeFlightNumbers = Set.copyOf(excludeFlightNumberList);
        }
        PathResponse<AirlinePath> response = routeService.getAirlinePathList(originSet,destinationSet,
                airlineOption,limit,excludeAirportCodes,excludeRouteIds,excludeFlightNumbers);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.ROUTE_PATH)
    @Cacheable("pathAirlineCache")
    public List<RoutePath> getPathList(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME) List<String> originList,
                                       @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME) List<String> destinationList,
                                       @RequestParam(name = ParameterNames.EXCLUDE_PARAM_NAME, required = false) List<String> excludeAirportCodeList,
                                       @RequestParam(name = ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteIdList,
                                       @RequestParam(name = ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,required = false) List<String> excludeFlightNumberList,
                                       @RequestParam(name = ParameterNames.LIMIT_PARAM_NAME,defaultValue = "5") String limitString) {
        Integer limit = ValidationUtils.validateAndGetInteger(ParameterNames.LIMIT_PARAM_NAME,limitString,true);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ParameterNames.ORIGIN_PARAM_NAME,Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(ParameterNames.DESTINATION_PARAM_NAME,Set.copyOf(destinationList),airportService);
        Set<String> excludeAirportCodes = Set.of();
        Set<Integer> excludeRouteIds = Set.of();
        Set<String> excludeFlightNumbers = Set.of();
        if (excludeAirportCodeList != null) {
            excludeAirportCodeList.replaceAll(iata -> ValidationUtils.validateIataToUpperCase(iata,airportService,ParameterNames.EXCLUDE_PARAM_NAME, true));
            excludeAirportCodes = Set.copyOf(excludeAirportCodeList);
        }
        if (excludeRouteIdList != null) {
            excludeRouteIds = excludeRouteIdList.stream()
                    .map(routeIdString -> ValidationUtils.validateAndGetInteger(ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdString, true))
                    .collect(Collectors.toSet());
        }
        if (excludeFlightNumberList != null) {
            excludeFlightNumbers = Set.copyOf(excludeFlightNumberList);
        }
        return routeService.getRoutePathList(originSet,destinationSet,limit,excludeAirportCodes,excludeRouteIds,excludeFlightNumbers);
    }
}
