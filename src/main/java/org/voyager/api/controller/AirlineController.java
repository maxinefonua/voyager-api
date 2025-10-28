package org.voyager.api.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.api.model.query.AirlineQuery;
import org.voyager.api.service.AirlineService;
import org.voyager.api.service.AirportsService;
import org.voyager.api.validate.ValidationUtils;
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
            @RequestParam(name = ParameterNames.IATA_PARAM_NAME, required = false) List<String> iataList) {
        if (iataList == null) {
            LOGGER.info("GET /airlines");
            return airlineService.getAirlines();
        }
        StringJoiner iataJoiner = new StringJoiner(",");
        iataList.forEach(iataJoiner::add);
        LOGGER.info(String.format("GET /airlines with iataList: %s",iataJoiner));
        iataList = ValidationUtils.validateIataCodeList(
                ParameterNames.IATA_PARAM_NAME,
                Set.copyOf(iataList),airportsService);

        AirlineQuery airlineQuery = AirlineQuery.builder().withIataList(iataList).withIsActive(true).build();
        List<Airline> response = airlineService.getAirlines(airlineQuery);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
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
