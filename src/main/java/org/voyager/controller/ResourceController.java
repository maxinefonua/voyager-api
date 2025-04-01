package org.voyager.controller;
import jakarta.annotation.PostConstruct;
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
import org.voyager.model.TownDisplay;
import org.voyager.model.entity.Town;
import org.voyager.model.response.VoyagerListResponse;
import org.voyager.model.response.geonames.GeoName;
import org.voyager.repository.TownRepository;
import org.voyager.service.AirportsService;
import org.voyager.service.RegionService;
import org.voyager.service.SearchLocationService;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.voyager.error.MessageConstants.COUNTRY_CODE_PARAM_NAME;
import static org.voyager.error.MessageConstants.VALID_IATA_CONSTRAINT;
import static org.voyager.error.MessageConstants.IATA_PARAM_NAME;

@RestController
class ResourceController {

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private SearchLocationService<GeoName> searchLocationService;

    @Autowired
    private AirportsService<AirportDisplay> airportsService;

    private Set<String> iataCodes;

    @PostConstruct
    public void loadValidationFields() {
        iataCodes = airportsService.getIataCodes();
    }

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
    public VoyagerListResponse<GeoName> search(@RequestParam String q, @RequestParam(defaultValue = "0") Integer skipRowCount) {
        System.out.println("fetching uncached q = '" + q + "', skipRowCount = " + skipRowCount);
        return searchLocationService.search(q,skipRowCount);
    }

    @GetMapping("/nearby-airports")
    @Cacheable("nearbyAirportsCache")
    public VoyagerListResponse<AirportDisplay> nearbyAirports(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam(defaultValue = "5") Integer limit) {
        System.out.println("fetching uncached nearby airports with limit: " + limit);
        List<AirportDisplay> results = airportsService.getSortedByDistance(latitude,longitude,limit);
        return VoyagerListResponse.<AirportDisplay>builder().results(results).resultCount(results.size()).build();
    }

    @GetMapping("/airports/{iata}")
    @Cacheable("iataCache")
    public AirportDisplay getAirportsByIata(@PathVariable(IATA_PARAM_NAME) String iata) {
        System.out.println("fetching uncached airport by iata");
        if (!iataCodes.contains(iata.toUpperCase())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildInvalidPathVariableMessage(iata.toUpperCase(),VALID_IATA_CONSTRAINT));
        Optional<AirportDisplay> result = airportsService.getByIata(iata.toUpperCase());
        // TODO: update to correct message
        if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "A valid IATA code was provided, but returned no results from airports service");
        return result.get();
    }

    @GetMapping("/airports")
    @Cacheable("airportsCache")
    public VoyagerListResponse<AirportDisplay> getAirports(@RequestParam(COUNTRY_CODE_PARAM_NAME) Optional<String> countryCodeOptional) {
        if (countryCodeOptional.isEmpty()) {
            System.out.println("fetching uncached airports");
            List<AirportDisplay> airportDisplays = airportsService.getAirports();
            return VoyagerListResponse.<AirportDisplay>builder().results(airportDisplays).resultCount(airportDisplays.size()).build();
        } else {
            String countryCode = countryCodeOptional.get().toUpperCase();
            // TODO: add country code validation
            if (countryCode.length() != 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(COUNTRY_CODE_PARAM_NAME,countryCode));
            System.out.println("fetching uncached airports by country code: " + countryCode);
            List<AirportDisplay> airportDisplays = airportsService.getByCountryCode(countryCode);
            return VoyagerListResponse.<AirportDisplay>builder().results(airportDisplays).resultCount(airportDisplays.size()).build();
        }
    }
}
