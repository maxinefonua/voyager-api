package org.voyager.validate;

import io.vavr.control.Option;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.location.Source;
import org.voyager.model.location.LocationForm;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.AirportsService;
import org.voyager.service.RouteService;

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

    public static String validateIataToUpperCase(String iata, RouteService routeService, String varName, boolean isParam) {
        if (!iata.matches(IATA_CODE_REGEX)) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName,iata));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName,iata));
        }
        if (varName.equals(ORIGIN_PARAM_NAME)) {
            if (!routeService.originExists(iata.toUpperCase())) {
                if (isParam) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        MessageConstants.buildResourceNotFoundForParameterMessage(varName, iata));
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        MessageConstants.buildResourceNotFoundForPathVariableMessage(varName, iata));
            }
        } else if (varName.equals(DESTINATION_PARAM_NAME)) {
            if (!routeService.destinationExists(iata.toUpperCase())) {
                if (isParam) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        MessageConstants.buildResourceNotFoundForParameterMessage(varName, iata));
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        MessageConstants.buildResourceNotFoundForPathVariableMessage(varName, iata));
            }
        }
        return iata.toUpperCase();
    }

    public static String validateIataToUpperCase(String iata, AirportsService airportsService, String varName, boolean isParam) {
        if (!iata.matches(IATA_CODE_REGEX)) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName,iata));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName,iata));
        }
        if (!airportsService.ifIataExists(iata.toUpperCase())) {
            if (isParam) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForParameterMessage(varName,iata));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForPathVariableMessage(varName,iata));
        }
        return iata.toUpperCase();
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

    public static void validateDeltaForm(@Valid @NotNull DeltaForm deltaForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    joiner.add(String.format("'%s' %s",fieldError.getField(),fieldError.getDefaultMessage()));
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid request body: %s.",joiner));
        }
        deltaForm.setIata(deltaForm.getIata().toUpperCase());
        deltaForm.setStatus(deltaForm.getStatus().toUpperCase());
        try {
            DeltaStatus.valueOf(deltaForm.getStatus());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstants.buildInvalidRequestBodyPropertyMessage(DELTA_STATUS_PARAM_NAME,deltaForm.getStatus()));
        }
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

    public static void validateRoutePatch(@Valid @NotNull RoutePatch routePatch, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    joiner.add(String.format("'%s' %s",fieldError.getField(),fieldError.getDefaultMessage()));
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid request body: %s.",joiner));
        }
    }

    public static void validateDeltaPatch(@Valid @NotNull DeltaPatch deltaPatch, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    joiner.add(String.format("'%s' %s",fieldError.getField(),fieldError.getDefaultMessage()));
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid request body: %s.",joiner));
        }
    }

    public static List<DeltaStatus> resolveDeltaStatusList(List<String> statusStringList) {
        List<DeltaStatus> statusList = new ArrayList<>();
        statusStringList.forEach(statusString -> {
            try {
                statusList.add(DeltaStatus.valueOf(statusString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstants.buildInvalidRequestParameterMessage(DELTA_STATUS_PARAM_NAME,statusString));
            }
        });
        return statusList;
    }

    public static void validateIataAndAirportPatch(String iata, AirportPatch airportPatch, AirportsService airportsService, String varName) {
        if (!iata.matches(IATA_CODE_REGEX)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildInvalidPathVariableMessage(varName,iata));
        if (!airportsService.ifIataExists(iata.toUpperCase())) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                MessageConstants.buildResourceNotFoundForPathVariableMessage(varName,iata));
        if (airportPatch == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                ("Invalid request body. A PATCH request to this path requires a non-null request body."));
        if (StringUtils.isBlank(airportPatch.getName()) && StringUtils.isBlank(airportPatch.getCity())
                && StringUtils.isBlank(airportPatch.getSubdivision()) && StringUtils.isBlank(airportPatch.getType())
                && airportPatch.getLongitude() == null && airportPatch.getLatitude() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    ("Invalid request body. A PATCH request to this path requires at least one non-null field to update."));
        if (StringUtils.isNotBlank(airportPatch.getType())) {
            try {
                AirportType.valueOf(airportPatch.getType().toUpperCase());
                airportPatch.setType(airportPatch.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestBodyPropertyMessage(TYPE_PARAM_NAME, airportPatch.getType()));
            }
        }
    }
}
