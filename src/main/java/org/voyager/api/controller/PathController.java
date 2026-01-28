package org.voyager.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.response.SearchResponse;
import org.voyager.api.service.RouteService;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.PathSearchService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
            @RequestParam(name = ParameterNames.ORIGIN)
            List<String> originList,
            @RequestParam(name = ParameterNames.DESTINATION)
            List<String> destinationList,
            @RequestParam(name = ParameterNames.AIRLINE, required = false)
            List<String> airlineStringList,
            @RequestParam(name = ParameterNames.EXCLUDE, required = false)
            List<String> excludeAirportCodeList,
            @RequestParam(name = ParameterNames.EXCLUDE_ROUTE, required = false)
            List<String> excludeRouteIdList,
            @RequestParam(name = ParameterNames.EXCLUDE_FLIGHT, required = false)
            List<String> excludeFlightNumberList,
            @RequestParam(name = ParameterNames.ZONE_ID, required = false)
            String zoneIdString,
            @RequestParam(name = ParameterNames.SKIP, defaultValue = "0")
            String skipString,
            @RequestParam(name = ParameterNames.SIZE, defaultValue = "10")
            String sizeString,
            @RequestParam(name = ParameterNames.START,defaultValue = "now")
            String startString) {
        LOGGER.info("GET /path-airline called with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}",
                ParameterNames.ORIGIN,originList, ParameterNames.DESTINATION,destinationList,
                ParameterNames.AIRLINE,airlineStringList,
                ParameterNames.EXCLUDE,excludeAirportCodeList,
                ParameterNames.EXCLUDE_ROUTE,excludeRouteIdList,
                ParameterNames.EXCLUDE_FLIGHT,excludeFlightNumberList,
                ParameterNames.ZONE_ID,zoneIdString,
                ParameterNames.START,startString,
                ParameterNames.SIZE,sizeString,
                ParameterNames.SKIP,skipString);

        Set<String> originSet = ValidationUtils.validateIataCodeSet(ParameterNames.ORIGIN,
                Set.copyOf(originList),airportService);
        Set<String> destinationSet = ValidationUtils.validateIataCodeSet(ParameterNames.DESTINATION,
                Set.copyOf(destinationList),airportService);

        List<Airline> airlineList = Arrays.stream(Airline.values()).toList();
        if (airlineStringList != null && !airlineStringList.isEmpty()) {
            airlineList = airlineStringList.stream().map(ValidationUtils::validateAndGetAirline).toList();
        }

        Set<String> excludeAirportCodes = new HashSet<>();
        if (excludeAirportCodeList != null) {
            excludeAirportCodes = ValidationUtils.validateIataCodeSet(ParameterNames.EXCLUDE,
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

        int size = ValidationUtils.validateAndGetInteger(ParameterNames.SIZE,sizeString);
        int skip = ValidationUtils.validateAndGetInteger(ParameterNames.SKIP,skipString);
        ZoneId zoneId = ZoneOffset.UTC;
        if (StringUtils.isNotBlank(zoneIdString)) {
            zoneId = ValidationUtils.validateAndGetZoneId(zoneIdString);
        }

        ZonedDateTime startTime;
        if (StringUtils.isNotBlank(startString) && startString.equalsIgnoreCase("now")) {
            startTime = ZonedDateTime.now();
        } else {
            startTime = ValidationUtils.validateAndGetZDT(ParameterNames.START, startString);
        }
        startTime = startTime.toLocalDate().atStartOfDay(zoneId);
        return pathSearchService.searchPaths(PathSearchRequest.builder()
                .origins(originSet)
                .destinations(destinationSet)
                .excludeDestinations(excludeAirportCodes)
                .excludeRouteIds(excludeRouteIds)
                .excludeFlightNumbers(excludeFlightNumbers)
                .airlines(airlineList)
                .startTime(startTime)
                .skip(skip)
                .size(size)
                .build());
    }
}