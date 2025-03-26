package org.voyager.utls;

import org.apache.commons.lang3.StringUtils;

public class ConstantsUtil {
    public static final String GEONAMES_API_USERNAME = "GEONAMES_API_USERNAME";
    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    public static final String VOYAGER_API_KEY = "VOYAGER_API_KEY";
    public static final String ENV_VAR_LITERAL = "${%s}";

    public static boolean invalidEnvironmentVar(String envVarKey, String envVarVal) {
        return StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,envVarKey).equals(envVarVal);
    }
}
