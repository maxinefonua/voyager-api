package org.voyager.api.controller.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.config.EnvironmentConfig;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;

@RestController
@RequestMapping(Path.Admin.COUNTRIES)
public class CountryAdminController {
    @Autowired
    EnvironmentConfig environmentConfig;

    @Autowired
    CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryAdminController.class);

    @PostMapping
    public Country addCountry(@RequestBody(required = false) @Valid CountryForm countryForm,
                              BindingResult bindingResult) {
        LOGGER.info("POST countries called with countryForm: '{}'", countryForm);
        ValidationUtils.validateCountryForm(countryForm,bindingResult,countryService);
        Country response = countryService.addCountry(countryForm);
        LOGGER.debug("POST countries response: '{}'", response);
        return response;
    }

    @DeleteMapping(Path.BY_COUNTRY_CODE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCountry(@PathVariable(ParameterNames.IATA) String countryCodeString) {
        if (!environmentConfig.isTestEnvironment()) {
            LOGGER.warn("DELETE endpoint called in non-test environment: {}", environmentConfig.getRuntimeEnvironment());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "DELETE endpoint only available in dev/stage environments");
        }
        LOGGER.info("Test DELETE {}/{} in environment: {}",
                Path.Admin.COUNTRIES,countryCodeString,environmentConfig.getRuntimeEnvironment());
        String validatedCountryCode = ValidationUtils.validateAndGetCountryCode(
                false,countryCodeString,countryService);
        countryService.deleteCountry(validatedCountryCode);
        LOGGER.debug("Successfully deleted country: {}", validatedCountryCode);
    }
}
