package org.voyager.controller;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.model.Airline;
import org.voyager.model.route.Path;
import org.voyager.model.route.PathAirline;
import org.voyager.service.AirportsService;
import org.voyager.service.RouteService;
import org.voyager.validate.ValidationUtils;

import java.util.ArrayList;
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

    @GetMapping("/path2/{origin}/to/{destination}")
    public Path getOriginalRoutes(@PathVariable(name = ORIGIN_PARAM_NAME) String origin,
                          @PathVariable(name = DESTINATION_PARAM_NAME) String destination,
                          @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString,
                          @RequestParam(name = EXCLUDE_PARAM_NAME, required = false) List<String> exclusionList,
                          @RequestParam(name = EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteList) {
        origin = ValidationUtils.validateIataToUpperCase(origin,airportService,ORIGIN_PARAM_NAME,false);
        destination = ValidationUtils.validateIataToUpperCase(destination,airportService,DESTINATION_PARAM_NAME,false);
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
        Set<String> exclusionSet = Set.of();
        List<Integer> excludeRouteIds = new ArrayList<>();
        if (exclusionList != null) {
            exclusionList.replaceAll(iata -> ValidationUtils.validateIataToUpperCase(iata,airportService,EXCLUDE_PARAM_NAME, true));
            exclusionSet = Set.copyOf(exclusionList);
        }
        if (excludeRouteList != null) {
            excludeRouteList.forEach(routeIdString -> {
                Integer routeId = Integer.parseInt(routeIdString);
                if (!excludeRouteIds.contains(routeId)) excludeRouteIds.add(routeId);
            });
        }
        return routeService.buildPathWithExclusions(origin,destination,airlineOption,exclusionSet,excludeRouteIds);
    }

    @GetMapping("/path/{origin}/to/{destination}")
    public List<PathAirline> getRoutes(@PathVariable(name = ORIGIN_PARAM_NAME) String origin,
                                       @PathVariable(name = DESTINATION_PARAM_NAME) String destination,
                                       @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString,
                                       @RequestParam(name = EXCLUDE_PARAM_NAME, required = false) List<String> excludeAirportCodeList,
                                       @RequestParam(name = EXCLUDE_ROUTE_PARAM_NAME,required = false) List<String> excludeRouteIdList,
                                       @RequestParam(name = EXCLUDE_FLIGHT_PARAM_NAME,required = false) List<String> excludeFlightNumberList,
                                       @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "10") String limitString) {
        Integer limit = ValidationUtils.validateAndGetInteger(LIMIT_PARAM_NAME,limitString,true);
        origin = ValidationUtils.validateIataToUpperCase(origin,airportService,ORIGIN_PARAM_NAME,false);
        destination = ValidationUtils.validateIataToUpperCase(destination,airportService,DESTINATION_PARAM_NAME,false);
        Option<Airline> airlineOption = ValidationUtils.resolveAirlineString(airlineString);
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
        return routeService.getAirlinePathList(origin,destination,airlineOption,limit,excludeAirportCodes,excludeRouteIds,excludeFlightNumbers);
    }
}
