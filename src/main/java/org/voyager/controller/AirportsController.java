package org.voyager.controller;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.service.AirportsService;
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
    public List<String> getIataCodes(@RequestParam(name = TYPE_PARAM_NAME, required = false) List<String> typeList) {
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        if (airportTypeList.isEmpty()) return airportsService.getIata();
        return airportsService.getIataByTypeIn(airportTypeList);
    }

    @GetMapping("/airports")
    public List<Airport> getAirports(@RequestParam(name = COUNTRY_CODE_PARAM_NAME, required = false) String countryCodeString,
                                            @RequestParam(name = TYPE_PARAM_NAME, required = false) List<String> typeList,
                                            @RequestParam(name = AIRLINE_PARAM_NAME, required = false) String airlineString) {
        if (countryCodeString != null) countryCodeString = ValidationUtils.validateAndGetCountryCode(countryCodeString);
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        Option<Airline> airline = ValidationUtils.resolveAirlineString(airlineString);
        return airportsService.getAll(Option.of(countryCodeString),airportTypeList,airline);
    }

    @GetMapping("/airports/{iata}")
    public Airport getAirportByIata(@PathVariable(IATA_PARAM_NAME) String iata) {
        LOGGER.debug(String.format("fetching uncached airport by iata code: %s",iata));
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,IATA_PARAM_NAME,false);
        return airportsService.getByIata(iata);
    }

    @PatchMapping("/airports/{iata}")
    public Airport patchAirportByIata(@PathVariable(IATA_PARAM_NAME) String iata, @RequestBody(required = false) @Valid AirportPatch airportPatch, BindingResult bindingResult) {
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,IATA_PARAM_NAME,false);
        ValidationUtils.validateAirportPatch(airportPatch,bindingResult);
        LOGGER.debug(String.format("patching airport at iata '%s' with patch: %s",iata,airportPatch));
        return airportsService.patch(iata,airportPatch);
    }

    @GetMapping("/nearby-airports")
    public List<Airport> nearbyAirports(@RequestParam(LATITUDE_PARAM_NAME) Double latitude,
                                        @RequestParam(LONGITUDE_PARAM_NAME) Double longitude,
                                        @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "5") Integer limit,
                                        @RequestParam(name = TYPE_PARAM_NAME, required = false) List<String> typeList,
                                        @RequestParam(name = AIRLINE_PARAM_NAME, required = false) List<String> airlineStringList) {
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = ValidationUtils.resolveAirlineStringList(airlineStringList);
        return airportsService.getByDistance(latitude,longitude,limit,airportTypeList,airlineList);
    }
}
