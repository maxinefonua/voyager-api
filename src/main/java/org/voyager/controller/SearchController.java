package org.voyager.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.voyager.model.location.Location;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.service.*;

import static org.voyager.utils.ConstantsUtils.*;

class SearchController {
    @Autowired
    private SearchLocationService searchLocationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    @GetMapping("/search")
    public SearchResult<ResultSearch> search(@RequestParam(QUERY_PARAM_NAME) String q,
                                             @RequestParam(name = SKIP_ROW_PARAM_NAME,defaultValue = "0") Integer skipRowCount,
                                             @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "10") Integer limit) {
        SearchResult<ResultSearch> cachedResults = searchLocationService.search(q,skipRowCount,limit);
        return SearchResult.<ResultSearch>builder()
                .results(searchLocationService.augmentLocationStatus(cachedResults.getResults()))
                .resultCount(cachedResults.getResultCount()).build();
    }

    @GetMapping("/fetch/{sourceId}")
    public ResultSearchFull fetchResultSearch(@PathVariable(SOURCE_ID_PARAM_NAME) String sourceId) {
        return searchLocationService.fetchResultSearch(sourceId);
    }

    @GetMapping("/search-attribution")
    @Cacheable("searchAttributionCache")
    public LookupAttribution attribution(){
        return searchLocationService.attribution();
    }
}
