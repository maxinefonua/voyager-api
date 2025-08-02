package org.voyager.error;

import java.util.Map;

import static org.voyager.utils.ConstantsUtils.*;

public class MessageConstants {
    private static final String INVALID_REQUEST_PARAM = "Invalid request parameter '%s' with value '%s'. %s";
    private static final String MISSING_REQUEST_PARAM = "Missing request parameter '%s'. %s";
    private static final String INVALID_REQUEST_BODY_PATCH = "Invalid request body for '%s'. A valid PATCH has at least one field set";
    private static final String INVALID_REQUEST_BODY_PROPERTY = "Invalid request body property '%s' with value '%s'. %s";
    private static final String INVALID_REQUEST_BODY_PROPERTY_NO_MESSAGE = "Invalid request body property '%s' with value '%s'";

    private static final String RESOURCE_NOT_FOUND_FOR_PATH_VAR = "Resource not found for path variable '%s' with value '%s'. %s";
    private static final String RESOURCE_NOT_FOUND_FOR_PATH_VAR_NO_MESSAGE = "Resource not found for path variable '%s' with value '%s'";
    private static final String RESOURCE_NOT_FOUND_FOR_PARAM = "Resource not found for parameter '%s' with value '%s'. %s";
    private static final String RESOURCE_NOT_FOUND_FOR_MULTI_PARAM = "Resource not found for parameter '%s' with value '%s' and parameter '%s' with value '%s'. %s";
    private static final String INVALID_PATH_VAR = "Invalid path variable '%s' with value '%s'. %s";

    private static final String GET_GEONAMEID_ERROR = "Error fetching feature details for query result '%s' with geonameId '%s'.";

    private static final String REPOSITORY_SAVE_ERROR = "Internal error occured attempting to save %s. Please consult with API docs to ensure request body is valid.";
    private static final String REPOSITORY_SAVE_WITH_IATA_ERROR = "Internal error occured attempting to save %s with '%s'. Please consult with API docs to ensure request body is valid.";
    private static final String REPOSITORY_PATCH_ERROR = "Internal error occured attempting to patch %s at '%s'. Alerting has yet to be implemented.";

    public static final String INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE = "An internal service error has occured. Alerting yet to be implemented.";

    public static String buildRespositorySaveErrorMessage(String entityName){
        return String.format(REPOSITORY_SAVE_ERROR,entityName);
    }

    public static String buildRespositorySaveErrorMessageWithIata(String entityName,String iata){
        return String.format(REPOSITORY_SAVE_WITH_IATA_ERROR,entityName,iata);
    }

    public static String buildRespositoryPatchErrorMessage(String entityName, String entityId){
        return String.format(REPOSITORY_PATCH_ERROR,entityName,entityId);
    }

    public static String buildGetGeonameErrorMessage(String geonameName, Long geonameId){
        return String.format(GET_GEONAMEID_ERROR,geonameName,geonameId);
    }

    public static String buildInvalidRequestParameterMessage(String paramName, String paramValue){
        return String.format(INVALID_REQUEST_PARAM,paramName,paramValue,CONSTRAINT_MAP.get(paramName));
    }

    public static String buildInvalidPathVariableMessage(String varName, String varValue){
        return String.format(INVALID_PATH_VAR,varName,varValue,CONSTRAINT_MAP.get(varName));
    }

    public static String buildResourceNotFoundForPathVariableMessage(String varName, String varValue){
        return String.format(RESOURCE_NOT_FOUND_FOR_PATH_VAR,varName,varValue,NOT_FOUND_MAP.get(varName));
    }

    public static String buildResourceNotFoundForRequestBodyProperty(String varName, String varValue){
        return String.format(INVALID_REQUEST_BODY_PROPERTY,varName,varValue,NOT_FOUND_MAP.get(varName));
    }

    public static String buildResourceNotFoundForPathVariableNoMessage(String varName, String varValue){
        return String.format(RESOURCE_NOT_FOUND_FOR_PATH_VAR_NO_MESSAGE,varName,varValue);
    }

    public static String buildResourceNotFoundForParameterMessage(String paramName, String varValue){
        return String.format(RESOURCE_NOT_FOUND_FOR_PARAM,paramName,varValue,NOT_FOUND_MAP.get(paramName));
    }

    public static String buildResourceNotFoundForMultiParameterMessage(String paramName1, String varValue1, String paramName2, String varValue2){
        return String.format(RESOURCE_NOT_FOUND_FOR_MULTI_PARAM,paramName1,varValue1,paramName2,varValue2,
                "Valid values required for both parameters.");
    }


