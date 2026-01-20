package org.voyager.api.controller;

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
import org.voyager.commons.model.airport.AirportQuery;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.api.model.query.IataQuery;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.CountryService;
import org.voyager.api.validate.ValidationUtils;
import org.voyager.commons.model.response.PagedResponse;
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
    public List<String> getIataCodes(@RequestParam(name = ParameterNames.TYPE,
            required = false) List<String> typeList,
                                     @RequestParam(name = ParameterNames.AIRLINE,
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
    public PagedResponse<Airport> getPagedAirports(
            @RequestParam(name = ParameterNames.COUNTRY_CODE, required = false) String countryCodeString,
            @RequestParam(name = ParameterNames.TYPE, required = false) List<String> typeList,
            @RequestParam(name = ParameterNames.AIRLINE, required = false) List<String> airlineStringList,
            @RequestParam(name = ParameterNames.SIZE, defaultValue = "100") String pageSizeString,
            @RequestParam(name = ParameterNames.PAGE, defaultValue = "0") String pageNumberString) {
        LOGGER.info("GET /airports called with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}",
                ParameterNames.COUNTRY_CODE,countryCodeString,
                ParameterNames.TYPE,typeList,
                ParameterNames.AIRLINE,airlineStringList,
                ParameterNames.SIZE,pageSizeString,
                ParameterNames.PAGE,pageNumberString);
        AirportQuery airportQuery = AirportQuery.builder().build();
        Integer pageSize = ValidationUtils.validateAndGetInteger(ParameterNames.SIZE,pageSizeString);
        Integer pageNumber = ValidationUtils.validateAndGetInteger(ParameterNames.PAGE,pageNumberString);
        airportQuery.setPage(pageNumber);
        airportQuery.setSize(pageSize);
        if (countryCodeString != null) {
            airportQuery.setCountryCode(ValidationUtils.validateAndGetCountryCode(
                    true,countryCodeString, countryService));
        }
        if (typeList != null && !typeList.isEmpty()) {
            List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
            airportQuery.setAirportTypeList(airportTypeList);
        }
        if (airlineStringList != null && !airlineStringList.isEmpty()) {
            List<Airline> airlineList = airlineStringList.stream().map(ValidationUtils::validateAndGetAirline).toList();
            airportQuery.setAirlineList(airlineList);
        }
        ValidationUtils.validate(airportQuery);
        return airportsService.getPagedAirports(airportQuery);
    }

    @GetMapping(Path.AIRPORT_BY_IATA)
    @Cacheable(value = "airportCache", key = "#iata")
    public Airport getAirportByIata(@PathVariable(ParameterNames.IATA) String iata) {
        LOGGER.info("GET /airports/{}", iata);
        iata = ValidationUtils.validateIataToUpperCase(iata,airportsService,ParameterNames.IATA,false);
        Airport response = airportsService.getByIata(iata);
        LOGGER.debug("GET airports response: '{}'", response);
        return response;
    }

    @GetMapping(Path.NEARBY_AIRPORTS)
    @Cacheable("nearbyAirportsCache")
    // TODO: add param for kilometer radius
    public List<Airport> nearbyAirports(@RequestParam(value = ParameterNames.LATITUDE,required = false)
                                            String latitudeString,
                                        @RequestParam(value = ParameterNames.LONGITUDE, required = false)
                                            String longitudeString,
                                        @RequestParam(value = ParameterNames.IATA, required = false)
                                            String iataString,
                                        @RequestParam(name = ParameterNames.LIMIT,defaultValue = "5")
                                            String limitString,
                                        @RequestParam(name = ParameterNames.TYPE, required = false)
                                            List<String> typeList,
                                        @RequestParam(name = ParameterNames.AIRLINE, required = false)
                                            List<String> airlineStringList) {
        LOGGER.info("GET /nearby-airports called with {}:{}, {}:{}, {}:{}, {}:{}, {}:{}, {}:{}",
                ParameterNames.LATITUDE,latitudeString,
                ParameterNames.LONGITUDE,longitudeString,
                ParameterNames.IATA,iataString,
                ParameterNames.LIMIT,limitString,
                ParameterNames.TYPE,typeList,
                ParameterNames.AIRLINE,airlineStringList);

        boolean hasIataParam = iataString != null;
        boolean hasLngLatParams = latitudeString != null | longitudeString != null;

        if (hasIataParam && hasLngLatParams) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Cannot specify both %s and %s/%s parameters. " +
                                    "Use %s for nearby airport OR %s/%s for nearby coordinates",
                            ParameterNames.IATA,
                            ParameterNames.LATITUDE,ParameterNames.LONGITUDE,
                            ParameterNames.IATA,
                            ParameterNames.LATITUDE,ParameterNames.LONGITUDE));
        }

        List<AirportType> airportTypeList = ValidationUtils.resolveTypeList(typeList);
        List<Airline> airlineList = ValidationUtils.resolveAirlineStringList(airlineStringList);
        int limit = ValidationUtils.validateAndGetInteger(ParameterNames.LIMIT,limitString);

        if (hasLngLatParams) {
            Double latitude = ValidationUtils.validateAndGetDouble(ParameterNames.LATITUDE, latitudeString);
            Double longitude = ValidationUtils.validateAndGetDouble(ParameterNames.LONGITUDE, longitudeString);
            List<Airport> response = airportsService.getByDistance(latitude,longitude,limit,airportTypeList,airlineList);
            LOGGER.debug("GET nearby response: '{}'", response);
            return response;
        }

        String iata = ValidationUtils.validateIataToUpperCase(iataString,airportsService,ParameterNames.IATA,true);
        return airportsService.getNearbyAirport(iata,limit,airportTypeList,airlineList);
    }
}
