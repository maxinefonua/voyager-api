package org.voyager.controller;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.model.Airline;
import org.voyager.model.route.Path;
import org.voyager.model.route.PathAirline;
import org.voyager.model.route.PathResponse;
import org.voyager.service.AirportsService;
import org.voyager.service.RouteService;
import org.voyager.validate.ValidationUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.voyager.utils.ConstantsUtils.*;
import static org.voyager.utils.ConstantsUtils.EXCLUDE_PARAM_NAME;

@RestController
public class PathController {
    @Autowired
    RouteService routeService;
    @Autowired
    AirportsService airportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    @GetMapping("/path-airline")
    @Cacheable("pathAirlineCache")
    public PathResponse<PathAirline> getPath(@RequestParam(name = ORIGIN_PARAM_NAME) List<String> originList,
                                             @RequestParam(name = DESTINATION_PARAM_NAME) List<String> destinationList,
                                             @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString,
                                             @RequestParam(name = EXCLUDE_PARAM_NAME, required = false) List<String> excludeAirportCodeList,
                                             @RequestParam(name = EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteIdList,
                                             @RequestParam(name = EXCLUDE_FLIGHT_PARAM_NAME,required = false) List<String> excludeFlightNumberList,
                                             @RequestParam(name = LIMIT_PARAM_NAME, defaultValue = "5") String limitString) {
        LOGGER.info(String.format("GET /path-airline called with originList: '%s', destinationList: '%s', " +
                "airlineString: '%s', excludeAirportCodeList: '%s', excludeRouteIdList: '%s', " +
                "excludeFlightNumberList: '%s', limitString: '%s", originList,destinationList,airlineString,
                excludeAirportCodeList,excludeRouteIdList,excludeFlightNumberList,limitString));
        Integer limit = ValidationUtils.validateAndGetInteger(LIMIT_PARAM_NAME,limitString,true);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ORIGIN_PARAM_NAME,
                Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(DESTINATION_PARAM_NAME,
                Set.copyOf(destinationList),airportService);
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        Set<String> excludeAirportCodes = Set.of();
        if (excludeAirportCodeList != null) {
            excludeAirportCodeList.replaceAll(iata -> ValidationUtils.validateIataToUpperCase(
                    iata,airportService,EXCLUDE_PARAM_NAME, true));
            excludeAirportCodes = Set.copyOf(excludeAirportCodeList);
        }

        Set<Integer> excludeRouteIds = Set.of();
        if (excludeRouteIdList != null) {
            excludeRouteIds = excludeRouteIdList.stream()
                    .map(routeIdString -> ValidationUtils.validateAndGetInteger(
                            EXCLUDE_ROUTE_PARAM_NAME,routeIdString, true))
                    .collect(Collectors.toSet());
        }

        Set<String> excludeFlightNumbers = Set.of();
        if (excludeFlightNumberList != null) {
            excludeFlightNumbers = Set.copyOf(excludeFlightNumberList);
        }
        PathResponse<PathAirline> response = routeService.getAirlinePathList(originSet,destinationSet,
                airlineOption,limit,excludeAirportCodes,excludeRouteIds,excludeFlightNumbers);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    public List<Path> getPathList(@RequestParam(name = ORIGIN_PARAM_NAME) List<String> originList,
                                  @RequestParam(name = DESTINATION_PARAM_NAME) List<String> destinationList,
                                  @RequestParam(name = EXCLUDE_PARAM_NAME, required = false) List<String> excludeAirportCodeList,
                                  @RequestParam(name = EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteIdList,
                                  @RequestParam(name = EXCLUDE_FLIGHT_PARAM_NAME,required = false) List<String> excludeFlightNumberList,
                                  @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "5") String limitString) {
        Integer limit = ValidationUtils.validateAndGetInteger(LIMIT_PARAM_NAME,limitString,true);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ORIGIN_PARAM_NAME,Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(DESTINATION_PARAM_NAME,Set.copyOf(destinationList),airportService);
        Set<String> excludeAirportCodes = Set.of();
        Set<Integer> excludeRouteIds = Set.of();
        Set<String> excludeFlightNumbers = Set.of();
        if (excludeAirportCodeList != null) {
            excludeAirportCodeList.replaceAll(iata -> ValidationUtils.validateIataToUpperCase(iata,airportService,EXCLUDE_PARAM_NAME, true));
            excludeAirportCodes = Set.copyOf(excludeAirportCodeList);
        }
        if (excludeRouteIdList != null) {
            excludeRouteIds = excludeRouteIdList.stream()
                    .map(routeIdString -> ValidationUtils.validateAndGetInteger(EXCLUDE_ROUTE_PARAM_NAME,routeIdString, true))
                    .collect(Collectors.toSet());
        }
        if (excludeFlightNumberList != null) {
            excludeFlightNumbers = Set.copyOf(excludeFlightNumberList);
        }
        return routeService.getPathList(originSet,destinationSet,limit,excludeAirportCodes,excludeRouteIds,excludeFlightNumbers);
    }
}
