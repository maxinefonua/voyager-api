package org.voyager.controller;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.Airline;
import org.voyager.model.AirportDisplay;
import org.voyager.model.AirportType;
import org.voyager.service.AirportsService;
import org.voyager.service.RouteService;
import org.voyager.validate.ValidationUtils;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;
import static org.voyager.utils.ConstantsUtils.AIRLINE_PARAM_NAME;

@RestController
public class AirportsController {
    @Autowired
    private AirportsService airportsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsController.class);

    @GetMapping("/iata")
    @Cacheable("iataCodesCache")
    public List<String> getIataCodes(@RequestParam(name = TYPE_PARAM_NAME, required = false) String typeString) {
        Option<AirportType> typeOptional = ValidationUtils.resolveTypeString(typeString);
        if (typeOptional.isEmpty()) return airportsService.getIata();
        return airportsService.getIataByType(typeOptional.get());
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

    @GetMapping("/airports/{iata}")
    @Cacheable("iataCache")
    public AirportDisplay getAirportByIata(@PathVariable(IATA_PARAM_NAME) String iata) {
        LOGGER.debug(String.format("fetching uncached airport by iata code: %s",iata));
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,IATA_PARAM_NAME,false);
        return airportsService.getByIata(iata);
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
}
