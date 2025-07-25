package org.voyager.validate;

import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.error.MessageConstants;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.country.Continent;
import org.voyager.model.country.CountryForm;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.model.location.LocationPatch;
import org.voyager.model.location.Source;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.Status;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.model.validate.ValidEnum;
import org.voyager.service.AirportsService;
import org.voyager.service.CountryService;
import org.voyager.service.RouteService;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static org.voyager.utils.ConstantsUtils.*;

public class ValidationUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);


    public static List<AirportType> resolveTypeList(List<String> typeList) {
        List<AirportType> airportTypeList = new ArrayList<>();
        if (typeList != null) {
            for (String typeString : typeList) {
                try {
                    airportTypeList.add(AirportType.valueOf(typeString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            MessageConstants.buildInvalidRequestParameterMessage(TYPE_PARAM_NAME, typeString));
                }
            }
        }
        return airportTypeList;
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

    public static List<Airline> resolveAirlineStringList(List<String> airlineStringList) {
        List<Airline> airlineList = new ArrayList<>();
        if (airlineStringList != null) {
            for (String airlineString : airlineStringList) {
                try {
                    airlineList.add(Airline.valueOf(airlineString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            MessageConstants.buildInvalidRequestParameterMessage(TYPE_PARAM_NAME,airlineString));
                }
            }
        }
        return airlineList;
    }

    public static Option<Source> resolveSourceString(String sourceString) {
        if (StringUtils.isEmpty(sourceString)) return Option.none();
        try {
            return Option.of(Source.valueOf(sourceString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(SOURCE_PROPERTY_NAME, sourceString));
        }
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

    public static Status validateAndGetLocationStatus(String statusString) {
        try {
            return Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(LOCATION_STATUS_PARAM_NAME, statusString));
        }
    }

    public static Set<String> validateIataCodeSet(String paramName, Set<String> iataCodeList, AirportsService airportsService) {
        if (iataCodeList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(paramName,""));
        }
        Set<String> validatedSet = new HashSet<>();
        iataCodeList.forEach(iata -> validatedSet.add(
                validateIataToUpperCase(iata,airportsService,paramName,true)));
        return validatedSet;
    }

    public static List<String> validateIataCodeList(String paramName, Set<String> iataCodeList, AirportsService airportsService) {
        if (iataCodeList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(paramName,""));
        }
        List<String> validatedList = new ArrayList<>();
        iataCodeList.forEach(iata -> validatedList.add(
                validateIataToUpperCase(iata,airportsService,paramName,true)));
        return validatedList;
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
            // TODO: return max value of 10, min value of 1
            return Integer.valueOf(varVal);
        } catch (IllegalArgumentException e) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName, varVal));
        }
    }

    public static List<String> validateAndGetCountryCodeList(List<String> countryCodeList, CountryService countryService) {
        if (countryCodeList == null) return List.of();
        countryCodeList.replaceAll(countryCode -> validateAndGetCountryCode(true,countryCode,countryService));
        return countryCodeList;
    }

    public static String validateAndGetCountryCode(boolean isParam, String countryCodeString, CountryService countryService) {
        if (!countryCodeString.matches(COUNTRY_CODE_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(COUNTRY_CODE_PARAM_NAME, countryCodeString));
        }
        if (!countryService.countryCodeExists(countryCodeString.toUpperCase())) {
            if (isParam)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(COUNTRY_CODE_PARAM_NAME, countryCodeString));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildInvalidPathVariableMessage(COUNTRY_CODE_PARAM_NAME, countryCodeString));
        }
        return countryCodeString.toUpperCase();
    }

    public static void validateLocationForm(LocationForm locationForm, BindingResult bindingResult,AirportsService airportsService) {
        processRequestBodyBindingErrors(locationForm,bindingResult);
        locationForm.setAirports(getValidatedLocationAirports(locationForm.getAirports(),airportsService));
        locationForm.setCountryCode(locationForm.getCountryCode().toUpperCase());
    }

    public static void validateRouteForm(RouteForm routeForm, BindingResult bindingResult) {
        processRequestBodyBindingErrors(routeForm,bindingResult);
        routeForm.setOrigin(routeForm.getOrigin().toUpperCase());
        routeForm.setDestination(routeForm.getDestination().toUpperCase());
    }

    public static void validateFlightForm(FlightForm flightForm, BindingResult bindingResult) {
        processRequestBodyBindingErrors(flightForm,bindingResult);
        flightForm.setAirline(flightForm.getAirline().toUpperCase());
    }

    public static void validateCountryForm(CountryForm countryForm, BindingResult bindingResult,
                                           CountryService countryService) {
        processRequestBodyBindingErrors(countryForm,bindingResult);
        if (countryService.countryCodeExists(countryForm.getCountryCode()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("%s country code already exists",
                    countryForm.getCountryCode()));
    }

    public static void validateFlightPatch(FlightPatch flightPatch, BindingResult bindingResult) {
        processRequestBodyBindingErrors(flightPatch,bindingResult);
    }

    public static void validateLocationPatch(LocationPatch locationPatch, BindingResult bindingResult, AirportsService airportsService) {
        processRequestBodyBindingErrors(locationPatch,bindingResult);
        if (locationPatch.getAirports() != null) locationPatch.setAirports(getValidatedLocationAirports(locationPatch.getAirports(),airportsService));
        if (locationPatch.getStatus() != null) locationPatch.setStatus(locationPatch.getStatus().toUpperCase());
    }

    public static void validateAirportPatch(AirportPatch airportPatch, BindingResult bindingResult) {
        processRequestBodyBindingErrors(airportPatch,bindingResult);
    }

    private static List<String>  getValidatedLocationAirports(List<String> airports, AirportsService airportsService) {
        List<String> unique = new ArrayList<>();
        for (String iata : airports) {
            if (iata == null || !iata.matches(IATA_CODE_REGEX)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestBodyPropertyMessage(AIRPORTS_PROPERTY_NAME,iata));
            if (!airportsService.ifIataExists(iata.toUpperCase())) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildResourceNotFoundForPathVariableMessage(AIRPORTS_PROPERTY_NAME,iata));
            if (!unique.contains(iata)) unique.add(iata.toUpperCase());
        }
        return unique;
    }

    private static void processRequestBodyBindingErrors(Object requestBody, BindingResult bindingResult){
        if (requestBody == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required request body missing");
        if (bindingResult.hasErrors()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    if (fieldError.getRejectedValue() == null)
                        joiner.add(String.format("'%s' %s",fieldError.getField(),fieldError.getDefaultMessage()));
                    else
                        joiner.add(resolveInvalidInputErrors(fieldError,requestBody));
                } else joiner.add(error.getDefaultMessage());

            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid request body: %s",joiner));
        }
    }

    private static String resolveInvalidInputErrors(FieldError fieldError, Object requestBody) {
        if (fieldError.getField().equals(SOURCE_PROPERTY_NAME)) return String.format("'%s' %s but has invalid value '%s'",
                fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());

        Optional<Field> fieldOptional = Arrays.stream(requestBody.getClass().getDeclaredFields()).filter(
                field -> fieldError.getField().equals(field.getName())).findAny();
        if (fieldOptional.isEmpty()) return String.format("'%s' %s but has invalid value '%s'",
                fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());

        Field field = fieldOptional.get();
        ValidEnum validEnum = field.getAnnotation(ValidEnum.class);
        if (validEnum == null) return String.format("'%s' %s but has invalid value '%s'",
                fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());

        StringJoiner valueJoiner = new StringJoiner(",");
        Arrays.stream(validEnum.enumClass().getEnumConstants()).forEach(value -> {
            valueJoiner.add(String.format("'%s'",value.name().toLowerCase()));
        });
        return (String.format("'%s' accepts values [%s] but has invalid value '%s'",
                fieldError.getField(),valueJoiner,fieldError.getRejectedValue()));
    }

    public static Integer resolveRouteId(String routeIdString, RouteService routeService) {
        try {
            Integer routeId = Integer.valueOf(routeIdString);
            Option<Route> routeOption = routeService.getRouteById(routeId);
            if (routeOption.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildResourceNotFoundForParameterMessage(ROUTE_ID_PARAM_NAME,routeIdString));
            }
            return routeId;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ROUTE_ID_PARAM_NAME, routeIdString));
        }
    }

    public static List<Continent> resolveContinentStringList(List<String> continentStringList) {
        return handleNullPointerExceptions(() -> {
            if (continentStringList == null) return List.of();
            Set<Continent> continentSet = new HashSet<>();
            for (String continentString : continentStringList) {
                try {
                    continentSet.add(Continent.valueOf(continentString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    try {
                        continentSet.add(Continent.fromDisplayText(continentString));
                    } catch (IllegalArgumentException illegalArgumentException) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                MessageConstants.buildInvalidRequestParameterMessage(CONTINENT_PARAM_NAME,continentString));
                    }
                }
            }
            return new ArrayList<>(continentSet);
        });
    }

    public static void validateRoutePatch(RoutePatch routePatch, BindingResult bindingResult) {
        processRequestBodyBindingErrors(routePatch,bindingResult);
    }

    public static <T> T handleNullPointerExceptions(Supplier<T> repositoryFunction) {
        try {
            return repositoryFunction.get();
        } catch (NullPointerException nullPointerException) {
            LOGGER.error(nullPointerException.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An internal service error has occured. Alerting yet to be implemented.");
        }
    }

    public static Option<Status> resolveStatusString(String statusString) {
        if (StringUtils.isBlank(statusString)) return Option.none();
        try {
            return Option.of(Status.valueOf(statusString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(LOCATION_STATUS_PARAM_NAME, statusString));
        }
    }
}
