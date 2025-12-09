package org.voyager.api.controller.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.voyager.api.service.AirportsService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;

@RestController
@RequestMapping(Path.Admin.AIRPORTS)
public class AirportAdminController {
    @Autowired
    AirportsService airportsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportAdminController.class);

    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = "iataCache", allEntries = true),
            @CacheEvict(value = "airportsCache", allEntries = true),
            @CacheEvict(value = "nearbyAirportsCache", allEntries = true)
    })
    public Airport addAirport(@RequestBody(required = false) @Valid AirportForm airportForm,
                              BindingResult bindingResult) {
        LOGGER.info("POST /airports with airportForm: {}",airportForm);
        ValidationUtils.validate(airportForm,bindingResult);
        Airport created = airportsService.createAirport(airportForm);
        LOGGER.debug("response: '{}'", created);
        return created;
    }

    @PatchMapping(Path.BY_IATA)
    @Caching(evict = {
            @CacheEvict(value = "airportCache", key = "#iata"),
            @CacheEvict(value = "iataCache", allEntries = true),
            @CacheEvict(value = "airportsCache", allEntries = true),
            @CacheEvict(value = "nearbyAirportsCache", allEntries = true)
    }, put = {
            @CachePut(value = "airportCache", key = "#iata") // Cache the updated object
    })
    public Airport patchAirport(@PathVariable(ParameterNames.IATA_PARAM_NAME) String iata,
                                      @RequestBody(required = false) @Valid AirportPatch airportPatch,
                                      BindingResult bindingResult) {
        LOGGER.info("PATCH /airports/{} with airportPatch: '{}'", iata, airportPatch);
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,ParameterNames.IATA_PARAM_NAME,false);
        ValidationUtils.validateAirportPatch(airportPatch,bindingResult);
        Airport response = airportsService.patch(iata,airportPatch);
        LOGGER.debug("PATCH response: '{}'", response);
        return response;
    }
}
