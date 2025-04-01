package org.voyager.error;

import java.util.Map;

public class MessageConstants {
    private static final String INVALID_PATH_VARIABLE = "Invalid path variable '%s'. %s";
    private static final String INVALID_REQUEST_PARAM = "Invalid request parameter '%s', with value '%s'. %s";
    private static final String GET_GEONAMEID_ERROR = "Error fetching feature details for query result '%s', with geonameId '%s'.";

    public static String buildGetGeonameErrorMessage(String geonameName, Long geonameId){
        return String.format(GET_GEONAMEID_ERROR,geonameName,geonameId);
    }
    public static String buildInvalidPathVariableMessage(String varValue, String validConstraints){
        return String.format(INVALID_PATH_VARIABLE,varValue, validConstraints);
    }
    public static String buildInvalidRequestParameterMessage(String paramName, String paramValue){
        return String.format(INVALID_REQUEST_PARAM,paramName,paramValue,PARAM_TO_CONSTRAINT_MAP.get(paramName));
    }
    public static final String COUNTRY_CODE_PARAM_NAME = "countryCode";
    public static final String IATA_PARAM_NAME = "iata";
    public static final String VALID_IATA_CONSTRAINT = "Must be a valid three-letter IATA code";

    private static final String VALID_COUNTRY_CODE_CONSTRAINT = "Must be a valid two-letter ISO 3166-1 alpha-2 country code";

    private static final Map<String,String> PARAM_TO_CONSTRAINT_MAP = Map.ofEntries(
            Map.entry(COUNTRY_CODE_PARAM_NAME,VALID_COUNTRY_CODE_CONSTRAINT)
    );
}
