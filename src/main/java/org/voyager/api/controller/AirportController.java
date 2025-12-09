package org.voyager.api.controller;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.api.model.query.IataQuery;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import java.util.List;

@RestController
public class AirportController {
    @Autowired
    AirportsService airportsService;

    @Autowired
    CountryService countryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportController.class);

    @GetMapping(Path.IATA)
    @Cacheable("iataCache")
    public List<String> getIataCodes(@RequestParam(name = ParameterNames.TYPE_PARAM_NAME,
            required = false) List<String> typeList,
                                     @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME,
                                             required = false) List<String> airlineStringList) {
        LOGGER.info("GET /iata called with typeList: {}, airlineList: {}", typeList, airlineStringList);
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
        LOGGER.debug("GET iata response: '{}'", response);
        return response;
    }

    @GetMapping(Path.AIRPORTS)
    @Cacheable("airportsCache")
    public List<Airport> getAirports(@RequestParam(name = ParameterNames.COUNTRY_CODE_PARAM_NAME, required = false) String countryCodeString,
                                     @RequestParam(name = ParameterNames.TYPE_PARAM_NAME, required = false) List<String> typeList,
                                     @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false) List<String> airlineStringList) {
        LOGGER.info("GET /airports called with countryCodeString: '{}', typeList: '{}', airlineStringList: '{}'", countryCodeString, typeList, airlineStringList);
        if (countryCodeString != null) countryCodeString = ValidationUtils.validateAndGetCountryCode(true,countryCodeString, countryService);
        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = List.of();
        if (airlineStringList != null && !airlineStringList.isEmpty()) {
            airlineList = airlineStringList.stream().map(ValidationUtils::validateAndGetAirline).toList();
        }
        List<Airport> response = airportsService.getAll(Option.of(countryCodeString),airportTypeList,airlineList);
        LOGGER.debug("response: '{}'", response);
        return response;
    }


    @GetMapping(Path.AIRPORT_BY_IATA)
    @Cacheable(value = "airportCache", key = "#iata")
    public Airport getAirportByIata(@PathVariable(ParameterNames.IATA_PARAM_NAME) String iata) {
        LOGGER.info("GET /airports/{}", iata);
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,ParameterNames.IATA_PARAM_NAME,false);
        Airport response = airportsService.getByIata(iata);
        LOGGER.debug("GET airports response: '{}'", response);
        return response;
    }

    @GetMapping(Path.NEARBY_AIRPORTS)
    @Cacheable("nearbyAirportsCache")
    // TODO: add param for kilometer radius
    public List<Airport> nearbyAirports(@RequestParam(value = ParameterNames.LATITUDE_PARAM_NAME,required = false)
                                            String latitudeString,
                                        @RequestParam(value = ParameterNames.LONGITUDE_PARAM_NAME, required = false)
                                            String longitudeString,
                                        @RequestParam(value = ParameterNames.IATA_PARAM_NAME, required = false)
                                            String iataString,
                                        @RequestParam(name = ParameterNames.LIMIT_PARAM_NAME,defaultValue = "5")
                                            String limitString,
                                        @RequestParam(name = ParameterNames.TYPE_PARAM_NAME, required = false)
                                            List<String> typeList,
                                        @RequestParam(name = ParameterNames.AIRLINE_PARAM_NAME, required = false)
                                            List<String> airlineStringList) {
        LOGGER.info("GET /nearby-airports called with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}",
                ParameterNames.LATITUDE_PARAM_NAME,latitudeString,
                ParameterNames.LONGITUDE_PARAM_NAME,longitudeString,
                ParameterNames.IATA_PARAM_NAME,iataString,
                ParameterNames.LIMIT_PARAM_NAME,limitString,
                ParameterNames.TYPE_PARAM_NAME,typeList,
                ParameterNames.AIRLINE_PARAM_NAME,airlineStringList);

        boolean hasIataParam = iataString != null;
        boolean hasLngLatParams = latitudeString != null | longitudeString != null;

        if (hasIataParam && hasLngLatParams) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Cannot specify both %s and %s/%s parameters. " +
                                    "Use %s for nearby airport OR %s/%s for nearby coordinates",
                            ParameterNames.IATA_PARAM_NAME,
                            ParameterNames.LATITUDE_PARAM_NAME,ParameterNames.LONGITUDE_PARAM_NAME,
                            ParameterNames.IATA_PARAM_NAME,
                            ParameterNames.LATITUDE_PARAM_NAME,ParameterNames.LONGITUDE_PARAM_NAME));
        }

        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = ValidationUtils.resolveAirlineStringList(airlineStringList);
        int limit = ValidationUtils.validateAndGetInteger(ParameterNames.LIMIT_PARAM_NAME,limitString);

        if (hasLngLatParams) {
            Double latitude = ValidationUtils.validateAndGetDouble(ParameterNames.LATITUDE_PARAM_NAME, latitudeString);
            Double longitude = ValidationUtils.validateAndGetDouble(ParameterNames.LONGITUDE_PARAM_NAME, longitudeString);
            List<Airport> response = airportsService.getByDistance(latitude,longitude,limit,airportTypeList,airlineList);
            LOGGER.debug("GET nearby response: '{}'", response);
            return response;
        }

        String iata = ValidationUtils.validateIataToUpperCase(iataString,airportsService,ParameterNames.IATA_PARAM_NAME,true);
        return airportsService.getNearbyAirport(iata,limit,airportTypeList,airlineList);
    }
}
