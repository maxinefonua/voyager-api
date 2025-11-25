package org.voyager.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.response.SearchResponse;
import org.voyager.api.service.*;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
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
    PathSearchService pathSearchService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    @GetMapping(Path.PATH)
    public SearchResponse getSearchResponse(
            @RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME)
            List<String> originList,
            @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME)
            List<String> destinationList,
            @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false)
            List<String> airlineStringList,
            @RequestParam(name = ParameterNames.EXCLUDE_PARAM_NAME, required = false)
            List<String> excludeAirportCodeList,
            @RequestParam(name = ParameterNames.EXCLUDE_ROUTE_PARAM_NAME, required = false)
            List<String> excludeRouteIdList,
            @RequestParam(name = ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME, required = false)
            List<String> excludeFlightNumberList,
            @RequestParam(name = ParameterNames.PAGE, defaultValue = "0")
            String pageString,
            @RequestParam(name = ParameterNames.PAGE_SIZE, defaultValue = "10")
            String sizeString,
            @RequestParam(name = ParameterNames.START)
            String startString,
            @RequestParam(name = ParameterNames.END)
            String endString) {
        LOGGER.info("GET /path-airline called with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}",
                ParameterNames.ORIGIN_PARAM_NAME,originList, ParameterNames.DESTINATION_PARAM_NAME,destinationList,
                ParameterNames.AIRLINE_PARAM_NAME,airlineStringList,
                ParameterNames.EXCLUDE_PARAM_NAME,excludeAirportCodeList,
                ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,excludeRouteIdList,
                ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,excludeFlightNumberList,
                ParameterNames.PAGE,pageString, ParameterNames.PAGE_SIZE,sizeString,
                ParameterNames.START,startString,ParameterNames.END,endString);
        if (pathSearchService.isPausedForEnrichment()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "undergoing data enrichment, please try again later");
        }
        int size = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE_SIZE,sizeString);
        Set<String> originSet = ValidationUtils.validateIataCodeSet(ParameterNames.ORIGIN_PARAM_NAME,
                Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(ParameterNames.DESTINATION_PARAM_NAME,
                Set.copyOf(destinationList),airportService);

        List<Airline> airlineList = Arrays.stream(Airline.values()).toList();
        if (airlineStringList != null && !airlineStringList.isEmpty()) {
            airlineList = airlineStringList.stream().map(ValidationUtils::validateAndGetAirline).toList();
        }

        Set<String> excludeAirportCodes = new HashSet<>();
        if (excludeAirportCodeList != null) {
            excludeAirportCodes = ValidationUtils.validateIataCodeSet(ParameterNames.EXCLUDE_PARAM_NAME,
                    Set.copyOf(excludeAirportCodeList),airportService);
        }

        Set<Integer> excludeRouteIds = Set.of();
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

        ZonedDateTime startTime = ValidationUtils.validateAndGetZDT(startString);
        ZonedDateTime endTime = ValidationUtils.validateAndGetZDT(endString);

        return pathSearchService.searchPaths(PathSearchRequest.builder()
                        .origins(originSet)
                        .destinations(destinationSet)
                        .excludeDestinations(excludeAirportCodes)
                        .excludeRouteIds(excludeRouteIds)
                        .excludeFlightNumbers(excludeFlightNumbers)
                        .airlines(airlineList)
                        .startTime(startTime)
                        .endTime(endTime)
                        .size(size)
                .build());
    }

    @GetMapping("/path/{searchId}")
    public SearchResponse getMorePathResults(
            @PathVariable(name = "searchId") String searchId,
            @RequestParam(name = ParameterNames.PAGE_SIZE, defaultValue = "20") String sizeString){
        LOGGER.info("GET {} with {}:{}, {}:{}",
                "/path/{searchId}","searchId",searchId,
                ParameterNames.PAGE_SIZE,sizeString);
        if (pathSearchService.isPausedForEnrichment()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "undergoing data enrichment, please try again later");
        }
        int size = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE_SIZE,sizeString);
        return pathSearchService.getMorePaths(searchId,size);
    }
}