    public static String buildMissingRequestParameterMessage(String paramName){
        return String.format(MISSING_REQUEST_PARAM,paramName,MISSING_SOURCE_CONSTRAINT);
    }

    public static String buildInvalidRequestBodyPatch(String requestBodyEntity){
        return String.format(INVALID_REQUEST_BODY_PATCH,requestBodyEntity);
    }


    public static String buildInvalidRequestBodyPropertyMessage(String propertyName, String propertyValue){
        return String.format(INVALID_REQUEST_BODY_PROPERTY,propertyName,propertyValue,CONSTRAINT_MAP.get(propertyName));
    }

    public static String buildInvalidRequestBodyPropertyNoMessage(String propertyName, String propertyValue){
        return String.format(INVALID_REQUEST_BODY_PROPERTY_NO_MESSAGE,propertyName,propertyValue);
    }

    public static final String VALID_IATA_CONSTRAINT = "Must be a valid three-letter IATA code";
    public static final String VALID_ID_CONSTRAINT = "Must be a valid integer value";
    private static final String VALID_COUNTRY_CODE_CONSTRAINT = "Must be a valid two-letter ISO 3166-1 alpha-2 country code";
    private static final String VALID_TYPE_CONSTRAINT = "Valid airport types are: 'civil', 'military', 'historical', 'other'";
    private static final String VALID_AIRLINE_CONSTRAINT = "Currently available airlines are: 'delta'";
    private static final String MISSING_SOURCE_CONSTRAINT = "Must also include 'source' parameter when providing a 'sourceId'";
    private static final String VALID_SOURCE_PROPERTY_CONSTRAINT = "Valid sources are: 'GEONAMES','NOMINATIM','PHOTON','MANUAL'";
    private static final String VALID_SOURCE_ID_CONSTRAINT = "Valid source id required";
    private static final String VALID_AIRPORTS_PROPERTY_CONSTRAINT = "Valid airports are existing three-letter IATA codes";
    private static final String VALID_CONTINENT_CONSTRAINT = "Must be a valid continent name or two-letter continent code";
    private static final String VALID_LIMIT_CONSTRAINT = "Must be a valid integer greater than 0";

    private static final String IATA_RESOURCE_NOT_FOUND = "Information on given IATA code is currently unavailable";

    private static final Map<String,String> NOT_FOUND_MAP = Map.ofEntries(
            Map.entry(IATA_PARAM_NAME,IATA_RESOURCE_NOT_FOUND),
            Map.entry(ORIGIN_PARAM_NAME,IATA_RESOURCE_NOT_FOUND),
            Map.entry(DESTINATION_PARAM_NAME,IATA_RESOURCE_NOT_FOUND),
            Map.entry(EXCLUDE_PARAM_NAME,IATA_RESOURCE_NOT_FOUND),
            Map.entry(AIRPORTS_PROPERTY_NAME,IATA_RESOURCE_NOT_FOUND)
            );

    private static final Map<String,String> CONSTRAINT_MAP = Map.ofEntries(
            Map.entry(COUNTRY_CODE_PARAM_NAME,VALID_COUNTRY_CODE_CONSTRAINT),
            Map.entry(AIRLINE_PARAM_NAME,VALID_AIRLINE_CONSTRAINT),
            Map.entry(TYPE_PARAM_NAME,VALID_TYPE_CONSTRAINT),
            Map.entry(SOURCE_PROPERTY_NAME,VALID_SOURCE_PROPERTY_CONSTRAINT),
            Map.entry(SOURCE_ID_PARAM_NAME,VALID_SOURCE_ID_CONSTRAINT),
            Map.entry(IATA_PARAM_NAME,VALID_IATA_CONSTRAINT),
            Map.entry(ID_PATH_VAR_NAME,VALID_ID_CONSTRAINT),
            Map.entry(ORIGIN_PARAM_NAME,VALID_IATA_CONSTRAINT),
            Map.entry(DESTINATION_PARAM_NAME,VALID_IATA_CONSTRAINT),
            Map.entry(EXCLUDE_PARAM_NAME,VALID_IATA_CONSTRAINT),
            Map.entry(AIRPORTS_PROPERTY_NAME,VALID_AIRPORTS_PROPERTY_CONSTRAINT),
            Map.entry(CONTINENT_PARAM_NAME,VALID_CONTINENT_CONSTRAINT),
            Map.entry(LIMIT_PARAM_NAME,VALID_LIMIT_CONSTRAINT)
    );

    private static final Map<String,String> REQUEST_BODY_PROPERTY_TO_CONSTRAINT_MAP = Map.ofEntries(
            Map.entry(SOURCE_PROPERTY_NAME,VALID_SOURCE_PROPERTY_CONSTRAINT)
    );
}
