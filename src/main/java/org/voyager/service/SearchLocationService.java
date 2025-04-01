package org.voyager.service;

import org.voyager.model.response.VoyagerListResponse;

public interface SearchLocationService<T> {
    // TODO: change to Either<ServiceError,List<T>> or whichever order is correct

    public VoyagerListResponse<T> search(String searchText, int startRow);

}
