package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.country.Continent;
import org.voyager.model.location.*;
import org.voyager.service.AirportsService;
import org.voyager.service.CountryService;
import org.voyager.service.LocationService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

@RestController
public class LocationController {
    @Autowired
    private LocationService locationService;
    @Autowired
    private AirportsService airportsService;
    @Autowired
    CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationController.class);

    @GetMapping("/locations")
    public List<Location> getLocations(@RequestParam(name = SOURCE_PROPERTY_NAME,required = false) String sourceString,
                                       @RequestParam(name = SOURCE_ID_PARAM_NAME, required = false) String sourceId,
                                       @RequestParam(name = COUNTRY_CODE_PARAM_NAME, required = false) List<String> countryCodeList,
                                       @RequestParam(name = LOCATION_STATUS_PARAM_NAME, required = false) String statusString,
                                       @RequestParam(name = CONTINENT_PARAM_NAME, required = false) List<String> continentStringList) {
        Option<String> sourceIdOption = StringUtils.isBlank(sourceId) ? Option.none() : Option.of(sourceId);
        Option<Source> sourceOption = sourceIdOption.isEmpty() ? Option.none() : Option.of(ValidationUtils.validateAndGetSource(sourceString));
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        if (continentList.isEmpty())
            countryCodeList = ValidationUtils.validateAndGetCountryCodeList(countryCodeList,countryService);
        else countryCodeList = List.of();
        Option<Status> statusOption = ValidationUtils.resolveStatusString(statusString);
        return locationService.getLocations(sourceOption,sourceIdOption,countryCodeList,statusOption,continentList);
    }

    @GetMapping("/locations/{id}")
    public Location getLocation(@PathVariable(name = "id") String idString) {
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        // TODO: update to check if location id exists instead of fetching location
        Option<Location> locationOption = locationService.getLocationById(id);
        if (locationOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableNoMessage("id",idString));
        return locationOption.get();
    }

    @PatchMapping("/locations/{id}")
    public Location patchLocation(@PathVariable(name = "id") String idString, @RequestBody(required = false) @Valid LocationPatch locationPatch, BindingResult bindingResult) {
        ValidationUtils.validateLocationPatch(locationPatch,bindingResult,airportsService);
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        // TODO: update to check if location id exists instead of fetching location
        Option<Location> locationOption = locationService.getLocationById(id);
        if (locationOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return locationService.patch(locationOption.get(),locationPatch);
    }

    @PostMapping("/locations")
    public Location addLocation(@RequestBody(required = false) @Valid LocationForm locationForm, BindingResult bindingResult) {
        ValidationUtils.validateLocationForm(locationForm,bindingResult,airportsService);
        return locationService.save(locationForm);
    }
}
