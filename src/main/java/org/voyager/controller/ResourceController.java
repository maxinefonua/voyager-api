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
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.LocationDisplay;
import org.voyager.model.location.Source;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
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

    @Autowired
    private RouteService routeService;

    @Autowired
    private AirportsService airportsService;

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

    @GetMapping("/nearby-airports")
    @Cacheable("nearbyAirportsCache")
    public List<AirportDisplay> nearbyAirports(@RequestParam(LATITUDE_PARAM_NAME) Double latitude,
                                               @RequestParam(LONGITUDE_PARAM_NAME) Double longitude,
                                               @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "5") Integer limit,
                                               @RequestParam(name = TYPE_PARAM_NAME, required = false) String typeString,
                                               @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString) {
        Option<AirportType> airportType = ValidationUtils.resolveTypeString(typeString);
        Option<Airline> airline = ValidationUtils.resolveAirlineString(airlineString);
        return airportsService.getByDistance(latitude,longitude,limit,airportType,airline);
    }

    @GetMapping("/iata")
    @Cacheable("iataCodesCache")
    public List<String> getIataCodes(@RequestParam(name = TYPE_PARAM_NAME, required = false) String typeString) {
        Option<AirportType> typeOptional = ValidationUtils.resolveTypeString(typeString);
        if (typeOptional.isEmpty()) return airportsService.getIata();
        return airportsService.getIataByType(typeOptional.get());
    }

    @GetMapping("/routes/{id}")
    public RouteDisplay getRouteById(@PathVariable(name = "id") String idString) {
        Integer id = ValidationUtils.validateAndGetInteger(ID_PATH_VAR_NAME,idString,false);
        Option<RouteDisplay> routeDisplay = routeService.getRouteById(id);
        if (routeDisplay.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(ID_PATH_VAR_NAME,String.valueOf(id)));
        return routeDisplay.get();
    }

    @GetMapping("/routes")
    public List<RouteDisplay> getRoutes(@RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString, @RequestParam(name = ORIGIN_PARAM_NAME, required = false) String origin, @RequestParam(name = DESTINATION_PARAM_NAME, required = false) String destination) {
        Option<String> originOption = Option.none();
        Option<String> destinationOption = Option.none();
        if (!StringUtils.isEmpty(origin)) {
            ValidationUtils.validateAndGetIata(airportsService,origin,ORIGIN_PARAM_NAME,true);
            originOption = Option.of(origin.toUpperCase());
        }
        if (!StringUtils.isEmpty(destination)) {
            ValidationUtils.validateAndGetIata(airportsService,destination,DESTINATION_PARAM_NAME,true);
            destinationOption = Option.of(destination.toUpperCase());
        }
        Option<Airline> airlineOptional = ValidationUtils.resolveAirlineString(airlineString);
        return routeService.getRoutes(originOption,destinationOption,airlineOptional);
    }

    @PostMapping("/routes")
    public RouteDisplay addRoute(@RequestBody @Valid @NotNull RouteForm routeForm, BindingResult bindingResult) {
        ValidationUtils.validateRouteForm(routeForm, bindingResult);
        return routeService.save(routeForm);
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

    @GetMapping("/airports/{iata}")
    @Cacheable("iataCache")
    public AirportDisplay getAirportByIata(@PathVariable(IATA_PARAM_NAME) String iata) {
        LOGGER.debug(String.format("fetching uncached airport by iata code: %s",iata));
        return ValidationUtils.validateAndGetIata(airportsService,iata,IATA_PARAM_NAME,false);
    }

    @GetMapping("/airports")
    @Cacheable("airportsCache")
    public List<AirportDisplay> getAirports(@RequestParam(name = COUNTRY_CODE_PARAM_NAME, required = false) String countryCodeString,
                                            @RequestParam(name = TYPE_PARAM_NAME, required = false) String typeString,
                                            @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString) {
        if (countryCodeString != null) countryCodeString = ValidationUtils.validateAndGetCountryCode(countryCodeString);
        Option<AirportType> airportType = ValidationUtils.resolveTypeString(typeString);
        Option<Airline> airline = ValidationUtils.resolveAirlineString(airlineString);
        return airportsService.getAll(Option.of(countryCodeString),airportType,airline);
    }
}
