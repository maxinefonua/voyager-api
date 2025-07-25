package org.voyager.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.service.CountryService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.CONTINENT_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.COUNTRY_CODE_PARAM_NAME;


@RestController
public class CountryCountroller {
    @Autowired
    CountryService countryService;

    @GetMapping("/countries")
    public List<Country> getCountries(
            @RequestParam(name = CONTINENT_PARAM_NAME, required = false) List<String> continentStringList) {
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        return countryService.getAll(continentList);
    }

    @GetMapping("/countries/{countryCode}")
    public Country getCountry(@PathVariable(name = COUNTRY_CODE_PARAM_NAME) String countryCodeString) {
        String validatedCountryCode = ValidationUtils.validateAndGetCountryCode(false,countryCodeString,countryService);
        return countryService.getCountry(validatedCountryCode).get();
    }

    @PostMapping("/countries")
    public Country addCountry(@RequestBody(required = false) @Valid CountryForm countryForm,
                              BindingResult bindingResult) {
        ValidationUtils.validateCountryForm(countryForm,bindingResult,countryService);
        return countryService.addCountry(countryForm);
    }
}