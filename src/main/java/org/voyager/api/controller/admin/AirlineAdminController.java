package org.voyager.api.controller.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.api.service.AirlineService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airline.AirlineBatchUpsertResult;

@RestController
@RequestMapping(Path.Admin.AIRLINES)
public class AirlineAdminController {
    @Autowired
    AirlineService airlineService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineAdminController.class);

    @PostMapping
    public AirlineBatchUpsertResult batchUpsertAirline(
            @RequestBody(required = false) @Valid AirlineBatchUpsert airlineBatchUpsert,
            BindingResult bindingResult) {
        LOGGER.info("POST {} called with airlineBatchUpsert: '{}'", Path.AIRLINES, airlineBatchUpsert);
        ValidationUtils.validateAirlineBatchUpsert(airlineBatchUpsert,bindingResult);
        return airlineService.batchUpsert(airlineBatchUpsert);
    }

    @PostMapping(Path.Admin.DEACTIVATE)
    public Integer deactivateAirline(
            @RequestParam(name = ParameterNames.AIRLINE, required = false) String airlineString) {
        LOGGER.info("POST {} called with airline: '{}'", Path.AIRLINES.concat(Path.Admin.DEACTIVATE), airlineString);
        Airline airline = ValidationUtils.validateAndGetAirline(airlineString);
        return airlineService.deactivateAirline(airline);
    }
}
