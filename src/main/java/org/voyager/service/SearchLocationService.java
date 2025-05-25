package org.voyager.service;

import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.SearchResult;

public interface SearchLocationService {
    public SearchResult<ResultSearch> search(String query, int startRow, int limit);
    public LookupAttribution attribution();
}
