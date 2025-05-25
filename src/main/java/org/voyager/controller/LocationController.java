package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.location.Location;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.Source;
import org.voyager.service.LocationService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.SOURCE_ID_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.SOURCE_PROPERTY_NAME;

@RestController
public class LocationController {
    @Autowired
    private LocationService locationService;

    @GetMapping("/locations")
    public List<Location> getLocations(@RequestParam(name = SOURCE_PROPERTY_NAME,required = false) String sourceString, @RequestParam(name = SOURCE_ID_PARAM_NAME, required = false) String sourceId) {
        if (StringUtils.isEmpty(sourceString) && StringUtils.isEmpty(sourceId)) return locationService.getLocations();
        Source source = ValidationUtils.validateAndGetSource(sourceString);
        if (StringUtils.isEmpty(sourceId)) return locationService.getLocationsBySource(source);
        return locationService.getLocationsBySourceAndSourceId(source,sourceId);
    }

    @GetMapping("/locations/{id}")
    public Location getLocationById(@PathVariable(name = "id") String idString) {
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        Option<Location> locationOption = locationService.getLocationById(id);
        if (locationOption.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableNoMessage("id",idString));
        return locationOption.get();
    }

    @PostMapping("/locations")
    public Location addLocation(@RequestBody @Valid @NotNull LocationForm locationForm, BindingResult bindingResult) {
        ValidationUtils.validateLocationForm(locationForm, bindingResult);
        return locationService.save(locationForm);
    }
}
