package org.voyager.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlinePathQuery;
import org.voyager.commons.model.airline.AirlineAirportQuery;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.AirportsService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import java.util.List;

@RestController
@RequestMapping(Path.AIRLINES)
public class AirlineController {
    @Autowired
    AirlineService airlineService;
    @Autowired
    AirportsService airportsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineController.class);

    @GetMapping
    public List<Airline> getAirlines(
            @RequestParam(name = ParameterNames.IATA, required = false)
                List<String> iataList,
            @RequestParam(name = ParameterNames.OPERATOR,defaultValue = "OR")
                String operatorString,
            @RequestParam(name = ParameterNames.ORIGIN, required = false)
                List<String> originList,
            @RequestParam(name = ParameterNames.DESTINATION, required = false)
                List<String> destinationList) {
        LOGGER.info("GET /airlines with iataList:{}, operatorString:{}, originList:{}, destinationList:{}",
                iataList,operatorString,originList,destinationList);

        boolean hasAirportParams = iataList != null && !iataList.isEmpty();
        boolean hasPathParams = originList != null || destinationList != null;

        if (hasAirportParams && hasPathParams) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Cannot specify both %s and %s/%s parameters. " +
                                    "Use %s for airport airlines OR %s/%s for path airlines",
                            ParameterNames.IATA,
                            ParameterNames.ORIGIN,ParameterNames.DESTINATION,
                            ParameterNames.IATA,
                            ParameterNames.ORIGIN,ParameterNames.DESTINATION));
        }

        if (hasAirportParams) {
            iataList = ValidationUtils.validateIataCodeList(
                    ParameterNames.IATA,iataList,airportsService);
            AirlineAirportQuery airlineAirportQuery = AirlineAirportQuery.builder().iatalist(iataList).build();
            if (StringUtils.isNotBlank(operatorString)) {
                SearchOperator operator = ValidationUtils.validateAndGetOperator(operatorString);
                airlineAirportQuery.setOperator(operator);
            }
            return airlineService.getAirlines(airlineAirportQuery);
        }

        if (hasPathParams) {
            originList = ValidationUtils.validateIataCodeList(
                    ParameterNames.ORIGIN,originList,airportsService);
            destinationList = ValidationUtils.validateIataCodeList(
                    ParameterNames.DESTINATION,destinationList,airportsService);
            AirlinePathQuery airlinePathQuery = AirlinePathQuery.builder()
                    .originList(originList).destinationList(destinationList).build();
            return airlineService.getAirlines(airlinePathQuery);
        }
        return airlineService.getAirlines();
    }
}
