package org.voyager.api.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

public class ExternalExceptions {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExternalExceptions.class);

    public static <T> void validateExternalResponse(ResponseEntity<T> responseEntity, String requestURL){
        if (responseEntity.getStatusCode().value() != 200 || responseEntity.getBody() == null) {
            LOGGER.error("Received non-200 status code or null response body from external API endpoint: {}, response: {}",
                    requestURL,responseEntity.getBody());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred fetching search results.");
        }
    }
    public static <T> void validateExternalResponse(ResponseEntity<T> responseEntity){
        if (responseEntity.getStatusCode().value() != 200 || responseEntity.getBody() == null) {
            LOGGER.error("External call returned non-200 status code or null response body response: {}",
                    responseEntity.getBody());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred fetching search results.");
        }
    }
}
