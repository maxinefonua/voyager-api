package org.voyager.service.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

public class ServiceUtils {
    public static <T> T handleJPAExceptions(Supplier<T> repositoryFunction) {
        try {
            return repositoryFunction.get();
        } catch (DataAccessException dataAccessException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An internal service error has occured. Alerting yet to be implemented.");
        }
    }

    public static void handleJPAExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (DataAccessException dataAccessException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An internal service error has occured. Alerting yet to be implemented.");
        }
    }
}
