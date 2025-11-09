package org.voyager.api.controller;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.model.path.PathDetailedResponse;
import org.voyager.api.model.query.PathQuery;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.PathService;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlinePathQuery;
import org.voyager.commons.model.path.airline.AirlinePath;
import org.voyager.commons.model.path.PathResponse;
import org.voyager.commons.model.path.airline.PathAirlineQuery;
import org.voyager.commons.model.path.route.RoutePath;
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
    @Autowired
    FlightService flightService;
    @Autowired
    PathService pathService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    // TODO: for PATH SERVICE, order routes to search by DISTANCE!!

    @GetMapping("/path")
    @Cacheable("pathCache")
    public PathDetailedResponse getPathDetailed(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME)
                                                    List<String> originList,
                                                @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME)
                                                List<String> destinationList,
                                                @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false)
                                                    String airlineString,
                                                @RequestParam(name = "page",required = false,defaultValue = "0")
                                                    String pageString,
                                                @RequestParam(name = "size",required = false,defaultValue = "5")
                                                    String sizeString) {
        LOGGER.info(String.format("GET /path called with originList: '%s', destinationList: '%s', " +
                        "airlineString: '%s', pageString: '%s', sizeString: '%s'", originList,destinationList,
                airlineString, pageString,sizeString));
        Integer page = ValidationUtils.validateAndGetInteger("page",pageString);
        Integer size = ValidationUtils.validateAndGetInteger("size",sizeString);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ParameterNames.ORIGIN_PARAM_NAME,
                Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(ParameterNames.DESTINATION_PARAM_NAME,
                Set.copyOf(destinationList),airportService);
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        PathQuery pathQuery = PathQuery.builder().originSet(originSet).destinationSet(destinationSet).page(page)
                .pageSize(size).airlineOption(airlineOption).build();
        try {
            org.voyager.commons.validate.ValidationUtils.validateAndThrow(pathQuery);
            return pathService.getPathDetailedList(pathQuery);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage(),e);
        }
    }


    public PathResponse<AirlinePath> getPathOld(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME) List<String> originList,
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

    @GetMapping(Path.AIRLINE_PATH)
    @Cacheable("airlinePathCache")
    public PagedResponse<AirlinePath> getPath(@RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME)
                                                 List<String> originList,
                                                                      @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME)
                                             List<String> destinationList,
                                                                      @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME,
                                                      required = false)
                                                 String airlineString,
                                                                      @RequestParam(name = ParameterNames.EXCLUDE_PARAM_NAME,
                                                      required = false)
                                                 List<String> excludeAirportCodeList,
                                                                      @RequestParam(name = ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,
                                                      required = false)
                                                 List<String> excludeRouteIdList,
                                                                      @RequestParam(name = ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,
                                                      required = false)
                                                 List<String> excludeFlightNumberList,
                                                                      @RequestParam(name = ParameterNames.PAGE, defaultValue = "0")
                                                 String pageString,
                                                                      @RequestParam(name = ParameterNames.PAGE_SIZE, defaultValue = "5")
                                                 String sizeString) {
        LOGGER.info("GET /path-airline called with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}",
                ParameterNames.ORIGIN_PARAM_NAME,originList, ParameterNames.DESTINATION_PARAM_NAME,destinationList,
                ParameterNames.AIRLINE_PARAM_NAME,airlineString,
                ParameterNames.EXCLUDE_PARAM_NAME,excludeAirportCodeList,
                ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,excludeRouteIdList,
                ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,excludeFlightNumberList,
                ParameterNames.PAGE,pageString, ParameterNames.PAGE_SIZE,sizeString);
        int page = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE,pageString);
        int size = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE_SIZE,sizeString);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ParameterNames.ORIGIN_PARAM_NAME,
                Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(ParameterNames.DESTINATION_PARAM_NAME,
                Set.copyOf(destinationList),airportService);

        Airline airline = null;
        if (StringUtils.isNotBlank(airlineString)) {
            airline = ValidationUtils.validateAndGetAirline(airlineString);
        }

        Set<String> excludeAirportCodes = null;
        if (excludeAirportCodeList != null) {
            excludeAirportCodes = ValidationUtils.validateIataCodeSet(ParameterNames.EXCLUDE_PARAM_NAME,
                    Set.copyOf(excludeAirportCodeList),airportService);
        }

        Set<Integer> excludeRouteIds = null;
        if (excludeRouteIdList != null) {
            excludeRouteIds = excludeRouteIdList.stream()
                    .map(routeIdString -> ValidationUtils.validateAndGetRouteId(routeIdString,routeService))
                    .collect(Collectors.toSet());
        }

        Set<String> excludeFlightNumbers = null;
        if (excludeFlightNumberList != null) {
            excludeFlightNumbers = excludeFlightNumberList.stream()
                    .map(flightNumberString -> ValidationUtils.validateAndGetFlightNumber(flightNumberString,flightService))
                    .collect(Collectors.toSet());
        }

        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder()
                .originSet(originSet)
                .destinationSet(destinationSet)
                .airline(airline)
                .page(page)
                .size(size)
                .excludeSet(excludeAirportCodes)
                .excludeRouteIdSet(excludeRouteIds)
                .excludeFlightNumberSet(excludeFlightNumbers)
                .build();

        PagedResponse<AirlinePath> response = pathService.getAirlinePathList(pathAirlineQuery);
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
