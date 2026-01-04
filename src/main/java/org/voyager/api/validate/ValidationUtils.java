package org.voyager.api.validate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.service.AirportsService;
import org.voyager.api.service.CountryService;
import org.voyager.api.service.FlightService;
import org.voyager.api.service.RouteService;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Regex;
import org.voyager.api.error.MessageConstants;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.commons.model.geoname.fields.FeatureClass;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.commons.model.route.Status;
import org.voyager.commons.validate.annotations.ValidEnum;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.voyager.api.error.MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;

public class ValidationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);

    public static List<AirportType> resolveTypeList(List<String> typeList) {
        List<AirportType> airportTypeList = new ArrayList<>();
        if (typeList != null) {
            for (String typeString : typeList) {
                try {
                    airportTypeList.add(AirportType.valueOf(typeString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.TYPE_PARAM_NAME, typeString));
                }
            }
        }
        return airportTypeList;
    }

    public static List<Airline> resolveAirlineStringList(List<String> airlineStringList) {
        List<Airline> airlineList = new ArrayList<>();
        if (airlineStringList != null) {
            for (String airlineString : airlineStringList) {
                try {
                    airlineList.add(Airline.valueOf(airlineString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.TYPE_PARAM_NAME,airlineString));
                }
            }
        }
        return airlineList;
    }

    public static Boolean validateAndGetBoolean(String paramName, String paramValue) {
        if (StringUtils.isEmpty(paramValue)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(paramName));
        try {
            return Boolean.valueOf(paramValue.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(paramName, paramValue));
        }
    }

    public static List<Status> validateAndGetStatusList(String paramName, List<String> statusStringList) {
        if (statusStringList == null || statusStringList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildMissingRequestParameterMessage(paramName));
        }
        return statusStringList.stream().map(statusString->{
            try{
                return Status.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildInvalidRequestParameterMessage(paramName,statusString));
            }
        }).toList();
    }

    public static Status validateAndGetStatus(String paramName, String paramValue) {
        if (StringUtils.isEmpty(paramValue)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(paramName));
        try {
            return Status.valueOf(paramValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(paramName, paramValue));
        }
    }

    public static FeatureClass validateAndGetFeatureClass(String paramName, String paramValue) {
        if (StringUtils.isEmpty(paramValue)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(paramName));
        try {
            return FeatureClass.valueOf(paramValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(paramName, paramValue));
        }
    }

    public static void validateQuery(String query) {
        if (StringUtils.isEmpty(query)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(ParameterNames.QUERY_PARAM_NAME));
    }

    public static Airline validateAndGetAirline(String airlineString) {
        if (StringUtils.isEmpty(airlineString)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                MessageConstants.buildMissingRequestParameterMessage(ParameterNames.AIRLINE_PARAM_NAME));
        try {
            return Airline.valueOf(airlineString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.AIRLINE_PARAM_NAME,airlineString));
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

    public static List<String> validateIataCodeList(String paramName, List<String> iataCodeList, AirportsService airportsService) {
        if (iataCodeList == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildMissingRequestParameterMessage(paramName));
        }
        if (iataCodeList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildEmptyRequestParameterList(paramName));
        }
        List<String> validatedList = new ArrayList<>();
        iataCodeList.forEach(iata -> validatedList.add(
                validateIataToUpperCase(iata,airportsService,paramName,true)));
        return validatedList;
    }

    public static String validateIataToUpperCase(String iata, AirportsService airportsService, String varName, boolean isParam) {
        if (StringUtils.isBlank(iata)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildMissingRequestParameterMessage(varName));
        }
        if (!iata.matches(Regex.AIRPORT_CODE_CASE_INSENSITIVE)) {
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

    public static Double validateAndGetDouble(String varName, String varVal) {
        if (StringUtils.isEmpty(varVal)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
        }
        try {
            return Double.valueOf(varVal);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
        }
    }

    public static Long validateAndGetLong(String varName, String varVal, boolean isParam) {
        if (StringUtils.isEmpty(varVal)) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName, varVal));
        }
        try {
            return Long.valueOf(varVal);
        } catch (IllegalArgumentException e) {
            if (isParam) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidPathVariableMessage(varName, varVal));
        }
    }

    public static Integer validateAndGetInteger(String varName, String varVal) {
        if (StringUtils.isEmpty(varVal)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
        }
        try {
            return Integer.valueOf(varVal);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
        }
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
                    MessageConstants.buildInvalidPathVariableMessage(varName, varVal));
        }
    }

    public static SearchOperator validateAndGetOperator(String operatorString) {
        try {
            return SearchOperator.valueOf(operatorString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.OPERATOR,operatorString));
        }
    }

    public static String validateAndGetFlightNumber(String flightNumberString, FlightService flightService) {
        if (!flightNumberString.matches(Regex.FLIGHT_NUMBER)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.FLIGHT_NUMBER_PARAM_NAME,
                            flightNumberString));
        }
        if (!flightService.existsByFlightNumber(flightNumberString)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildResourceNotFoundForParameterMessage(ParameterNames.FLIGHT_NUMBER_PARAM_NAME,
                            flightNumberString));
        }
        return flightNumberString;
    }

    public static String validateAndGetCountryCode(boolean isParam, String countryCodeString, CountryService countryService) {
        if (!countryCodeString.matches(Regex.COUNTRY_CODE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.COUNTRY_CODE_PARAM_NAME, countryCodeString));
        }
        if (!countryService.countryCodeExists(countryCodeString.toUpperCase())) {
            if (isParam)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.COUNTRY_CODE_PARAM_NAME, countryCodeString));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    MessageConstants.buildInvalidPathVariableMessage(ParameterNames.COUNTRY_CODE_PARAM_NAME, countryCodeString));
        }
        return countryCodeString.toUpperCase();
    }

    public static void validateRouteForm(RouteForm routeForm, BindingResult bindingResult) {
        processRequestBodyBindingErrors(routeForm,bindingResult);
        routeForm.setOrigin(routeForm.getOrigin().toUpperCase());
        routeForm.setDestination(routeForm.getDestination().toUpperCase());
    }

    public static void validateCountryForm(CountryForm countryForm, BindingResult bindingResult,
                                           CountryService countryService) {
        processRequestBodyBindingErrors(countryForm,bindingResult);
        if (countryService.countryCodeExists(countryForm.getCountryCode()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("%s country code already exists",
                    countryForm.getCountryCode()));
    }

    public static void validateAirportPatch(AirportPatch airportPatch, BindingResult bindingResult) {
        processRequestBodyBindingErrors(airportPatch,bindingResult);
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
        if (fieldError.getField().equals(ParameterNames.SOURCE_PARAM_NAME)) {
            return String.format("'%s' %s but has invalid value %s",
                    fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());
        }

        Optional<Field> fieldOptional = Arrays.stream(requestBody.getClass().getDeclaredFields()).filter(
                field -> fieldError.getField().equals(field.getName())).findAny();
        if (fieldOptional.isEmpty()) {
            return String.format("'%s' %s but has invalid value '%s'",
                    fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());
        }

        Field field = fieldOptional.get();
        ValidEnum validEnum = field.getAnnotation(ValidEnum.class);
        if (validEnum == null) {
            if (fieldError.getRejectedValue() != null && fieldError.getRejectedValue() instanceof Collection<?>) {
                return String.format("'%s' %s but there are elements with invalid values '%s'",
                        fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());
            }
            return String.format("'%s' %s but has invalid value '%s'",
                    fieldError.getField(),fieldError.getDefaultMessage(),fieldError.getRejectedValue());
        }

        StringJoiner valueJoiner = new StringJoiner(",");
        Arrays.stream(validEnum.enumClass().getEnumConstants()).forEach(value ->
                valueJoiner.add(String.format("'%s'",value.name().toLowerCase())));
        return (String.format("'%s' accepts values [%s] but has invalid value '%s'",
                fieldError.getField(),valueJoiner,fieldError.getRejectedValue()));
    }

    public static Integer validateAndGetRouteId(String routeIdString, RouteService routeService) {
        try {
            Integer routeId = Integer.valueOf(routeIdString);
            if (!routeService.existsById(routeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        MessageConstants.buildResourceNotFoundForParameterMessage(ParameterNames.ROUTE_ID_PARAM_NAME,routeIdString));
            }
            return routeId;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.ROUTE_ID_PARAM_NAME, routeIdString));
        }
    }

    public static ZoneId validateAndGetZoneId(String zoneIdString) {
        if (StringUtils.isBlank(zoneIdString)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildMissingRequestParameterMessage(ParameterNames.ZONE_ID));
        }
        try {
            return ZoneId.of(zoneIdString);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.ZONE_ID, zoneIdString));
        }
    }

    public static LocalDate validateAndGetLocalDate(String onDayString) {
        try {
            return LocalDate.parse(onDayString);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.ON_DAY, onDayString));
        }
    }

    public static ZonedDateTime validateAndGetZDT(String varName, String varVal) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
            return ZonedDateTime.parse(varVal, formatter);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MessageConstants.buildInvalidRequestParameterMessage(varName, varVal));
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
                                MessageConstants.buildInvalidRequestParameterMessage(ParameterNames.CONTINENT_PARAM_NAME,continentString));
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
                    INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
    }

    public static void validateAirlineBatchUpsert(AirlineBatchUpsert airlineBatchUpsert,
                                                  BindingResult bindingResult) {
        processRequestBodyBindingErrors(airlineBatchUpsert,bindingResult);
        airlineBatchUpsert.setIataList(airlineBatchUpsert.getIataList().stream()
                .map(String::toUpperCase).toList());
        airlineBatchUpsert.setAirline(airlineBatchUpsert.getAirline().toUpperCase());
    }

    public static <T> void validate(T object, BindingResult bindingResult) {
        processRequestBodyBindingErrors(object,bindingResult);
    }
}
