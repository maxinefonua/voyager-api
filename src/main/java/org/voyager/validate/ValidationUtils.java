package org.voyager.validate;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.AirportDisplay;
import org.voyager.model.AirportType;
import org.voyager.model.location.Source;
import org.voyager.model.location.LocationForm;
import org.voyager.model.route.RouteForm;
import org.voyager.service.AirportsService;

import java.util.*;

import static org.voyager.utils.ConstantsUtils.*;

public class ValidationUtils {
    public static Option<AirportType> resolveTypeString(String typeString) {
        Option<AirportType> airportType = Option.none();
        if (StringUtils.isNotEmpty(typeString)) {
            try {
                airportType = Option.of(AirportType.valueOf(typeString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestParameterMessage(TYPE_PARAM_NAME, typeString));
            }
        }
        return airportType;
    }

    public static Option<Airline> resolveAirlineString(String airlineString) {
        Option<Airline> airline = Option.none();
        if (StringUtils.isNotEmpty(airlineString)) {
            try {
                airline = Option.of(Airline.valueOf(airlineString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestParameterMessage(AIRLINE_PARAM_NAME, airlineString));
            }
        }
        return airline;
    }

    public static Source validateAndGetSource(String sourceString) {
        if (StringUtils.isEmpty(sourceString)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(SOURCE_PROPERTY_NAME));
        try {
            return Source.valueOf(sourceString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(SOURCE_PROPERTY_NAME, sourceString));
        }
    }

    public static AirportDisplay validateAndGetIata(AirportsService airportsService, String iata, String varName, boolean isParam) {
        if (StringUtils.isNotEmpty(iata) && !iata.matches(IATA_CODE_REGEX)) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestParameterMessage(varName,iata));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidPathVariableMessage(varName,iata));
        }
        Option<AirportDisplay> result = airportsService.getByIata(iata.toUpperCase());
        if (result.isEmpty()) {
            if (isParam) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForParameterMessage(varName,iata));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForPathVariableMessage(varName,iata));
        }
        return result.get();
    }

    public static Integer validateAndGetInteger(String varName, String varVal, boolean isParam) {
        if (StringUtils.isEmpty(varVal)) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName, varVal));
        }
        try {
            return Integer.valueOf(varVal);
        } catch (IllegalArgumentException e) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName, varVal));        }
    }

    public static String validateAndGetCountryCode(String countryCodeString) {
        if (!countryCodeString.matches(COUNTRY_CODE_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(COUNTRY_CODE_PARAM_NAME, countryCodeString));
        }
        return countryCodeString.toUpperCase();
    }

    public static void validateLocationForm(LocationForm locationForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    joiner.add(String.format("'%s' %s",fieldError.getField(),fieldError.getDefaultMessage()));
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid request body: %s.",joiner));
        }
        try {
            Source.valueOf(locationForm.getSource().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstants.buildInvalidRequestBodyPropertyNoMessage(SOURCE_PROPERTY_NAME,locationForm.getSource()));
        }
        locationForm.setSource(locationForm.getSource().toUpperCase());
        // TODO: validate country
        locationForm.setCountryCode(locationForm.getCountryCode().toUpperCase());
    }

    public static void validateRouteForm(RouteForm routeForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    joiner.add(String.format("'%s' %s",fieldError.getField(),fieldError.getDefaultMessage()));
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid request body: %s.",joiner));
        }
        try {
            Airline.valueOf(routeForm.getAirline().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstants.buildInvalidRequestBodyPropertyMessage(AIRLINE_PARAM_NAME,routeForm.getAirline()));
        }
        routeForm.setAirline(routeForm.getAirline().toUpperCase());
        routeForm.setOrigin(routeForm.getOrigin().toUpperCase());
        routeForm.setDestination(routeForm.getDestination().toUpperCase());
    }
}
