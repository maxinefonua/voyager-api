package org.voyager.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.vavr.control.Option;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.location.*;
import org.voyager.service.AirportsService;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationController.class);

    @GetMapping("/locations")
    public List<Location> getLocations(@RequestParam(name = SOURCE_PROPERTY_NAME,required = false) String sourceString,
                                       @RequestParam(name = SOURCE_ID_PARAM_NAME, required = false) String sourceId,
                                       @RequestParam(name = LOCATION_STATUS_PARAM_NAME, required = false) String statusString) {
        if (StringUtils.isEmpty(sourceString) && StringUtils.isEmpty(sourceId) && StringUtils.isEmpty(statusString)) return locationService.getLocations();
        if (StringUtils.isEmpty(sourceString) && StringUtils.isEmpty(sourceId)) {
            Status status = ValidationUtils.validateAndGetLocationStatus(statusString);
            return locationService.getLocationsByStatus(status);
        }
        Source source = ValidationUtils.validateAndGetSource(sourceString);
        if (StringUtils.isEmpty(sourceId) && StringUtils.isEmpty(statusString)) return locationService.getLocationsBySource(source);
        if (StringUtils.isEmpty(statusString)) return locationService.getLocationsBySourceAndSourceId(source,sourceId);
        Status status = ValidationUtils.validateAndGetLocationStatus(statusString);
        if (StringUtils.isEmpty(sourceId)) return locationService.getLocationsBySourceAndStatus(source,status);
        return locationService.getLocationsBySourceAndSourceIdAndStatus(source,sourceId,status);
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
