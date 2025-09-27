package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.service.CountryService;
import org.voyager.service.CurrencyService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

@RestController
public class CountryController {
    @Autowired
    CountryService countryService;

    @Autowired
    CurrencyService currencyService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryController.class);

    @GetMapping("/countries")
    public List<Country> getCountries(
            @RequestParam(name = CONTINENT_PARAM_NAME, required = false) List<String> continentStringList,
            @RequestParam(name = CURRENCY_CODE_PARAM_NAME,required = false) String currencyCode) {
        LOGGER.info(String.format("GET countries called with continentStringList: '%s', currencyCode: '%s'", continentStringList,currencyCode));
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        Option<String> currencyCodeOption = currencyCode == null ? Option.none() :
                Option.of(ValidationUtils.validateAndGetCurrencyCode(currencyCode,currencyService,true));
        List<Country> response = countryService.getAll(continentList,currencyCodeOption);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/countries/{countryCode}")
    public Country getCountry(@PathVariable(name = COUNTRY_CODE_PARAM_NAME) String countryCodeString) {
        LOGGER.info(String.format("GET countries/%s called", countryCodeString));
        String validatedCountryCode = ValidationUtils.validateAndGetCountryCode(false,countryCodeString,countryService);
        Country response = countryService.getCountry(validatedCountryCode).get();LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @PostMapping("/countries")
    public Country addCountry(@RequestBody(required = false) @Valid CountryForm countryForm,
                              BindingResult bindingResult) {
        LOGGER.info(String.format("POST countries called with countryForm: '%s'", countryForm));
        ValidationUtils.validateCountryForm(countryForm,bindingResult,countryService);
        Country response = countryService.addCountry(countryForm);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }
}