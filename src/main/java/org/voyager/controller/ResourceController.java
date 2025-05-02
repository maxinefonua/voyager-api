package org.voyager.controller;
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
import org.voyager.repository.TownRepository;
import org.voyager.service.AirportsService;
import org.voyager.service.LocationService;
import org.voyager.service.RegionService;
import org.voyager.service.SearchLocationService;
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
    private AirportsService<AirportDisplay> airportsService;

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
                                                    @RequestParam(name=SKIP_ROW_PARAM_NAME,defaultValue = "0") Integer skipRowCount,
                                                    @RequestParam(name=LIMIT_PARAM_NAME,defaultValue = "10") Integer limit) {
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
                                               @RequestParam(name=LIMIT_PARAM_NAME,defaultValue = "5") Integer limit,
                                               @RequestParam(TYPE_PARAM_NAME) Optional<String> typeOptional,
                                               @RequestParam(AIRLINE_PARAM_NAME) Optional<String> airlineOptional) {
        Optional<AirportType> airportType = ValidationUtils.resolveTypeOptional(typeOptional);
        Optional<Airline> airline = ValidationUtils.resolveAirlineOptional(airlineOptional);
        return airportsService.getByDistance(latitude,longitude,limit,airportType,airline);
    }

    @GetMapping("/iata")
    @Cacheable("iataCodesCache")
    public List<String> getIataCodes(@RequestParam Optional<AirportType> type) {
        if (type.isEmpty()) return airportsService.getIata();
        return airportsService.getIataByType(type.get());
    }

    @GetMapping("/locations")
    public List<LocationDisplay> getLocations(@RequestParam(SOURCE_PROPERTY_NAME) Optional<String> sourceOptional, @RequestParam(SOURCE_ID_PARAM_NAME) Optional<String> sourceIdOptional) {
        if (sourceOptional.isEmpty() && sourceIdOptional.isEmpty()) return locationService.getLocations();
        Source source = ValidationUtils.resolveSourceOptional(sourceOptional);
        if (sourceIdOptional.isEmpty()) return locationService.getLocationsBySource(source);
        return locationService.getLocationsBySourceAndSourceId(source,sourceIdOptional.get());
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
        ValidationUtils.validateIataCode(iata,airportsService.getIata());
        Optional<AirportDisplay> result = airportsService.getByIata(iata.toUpperCase());
        assert result.isPresent();
        return result.get();
    }

    @GetMapping("/airports")
    @Cacheable("airportsCache")
    public List<AirportDisplay> getAirports(@RequestParam(COUNTRY_CODE_PARAM_NAME) Optional<String> countryCodeOptional,
                                            @RequestParam(TYPE_PARAM_NAME) Optional<String> typeOptional,
                                            @RequestParam(AIRLINE_PARAM_NAME) Optional<String> airlineOptional) {
        String countryCode = countryCodeOptional.orElse(null);
        if (StringUtils.isNotEmpty(countryCode)) {
            if (countryCode.length() != 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(COUNTRY_CODE_PARAM_NAME,countryCode));
            countryCodeOptional = Optional.of(countryCode.toUpperCase());
        }
        Optional<AirportType> airportType = ValidationUtils.resolveTypeOptional(typeOptional);
        Optional<Airline> airline = ValidationUtils.resolveAirlineOptional(airlineOptional);
        return airportsService.getAll(countryCodeOptional,airportType,airline);
    }
}
