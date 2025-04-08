package org.voyager.validate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.AirportType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.voyager.error.MessageConstants.VALID_IATA_CONSTRAINT;
import static org.voyager.utils.ConstantsUtils.TYPE_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.AIRLINE_PARAM_NAME;

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

    public static void validateIataCode(String iata, List<String> airportsServiceIata) {
        Set<String> validCodes = new HashSet<>(airportsServiceIata);
        if (!validCodes.contains(iata.toUpperCase())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildInvalidPathVariableMessage(iata.toUpperCase(),VALID_IATA_CONSTRAINT));
    }
}
