package org.voyager.api.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.api.model.query.IataQuery;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import java.util.List;

@RestController
public class AirportsController {
    @Autowired
    private AirportsService airportsService;
    @Autowired
    private CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsController.class);

    @GetMapping(Path.IATA)
    @Cacheable("iataCache")
    public List<String> getIataCodes(@RequestParam(name = ParameterNames.TYPE_PARAM_NAME,
            required = false) List<String> typeList,
                                     @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME,
                                             required = false) List<String> airlineStringList) {
        LOGGER.info(String.format("GET /iata called with typeList: %s, airlineList: %s",typeList,airlineStringList));
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = ValidationUtils.resolveAirlineStringList(airlineStringList);
        if (airportTypeList.isEmpty() && airlineList.isEmpty()) return airportsService.getIata();
        IataQuery.IataQueryBuilder iataQueryBuilder = IataQuery.builder();
        if (!airportTypeList.isEmpty()) {
            iataQueryBuilder.withAirportTypeList(airportTypeList);
        }
        if (!airlineList.isEmpty()) {
            iataQueryBuilder.withAirlineList(airlineList);
        }
        List<String> response = airportsService.getIata(iataQueryBuilder.build());
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.AIRPORTS)
    @Cacheable("airportsCache")
    public List<Airport> getAirports(@RequestParam(name = ParameterNames.COUNTRY_CODE_PARAM_NAME, required = false) String countryCodeString,
                                     @RequestParam(name = ParameterNames.TYPE_PARAM_NAME, required = false) List<String> typeList,
                                     @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false) String airlineString) {
        LOGGER.info(String.format("GET /airports called with countryCodeString: '%s', typeList: '%s', airlineString: '%s'",
                countryCodeString,typeList,airlineString));
        if (countryCodeString != null) countryCodeString = ValidationUtils.validateAndGetCountryCode(true,countryCodeString, countryService);
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        Option<Airline> airline = ValidationUtils.resolveAirlineString(airlineString);
        List<Airport> response = airportsService.getAll(Option.of(countryCodeString),airportTypeList,airline);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @PostMapping(Path.Admin.AIRPORTS)
    @Caching(evict = {
            @CacheEvict(value = "iataCache", allEntries = true),
            @CacheEvict(value = "airportsCache", allEntries = true),
            @CacheEvict(value = "nearbyAirportsCache", allEntries = true)
    })
    public Airport addAirport(@RequestBody(required = false) @Valid AirportForm airportForm,
                              BindingResult bindingResult) {
        LOGGER.info("POST /airports with airportForm: {}",airportForm);
        ValidationUtils.validate(airportForm,bindingResult);
        Airport created = airportsService.createAirport(airportForm);
        LOGGER.info(String.format("response: '%s'",created));
        return created;
    }

    @GetMapping(Path.AIRPORT_BY_IATA)
    @Cacheable(value = "airportCache", key = "#iata")
    public Airport getAirportByIata(@PathVariable(ParameterNames.IATA_PARAM_NAME) String iata) {
        LOGGER.info(String.format("GET /airports/%s",iata));
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,ParameterNames.IATA_PARAM_NAME,false);
        Airport response = airportsService.getByIata(iata);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @PatchMapping(Path.Admin.AIRPORT_BY_IATA)
    @Caching(evict = {
            @CacheEvict(value = "airportCache", key = "#iata"),
            @CacheEvict(value = "iataCache", allEntries = true),
            @CacheEvict(value = "airportsCache", allEntries = true),
            @CacheEvict(value = "nearbyAirportsCache", allEntries = true)
    }, put = {
            @CachePut(value = "airportCache", key = "#iata") // Cache the updated object
    })
    public Airport patchAirportByIata(@PathVariable(ParameterNames.IATA_PARAM_NAME) String iata,
                                      @RequestBody(required = false) @Valid AirportPatch airportPatch,
                                      BindingResult bindingResult) {
        LOGGER.info(String.format("PATCH /airports/%s with airportPatch: '%s'",iata,airportPatch));
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,ParameterNames.IATA_PARAM_NAME,false);
        ValidationUtils.validateAirportPatch(airportPatch,bindingResult);
        Airport response = airportsService.patch(iata,airportPatch);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.NEARBY_AIRPORTS)
    @Cacheable("nearbyAirportsCache")
    public List<Airport> nearbyAirports(@RequestParam(ParameterNames.LATITUDE_PARAM_NAME) Double latitude,
                                        @RequestParam(ParameterNames.LONGITUDE_PARAM_NAME) Double longitude,
                                        @RequestParam(name = ParameterNames.LIMIT_PARAM_NAME,defaultValue = "5") Integer limit,
                                        @RequestParam(name = ParameterNames.TYPE_PARAM_NAME, required = false) List<String> typeList,
                                        @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false) List<String> airlineStringList) {
        LOGGER.info(String.format("GET /nearby-airports called with latitude: %f, longitude: %f, limit: %d, " +
                "typeList: '%s', airlineStringList: '%s'",latitude,longitude,limit,typeList,airlineStringList));
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = ValidationUtils.resolveAirlineStringList(airlineStringList);
        List<Airport> response = airportsService.getByDistance(latitude,longitude,limit,airportTypeList,airlineList);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }
}
