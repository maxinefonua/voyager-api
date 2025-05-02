package org.voyager.validate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.AirportType;
import org.voyager.model.location.Source;
import org.voyager.model.location.LocationForm;

import java.util.*;

import static org.voyager.error.MessageConstants.VALID_IATA_CONSTRAINT;
import static org.voyager.utils.ConstantsUtils.*;

public class ValidationUtils {
    public static Optional<AirportType> resolveTypeOptional(Optional<String> typeOptional) {
        Optional<AirportType> airportType = Optional.empty();
        String type = typeOptional.orElse(null);
        if (StringUtils.isNotEmpty(type)) {
            try {
                airportType = Optional.of(AirportType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestParameterMessage(TYPE_PARAM_NAME, type));
            }
        }
        return airportType;
    }

    public static Optional<Airline> resolveAirlineOptional(Optional<String> airlineOptional) {
        Optional<Airline> airline = Optional.empty();
        String airlineText = airlineOptional.orElse(null);
        if (StringUtils.isNotEmpty(airlineText)) {
            try {
                airline = Optional.of(Airline.valueOf(airlineText.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestParameterMessage(AIRLINE_PARAM_NAME, airlineText));
            }
        }
        return airline;
    }

    public static Source resolveSourceOptional(Optional<String> sourceOptional) {
        if (sourceOptional.isEmpty() || StringUtils.isEmpty(sourceOptional.get())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(SOURCE_PROPERTY_NAME));
        String sourceText = sourceOptional.get();
        try {
            Source source = Source.valueOf(sourceText.toUpperCase());
            return source;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(SOURCE_PROPERTY_NAME, sourceText));
        }
    }

    public static void validateIataCode(String iata, List<String> airportsServiceIata) {
        Set<String> validCodes = new HashSet<>(airportsServiceIata);
        if (!validCodes.contains(iata.toUpperCase())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildInvalidPathVariableMessage(iata.toUpperCase(),VALID_IATA_CONSTRAINT));
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
}
