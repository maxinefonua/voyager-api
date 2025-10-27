package org.voyager.api.service;

import org.springframework.cache.annotation.Cacheable;
import org.voyager.commons.model.result.LookupAttribution;
import org.voyager.commons.model.result.ResultSearch;
import org.voyager.commons.model.response.SearchResult;
import org.voyager.commons.model.result.ResultSearchFull;

import java.util.List;

public interface SearchLocationService {
    @Cacheable("searchCache")
    public SearchResult<ResultSearch> search(String query, int startRow, int limit);
    @Cacheable("locationCache")
    public ResultSearchFull fetchResultSearch(String sourceId);
    public LookupAttribution attribution();
    List<ResultSearch> augmentLocationStatus(List<ResultSearch> cachedResults);
}
