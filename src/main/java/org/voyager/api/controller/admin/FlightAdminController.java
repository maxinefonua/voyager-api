package org.voyager.api.controller.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.voyager.api.service.FlightService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.flight.FlightBatchUpsertResult;

@RestController
@RequestMapping(Path.Admin.FLIGHTS)
public class FlightAdminController {
    @Autowired
    FlightService flightService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightAdminController.class);

    @PostMapping
    public FlightBatchUpsertResult upsertFlights(
            @Valid @RequestBody(required = false) FlightBatchUpsert flightBatchUpsert,
            BindingResult bindingResult) {
        LOGGER.info("POST {} of {}",Path.Admin.FLIGHTS,flightBatchUpsert);
        ValidationUtils.validate(flightBatchUpsert,bindingResult);
        return flightService.batchUpsert(flightBatchUpsert);
    }

    @DeleteMapping
    public Integer batchDeleteFlights(
            @Valid @RequestBody(required = false) FlightBatchDelete flightBatchDelete,
            BindingResult bindingResult) {
        LOGGER.trace("DELETE {} of {}",Path.Admin.FLIGHTS,flightBatchDelete);
        ValidationUtils.validate(flightBatchDelete,bindingResult);
        return flightService.batchDelete(flightBatchDelete);
    }
}
