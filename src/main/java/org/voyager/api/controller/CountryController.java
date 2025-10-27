package org.voyager.api.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import java.util.List;

@RestController
public class CountryController {
    @Autowired
    CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryController.class);

    @GetMapping(Path.COUNTRIES)
    public List<Country> getCountries(
            @RequestParam(name = ParameterNames.CONTINENT_PARAM_NAME, required = false) List<String> continentStringList) {
        LOGGER.info(String.format("GET countries called with continentStringList: '%s'", continentStringList));
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        List<Country> response = countryService.getAll(continentList);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/countries/{countryCode}")
    public Country getCountry(@PathVariable(name = ParameterNames.COUNTRY_CODE_PARAM_NAME) String countryCodeString) {
        LOGGER.info(String.format("GET countries/%s called", countryCodeString));
        String validatedCountryCode = ValidationUtils.validateAndGetCountryCode(false,countryCodeString,countryService);
        Country response = countryService.getCountry(validatedCountryCode).get();LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @PostMapping(Path.Admin.COUNTRIES)
    public Country addCountry(@RequestBody(required = false) @Valid CountryForm countryForm,
                              BindingResult bindingResult) {
        LOGGER.info(String.format("POST countries called with countryForm: '%s'", countryForm));
        ValidationUtils.validateCountryForm(countryForm,bindingResult,countryService);
        Country response = countryService.addCountry(countryForm);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }
}