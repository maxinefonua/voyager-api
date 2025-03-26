package org.voyager.service;

import org.voyager.model.response.SearchResponseGeoNames;

public interface SearchLocationService<T> {
    // TODO: change to Either<ServiceError,List<T>> or whichever order is correct

    public T search(String searchText, int startRow);

}
