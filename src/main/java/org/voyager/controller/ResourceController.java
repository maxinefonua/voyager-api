package org.voyager.controller;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.AirportDisplay;
import org.voyager.model.AirportType;
import org.voyager.model.TownDisplay;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.repository.TownRepository;
import org.voyager.service.AirportsService;
import org.voyager.service.RegionService;
import org.voyager.service.SearchLocationService;

import java.util.*;

import static org.voyager.error.MessageConstants.*;

@RestController
class ResourceController {

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private SearchLocationService searchLocationService;

    @Autowired
    private AirportsService<AirportDisplay> airportsService;

    @GetMapping("/towns")
    @Cacheable("townCache")
    public List<TownDisplay> getTowns() {
        System.out.println("fetching uncached getTowns");
        return townRepository.findAll().stream().map(town -> new TownDisplay(town.getName(),town.getCountry(),
                regionService.getRegionById(town.getRegionId()).get().getName()
                )).toList();
    }

    @GetMapping("/search")
    @Cacheable("searchCache")
    public VoyagerListResponse<ResultSearch> search(@RequestParam String q, @RequestParam(defaultValue = "0") Integer skipRowCount, @RequestParam(defaultValue = "10") Integer limit) {
        System.out.println("fetching uncached q = '" + q + "', skipRowCount = " + skipRowCount);
        return searchLocationService.search(q,skipRowCount,limit);
    }

    @GetMapping("/search-attribution")
    @Cacheable("searchAttributionCache")
    public LookupAttribution attribution(){
        return searchLocationService.attribution();
    }

    @GetMapping("/nearby-airports")
    @Cacheable("nearbyAirportsCache")
    public List<AirportDisplay> nearbyAirports(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam(defaultValue = "5") Integer limit, @RequestParam(defaultValue = "CIVIL") AirportType type) {
        System.out.println("fetching uncached nearby airports with type: " + type + " limit: " + limit);
        return airportsService.getByTypeSortedByDistance(latitude,longitude,type,limit);
    }

    @GetMapping("/iata")
    @Cacheable("iataCodesCache")
    public List<String> getIataCodes(@RequestParam Optional<AirportType> type) {
        if (type.isEmpty()) return airportsService.getIata();
        return airportsService.getIataByType(type.get());
    }

    @GetMapping("/airports/{iata}")
    @Cacheable("iataCache")
    public AirportDisplay getAirportsByIata(@PathVariable(IATA_PARAM_NAME) String iata) {
        System.out.println("fetching uncached airport by iata");
        Set<String> validCodes = new HashSet<>(airportsService.getIata());
        if (!validCodes.contains(iata.toUpperCase())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildInvalidPathVariableMessage(iata.toUpperCase(),VALID_IATA_CONSTRAINT));
        Optional<AirportDisplay> result = airportsService.getByIata(iata.toUpperCase());
        // TODO: update to correct message
        if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "A valid IATA code was provided, but returned no results from airports service");
        return result.get();
    }

    @GetMapping("/airports")
    @Cacheable("airportsCache")
    public List<AirportDisplay> getAirports(@RequestParam(COUNTRY_CODE_PARAM_NAME) Optional<String> countryCodeOptional,
                                            @RequestParam(TYPE_PARAM_NAME) Optional<AirportType> typeOptional) {
        String countryCode = countryCodeOptional.orElse(null);
        AirportType type = typeOptional.orElse(null);
        if (StringUtils.isNotEmpty(countryCode)) {
            countryCode = countryCode.toUpperCase();
            if (countryCodeOptional.get().length() != 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(COUNTRY_CODE_PARAM_NAME,countryCode));
        }
        return airportsService.get(countryCode,type);
    }
}
