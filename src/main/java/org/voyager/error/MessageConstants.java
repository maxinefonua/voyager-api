package org.voyager.error;

import java.util.Map;

import static org.voyager.utils.ConstantsUtils.*;

public class MessageConstants {
    private static final String INVALID_PATH_VARIABLE = "Invalid path variable '%s'. %s";
    private static final String INVALID_REQUEST_PARAM = "Invalid request parameter '%s' with value '%s'. %s";
    private static final String MISSING_REQUEST_PARAM = "Missing request parameter '%s'. %s";
    private static final String INVALID_REQUEST_BODY_PROPERTY = "Invalid request body property '%s' with value '%s'. %s";
    private static final String INVALID_REQUEST_BODY_PROPERTY_NO_MESSAGE = "Invalid request body property '%s' with value '%s'";
    private static final String GET_GEONAMEID_ERROR = "Error fetching feature details for query result '%s' with geonameId '%s'.";
    private static final String REPOSITORY_SAVE_ERROR = "Exception thrown while trying to save %s. Please consult with API docs to ensure request body is of a valid %s.";

    public static String buildRespositorySaveErrorMessage(String entityName){
        return String.format(REPOSITORY_SAVE_ERROR,entityName,entityName);
    }

    public static String buildGetGeonameErrorMessage(String geonameName, Long geonameId){
        return String.format(GET_GEONAMEID_ERROR,geonameName,geonameId);
    }
    public static String buildInvalidPathVariableMessage(String varValue, String validConstraints){
        return String.format(INVALID_PATH_VARIABLE,varValue, validConstraints);
    }

    public static String buildInvalidRequestParameterMessage(String paramName, String paramValue){
        return String.format(INVALID_REQUEST_PARAM,paramName,paramValue,PARAM_TO_CONSTRAINT_MAP.get(paramName));
    }

    public static String buildMissingRequestParameterMessage(String paramName){
        return String.format(MISSING_REQUEST_PARAM,paramName,MISSING_SOURCE_CONSTRAINT);
    }

    public static String buildInvalidRequestBodyPropertyMessage(String propertyName, String propertyValue){
        return String.format(INVALID_REQUEST_BODY_PROPERTY,propertyName,propertyValue,REQUEST_BODY_PROPERTY_TO_CONSTRAINT_MAP.get(propertyName));
    }


    public static String buildInvalidRequestBodyPropertyNoMessage(String propertyName, String propertyValue){
        return String.format(INVALID_REQUEST_BODY_PROPERTY_NO_MESSAGE,propertyName,propertyValue);
    }

    public static final String VALID_IATA_CONSTRAINT = "Must be a valid three-letter IATA code";
    private static final String VALID_COUNTRY_CODE_CONSTRAINT = "Must be a valid two-letter ISO 3166-1 alpha-2 country code";
    private static final String VALID_TYPE_CONSTRAINT = "Valid airport types are: 'civil', 'military', 'historical', 'other'";
    private static final String VALID_AIRLINE_CONSTRAINT = "Currently available airlines are: 'delta'";
    private static final String MISSING_SOURCE_CONSTRAINT = "Must also include 'source' parameter when providing a 'sourceId'";
    private static final String VALID_SOURCE_PROPERTY_CONSTRAINT = "Valid sources are: 'GEONAMES','NOMINATIM','PHOTON','MANUAL'";

    private static final Map<String,String> PARAM_TO_CONSTRAINT_MAP = Map.ofEntries(
            Map.entry(COUNTRY_CODE_PARAM_NAME,VALID_COUNTRY_CODE_CONSTRAINT),
            Map.entry(AIRLINE_PARAM_NAME,VALID_AIRLINE_CONSTRAINT),
            Map.entry(TYPE_PARAM_NAME,VALID_TYPE_CONSTRAINT),
            Map.entry(SOURCE_PROPERTY_NAME,VALID_SOURCE_PROPERTY_CONSTRAINT)
    );
    private static final Map<String,String> REQUEST_BODY_PROPERTY_TO_CONSTRAINT_MAP = Map.ofEntries(
            Map.entry(SOURCE_PROPERTY_NAME,VALID_SOURCE_PROPERTY_CONSTRAINT)
    );
}
