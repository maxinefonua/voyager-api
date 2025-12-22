package org.voyager.api.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.function.Supplier;
import static org.voyager.api.error.MessageConstants.EXTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;
import static org.voyager.api.error.MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;

public class ServiceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtils.class);

    public static <T> ResponseEntity<T> handleExternalServiceExceptions(Supplier<ResponseEntity<T>> supplier) {
        try {
            ResponseEntity<T> externalResponse = supplier.get();
            HttpHeaders cleanedHeaders = new HttpHeaders();
            cleanedHeaders.setContentType(externalResponse.getHeaders().getContentType());
            return new ResponseEntity<>(
                    externalResponse.getBody(),
                    cleanedHeaders,
                    externalResponse.getStatusCode()
            );
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    EXTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
    }

    public static <T> T handleJPAExceptions(Supplier<T> repositoryFunction) {
        try {
            return repositoryFunction.get();
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("handleJPA caught dataAccessException: {}",dataAccessException.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
    }

    public static void handleJPAExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (DataAccessException dataAccessException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
    }
}
