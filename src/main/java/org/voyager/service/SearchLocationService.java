package org.voyager.service;

import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.VoyagerListResponse;

public interface SearchLocationService {
    public VoyagerListResponse<ResultSearch> search(String query, int startRow, int limit);
    public LookupAttribution attribution();
}
