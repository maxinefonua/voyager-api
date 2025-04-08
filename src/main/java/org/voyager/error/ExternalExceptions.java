package org.voyager.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

public class ExternalExceptions {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExternalExceptions.class);

    public static void validateExternalResponse(ResponseEntity responseEntity, String requestURL){
        if (responseEntity.getStatusCode().value() != 200 || responseEntity.getBody() == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Received non-200 status code or null response body from external API endpoint: ");
            sb.append(requestURL);
            if (responseEntity.hasBody()) {
                sb.append("r\n");
                sb.append("Response: ");
                sb.append(responseEntity.getBody());
            }
            LOGGER.error(sb.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred fetching search results.");
        }
    }
}
