package org.voyager.controller;
import io.vavr.control.Option;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.service.*;
import org.voyager.validate.ValidationUtils;
import java.util.*;
import static org.voyager.utils.ConstantsUtils.*;

@RestController
class ResourceController {
    @Autowired
    private SearchLocationService searchLocationService;
    @Autowired
    private LocationService locationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);

    @GetMapping("/search")
    public VoyagerListResponse<ResultSearch> search(@RequestParam(QUERY_PARAM_NAME) String q,
                                                    @RequestParam(name = SKIP_ROW_PARAM_NAME,defaultValue = "0") Integer skipRowCount,
                                                    @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "10") Integer limit) {
        LOGGER.debug(String.format("fetching uncached q = '%s', skipRowCount = %d",q,skipRowCount));
        return searchLocationService.search(q,skipRowCount,limit);
    }

    @GetMapping("/search-attribution")
    @Cacheable("searchAttributionCache")
    public LookupAttribution attribution(){
        return searchLocationService.attribution();
    }

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
