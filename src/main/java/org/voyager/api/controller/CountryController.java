package org.voyager.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import java.util.List;

@RestController
@RequestMapping(Path.COUNTRIES)
public class CountryController {
    @Autowired
    CountryService countryService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CountryController.class);

    @GetMapping
    public List<Country> getCountries(
            @RequestParam(name = ParameterNames.CONTINENT_PARAM_NAME, required = false) List<String> continentStringList) {
        LOGGER.info("GET countries called with continentStringList: '{}'", continentStringList);
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        List<Country> response = countryService.getAll(continentList);
        LOGGER.debug("GET countries response: '{}'", response);
        return response;
    }

    @GetMapping("/{countryCode}")
    public Country getCountry(@PathVariable(name = ParameterNames.COUNTRY_CODE_PARAM_NAME) String countryCodeString) {
        LOGGER.info("GET countries/{} called", countryCodeString);
        String validatedCountryCode = ValidationUtils.validateAndGetCountryCode(false,countryCodeString,countryService);
        Country response = countryService.getCountry(validatedCountryCode).get();
        LOGGER.info("GET country response: '{}'", response);
        return response;
    }
}