package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.service.AirportsService;
import org.voyager.service.CountryService;
import org.voyager.validate.ValidationUtils;

import java.util.List;
import java.util.Set;

import static org.voyager.utils.ConstantsUtils.*;
import static org.voyager.utils.ConstantsUtils.AIRLINE_PARAM_NAME;

@RestController
public class AirportsController {
    @Autowired
    private AirportsService airportsService;

    @Autowired
    private CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsController.class);

    @GetMapping("/iata")
    @Cacheable("iataCache")
    public List<String> getIataCodes(@RequestParam(name = TYPE_PARAM_NAME, required = false) List<String> typeList) {
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        if (airportTypeList.isEmpty()) return airportsService.getIata();
        return airportsService.getIataByTypeIn(airportTypeList);
    }

    @GetMapping("/airports")
    @Cacheable("airportsCache")
    public List<Airport> getAirports(@RequestParam(name = COUNTRY_CODE_PARAM_NAME, required = false) String countryCodeString,
                                     @RequestParam(name = TYPE_PARAM_NAME, required = false) List<String> typeList,
                                     @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString) {
        LOGGER.info(String.format("GET /airports called with countryCodeString: '%s', typeList: '%s', airlineString: '%s'",
                countryCodeString,typeList,airlineString));
        if (countryCodeString != null) countryCodeString = ValidationUtils.validateAndGetCountryCode(true,countryCodeString, countryService);
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        Option<Airline> airline = ValidationUtils.resolveAirlineString(airlineString);
        List<Airport> response = airportsService.getAll(Option.of(countryCodeString),airportTypeList,airline);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/airport-airlines")
    @Cacheable("airportAirlinesCache")
    public List<Airline> getAirlines(@RequestParam(name = IATA_PARAM_NAME) List<String> iataList) {
        LOGGER.info("GET /airport-airlines");
        iataList = ValidationUtils.validateIataCodeList(ORIGIN_PARAM_NAME,Set.copyOf(iataList),airportsService);
        List<Airline> response = airportsService.getAirlines(iataList);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/airports/{iata}")
    @Cacheable(value = "airportCache", key = "#iata")
    public Airport getAirportByIata(@PathVariable(IATA_PARAM_NAME) String iata) {
        LOGGER.info(String.format("GET /airports/%s",iata));
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,IATA_PARAM_NAME,false);
        Airport response = airportsService.getByIata(iata);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @PatchMapping("/airports/{iata}")
    @Caching(evict = {
            @CacheEvict(value = "airportCache", key = "#iata"),
            @CacheEvict(value = "airportsCache", allEntries = true)
    })
    public Airport patchAirportByIata(@PathVariable(IATA_PARAM_NAME) String iata,
                                      @RequestBody(required = false) @Valid AirportPatch airportPatch,
                                      BindingResult bindingResult) {
        LOGGER.info(String.format("PATCH /airports/%s with airportPatch: '%s'",iata,airportPatch));
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,IATA_PARAM_NAME,false);
        ValidationUtils.validateAirportPatch(airportPatch,bindingResult);
        Airport response = airportsService.patch(iata,airportPatch);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/nearby-airports")
    @Cacheable("nearbyAirportsCache")
    public List<Airport> nearbyAirports(@RequestParam(LATITUDE_PARAM_NAME) Double latitude,
                                        @RequestParam(LONGITUDE_PARAM_NAME) Double longitude,
                                        @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "5") Integer limit,
                                        @RequestParam(name = TYPE_PARAM_NAME, required = false) List<String> typeList,
                                        @RequestParam(name = AIRLINE_PARAM_NAME, required = false) List<String> airlineStringList) {
        LOGGER.info(String.format("GET /nearby-airports called with latitude: %f, longitude: %f, limit: %d, " +
                "typeList: '%s', airlineStringList: '%s'",latitude,longitude,limit,typeList,airlineStringList));
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = ValidationUtils.resolveAirlineStringList(airlineStringList);
        List<Airport> response = airportsService.getByDistance(latitude,longitude,limit,airportTypeList,airlineList);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }
}
