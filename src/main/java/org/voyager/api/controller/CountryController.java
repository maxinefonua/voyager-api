package org.voyager.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.voyager.api.model.request.CountryPageRequest;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.model.response.PagedResponse;

import java.util.List;

@RestController
@RequestMapping(Path.COUNTRIES)
public class CountryController {
    @Autowired
    CountryService countryService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CountryController.class);

    @GetMapping
    public PagedResponse<Country> getCountries(
            @RequestParam(name = ParameterNames.CONTINENT, required = false) List<String> continentStringList,
            @RequestParam(name = ParameterNames.PAGE, required = false, defaultValue = "0") String pageString,
            @RequestParam(name = ParameterNames.SIZE, required = false, defaultValue = "20") String pageSizeString) {
        LOGGER.info("GET countries called with continentStringList: '{}'", continentStringList);
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        int page = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE,pageString);
        int pageSize = ValidationUtils.validateAndGetInteger(ParameterNames.SIZE,pageSizeString);
        CountryPageRequest countryPageRequest = CountryPageRequest.builder()
                .continentList(continentList)
                .pageRequest(PageRequest.of(page,pageSize))
                .build();
        PagedResponse<Country> response = countryService.getPagedCountries(countryPageRequest);
        LOGGER.debug("GET countries response: '{}'", response);
        return response;
    }

    @GetMapping("/{countryCode}")
    public Country getCountryByCode(@PathVariable(name = ParameterNames.COUNTRY_CODE) String countryCodeString) {
        LOGGER.info("GET countries/{} called", countryCodeString);
        String validatedCountryCode = ValidationUtils.validateAndGetCountryCode(false,countryCodeString,countryService);
        Country response = countryService.getCountry(validatedCountryCode);
        LOGGER.info("GET country response: '{}'", response);
        return response;
    }
}