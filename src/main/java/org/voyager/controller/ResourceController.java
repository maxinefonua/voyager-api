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
import org.voyager.model.*;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.LocationDisplay;
import org.voyager.model.location.Source;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.model.route.PathDisplay;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.repository.TownRepository;
import org.voyager.service.*;
import org.voyager.validate.ValidationUtils;
import java.util.*;
import static org.voyager.utils.ConstantsUtils.*;

@RestController
class ResourceController {

    @Autowired
    private TownRepository townRepository;
    @Autowired
    private RegionService regionService;
    @Autowired
    private SearchLocationService searchLocationService;
    @Autowired
    private LocationService locationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);

    @GetMapping("/towns")
    @Cacheable("townCache")
    public List<TownDisplay> getTowns() {
        LOGGER.debug("fetching uncached getTowns");
        return townRepository.findAll().stream().map(town -> new TownDisplay(town.getName(),town.getCountry(),
                regionService.getRegionById(town.getRegionId()).get().getName()
                )).toList();
    }

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
    public List<LocationDisplay> getLocations(@RequestParam(name = SOURCE_PROPERTY_NAME,required = false) String sourceString, @RequestParam(name = SOURCE_ID_PARAM_NAME, required = false) String sourceId) {
        if (StringUtils.isEmpty(sourceString) && StringUtils.isEmpty(sourceId)) return locationService.getLocations();
        Source source = ValidationUtils.validateAndGetSource(sourceString);
        if (StringUtils.isEmpty(sourceId)) return locationService.getLocationsBySource(source);
        return locationService.getLocationsBySourceAndSourceId(source,sourceId);
    }

    @GetMapping("/locations/{id}")
    public LocationDisplay getLocationById(@PathVariable(name = "id") String idString) {
        Integer id = ValidationUtils.validateAndGetInteger("id",idString,false);
        Option<LocationDisplay> locationDisplay = locationService.getLocationById(id);
        if (locationDisplay.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableNoMessage("id",idString));
        return locationDisplay.get();
    }

    @PostMapping("/locations")
    public LocationDisplay addLocation(@RequestBody @Valid @NotNull LocationForm locationForm, BindingResult bindingResult) {
        ValidationUtils.validateLocationForm(locationForm, bindingResult);
        return locationService.save(locationForm);
    }
}
