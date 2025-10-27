package org.voyager.api.service.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

import static org.voyager.api.error.MessageConstants.EXTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;
import static org.voyager.api.error.MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE;

public class ServiceUtils {
    public static <T> T handleExternalServiceExceptions(Supplier<T> repositoryFunction) {
        try {
            return repositoryFunction.get();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    EXTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        }
    }

    public static <T> T handleJPAExceptions(Supplier<T> repositoryFunction) {
        try {
            return repositoryFunction.get();
        } catch (DataAccessException dataAccessException) {
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
