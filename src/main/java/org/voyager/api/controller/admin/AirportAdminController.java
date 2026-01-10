package org.voyager.api.controller.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    @Value("${runtime.environment}")
    private String runtimeEnvironment;

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

    @DeleteMapping(Path.BY_IATA)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Caching(evict = {
            @CacheEvict(value = "airportCache", key = "#iata"),
            @CacheEvict(value = "iataCache", allEntries = true),
            @CacheEvict(value = "airportsCache", allEntries = true),
            @CacheEvict(value = "nearbyAirportsCache", allEntries = true)
    })
    public void deleteAirport(@PathVariable(ParameterNames.IATA_PARAM_NAME) String iata) {
        // TODO: add filter check on delete airports endpoint, verify tests auth key
        if (!isTestEnvironment()) {
            LOGGER.warn("DELETE endpoint called in non-test environment: {}", runtimeEnvironment);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "DELETE endpoint only available in dev/stage environments");
        }

        iata = ValidationUtils.validateIataToUpperCase(iata, airportsService, ParameterNames.IATA_PARAM_NAME,
                false);
        LOGGER.info("Test DELETE /airports/{} in environment: {}", iata, runtimeEnvironment);

        airportsService.deleteAirport(iata);
        LOGGER.debug("Successfully deleted airport: {}", iata);
    }

    private boolean isTestEnvironment() {
        return runtimeEnvironment.equalsIgnoreCase("stage")
                || runtimeEnvironment.equalsIgnoreCase("dev");
    }
}