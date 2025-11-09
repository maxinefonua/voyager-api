package org.voyager.api.controller;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.*;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.AirportsService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.model.geoname.fields.SearchOperator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@RestController
public class AirlineController {
    @Autowired
    AirlineService airlineService;
    @Autowired
    AirportsService airportsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineController.class);

    @GetMapping(Path.AIRLINES)
    public List<Airline> getAirlines(
            @RequestParam(name = ParameterNames.IATA_PARAM_NAME, required = false) List<String> iataList,
            @RequestParam(name = ParameterNames.OPERATOR,defaultValue = "OR") String operatorString,
            @RequestParam(name = ParameterNames.ORIGIN_PARAM_NAME, required = false) List<String> originList,
            @RequestParam(name = ParameterNames.DESTINATION_PARAM_NAME, required = false) List<String> destinationList) {
        LOGGER.info("GET /airlines with iataList:{}, operatorString:{}, originList:{}, destinationList:{}",
                iataList,operatorString,originList,destinationList);

        boolean hasAirportParams = iataList != null;
        boolean hasPathParams = originList != null || destinationList != null;

        if (hasAirportParams && hasPathParams) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Cannot specifiy both %s and %s/%s paraemeters. " +
                                    "Use %s for airport airlines OR %s/%s for path airlines",
                            ParameterNames.IATA_PARAM_NAME,
                            ParameterNames.ORIGIN_PARAM_NAME,ParameterNames.DESTINATION_PARAM_NAME,
                            ParameterNames.IATA_PARAM_NAME,
                            ParameterNames.ORIGIN_PARAM_NAME,ParameterNames.DESTINATION_PARAM_NAME));
        }

        if (hasAirportParams) {
            iataList = ValidationUtils.validateIataCodeList(
                    ParameterNames.IATA_PARAM_NAME,iataList,airportsService);
            AirlineAirportQuery airlineAirportQuery = AirlineAirportQuery.builder().iatalist(iataList).build();
            if (StringUtils.isNotBlank(operatorString)) {
                SearchOperator operator = ValidationUtils.validateAndGetOperator(operatorString);
                airlineAirportQuery.setOperator(operator);
            }
            return airlineService.getAirlines(airlineAirportQuery);
        }

        if (hasPathParams) {
            originList = ValidationUtils.validateIataCodeList(
                    ParameterNames.ORIGIN_PARAM_NAME,originList,airportsService);
            destinationList = ValidationUtils.validateIataCodeList(
                    ParameterNames.DESTINATION_PARAM_NAME,destinationList,airportsService);
            AirlinePathQuery airlinePathQuery = AirlinePathQuery.builder()
                    .originList(originList).destinationList(destinationList).build();
            return airlineService.getAirlines(airlinePathQuery);
        }

        return airlineService.getAirlines();
    }

    @PostMapping(Path.Admin.AIRLINES)
    public List<AirlineAirport> batchUpsertAirline(@RequestBody(required = false) @Valid
                                                       AirlineBatchUpsert airlineBatchUpsert,
                                                   BindingResult bindingResult) {
        LOGGER.info(String.format("POST %s called with airlineBatchUpsert: '%s'",
                Path.AIRLINES,airlineBatchUpsert));
        ValidationUtils.validateAirlineBatchUpsert(airlineBatchUpsert,bindingResult);
        return airlineService.batchUpsert(airlineBatchUpsert);
    }

    @DeleteMapping(Path.Admin.AIRLINES)
    public Integer batchDeleteAirline(
            @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME) String airlineString) {
        LOGGER.info(String.format("DELETE %s called with airlineString: '%s'",
                Path.AIRLINES,airlineString));
        Airline airline = ValidationUtils.validateAndGetAirline(airlineString);
        Integer deleted = airlineService.batchDelete(airline);
        LOGGER.debug(String.format("response: %d deleted",deleted));
        return deleted;
    }
}
