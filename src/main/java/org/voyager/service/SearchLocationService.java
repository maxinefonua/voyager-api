package org.voyager.service;

import org.springframework.cache.annotation.Cacheable;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.SearchResult;

import java.util.List;

public interface SearchLocationService {
    @Cacheable("searchCache")
    public SearchResult<ResultSearch> search(String query, int startRow, int limit);
    public LookupAttribution attribution();
    List<ResultSearch> augmentLocationStatus(List<ResultSearch> cachedResults);
}
