package org.voyager.service;

public interface SearchLocationService<T> {
    // TODO: change to Either<ServiceError,List<T>> or whichever order is correct

    public T search(String searchText, int startRow);

}
