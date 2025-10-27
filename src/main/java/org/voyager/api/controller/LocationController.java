package org.voyager.api.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.location.*;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.CountryService;
import org.voyager.api.service.LocationService;
import org.voyager.api.validate.ValidationUtils;
import java.util.List;

@RestController
public class LocationController {
    @Autowired
    private LocationService locationService;
    @Autowired
    private AirportsService airportsService;
    @Autowired
    CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationController.class);

    @GetMapping(Path.Admin.LOCATIONS)
    public List<Location> getLocations(@RequestParam(name = ParameterNames.SOURCE_PARAM_NAME,required = false) String sourceString,
                                       @RequestParam(name = ParameterNames.LIMIT_PARAM_NAME,required = false) String limitString,
                                       @RequestParam(name = ParameterNames.COUNTRY_CODE_PARAM_NAME, required = false) List<String> countryCodeList,
                                       @RequestParam(name = ParameterNames.LOCATION_STATUS_PARAM_NAME, required = false) List<String> statusStringList,
                                       @RequestParam(name = ParameterNames.CONTINENT_PARAM_NAME, required = false) List<String> continentStringList) {
        LOGGER.info(String.format("GET /locations with sourceString '%s', limitString '%s', " +
                        "countryCodeList: '%s', statusStringList: '%s', continentStringList: '%s'",
               sourceString, limitString, countryCodeList, statusStringList, continentStringList));
        Option<Source> sourceOption = ValidationUtils.resolveSourceString(sourceString);
        Option<Integer> limitOption = ValidationUtils.resolveLimitString(limitString,true);
        List<Continent> continentList = ValidationUtils.resolveContinentStringList(continentStringList);
        List<Status> statusList = ValidationUtils.resolveStatusStringList(statusStringList);
        if (continentList.isEmpty())
            countryCodeList = ValidationUtils.validateAndGetCountryCodeList(countryCodeList,countryService);
        else countryCodeList = List.of();
        List<Location> response = locationService.getLocations(sourceOption,countryCodeList,statusList,continentList,limitOption);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.Admin.LOCATION)
    public Location getLocation(@RequestParam(name = ParameterNames.SOURCE_PARAM_NAME) String sourceString,
                                @RequestParam(name = ParameterNames.SOURCE_ID_PARAM_NAME) String sourceId) {
        LOGGER.info(String.format("GET /location with sourceString '%s', sourceId '%s'",
                sourceString, sourceId));
        Source source = ValidationUtils.validateAndGetSource(sourceString);
        Location response = locationService.getLocation(source,sourceId);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.Admin.LOCATION_BY_ID)
    public Location getLocation(@PathVariable(name = "id") String idString) {
        LOGGER.info(String.format("GET /locations/%s",idString));
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        if (!locationService.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableNoMessage("id",idString));
        Location response = locationService.getLocationById(id);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @PatchMapping(Path.Admin.LOCATION_BY_ID)
    public Location patchLocation(@PathVariable(name = "id") String idString,
                                  @RequestBody(required = false) @Valid LocationPatch locationPatch,
                                  BindingResult bindingResult) {
        LOGGER.info(String.format("PATCH /locations/%s with locationPatch: '%s'",idString,locationPatch));
        ValidationUtils.validateLocationPatch(locationPatch,bindingResult,airportsService);
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        if (!locationService.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ParameterNames.ID_PATH_VAR_NAME,String.valueOf(id)));
        Location response = locationService.patch(id,locationPatch);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @DeleteMapping(Path.Admin.LOCATION_BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLocation(@PathVariable(name = "id") String idString) {
        LOGGER.info(String.format("GET /locations/%s",idString));
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        if (!locationService.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableNoMessage("id",idString));
        locationService.deleteById(id);
    }

    @PostMapping(Path.Admin.LOCATIONS)
    public Location addLocation(@RequestBody(required = false) @Valid LocationForm locationForm,
                                BindingResult bindingResult) {
        LOGGER.info(String.format("POST /locations with locationForm: '%s'",locationForm));
        ValidationUtils.validateLocationForm(locationForm,bindingResult,airportsService);
        Location response = locationService.save(locationForm);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }
}
