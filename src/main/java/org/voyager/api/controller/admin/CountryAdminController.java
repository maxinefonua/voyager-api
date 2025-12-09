package org.voyager.api.controller.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;

@RestController
@RequestMapping(Path.Admin.COUNTRIES)
public class CountryAdminController {
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
}
