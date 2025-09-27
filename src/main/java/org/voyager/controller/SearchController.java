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

@RestController
class SearchController {
    @Autowired
    private SearchLocationService searchLocationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    @GetMapping("/search")
    public SearchResult<ResultSearch> search(@RequestParam(QUERY_PARAM_NAME) String query,
                                             @RequestParam(name = SKIP_ROW_PARAM_NAME,defaultValue = "0") Integer skipRowCount,
                                             @RequestParam(name = LIMIT_PARAM_NAME,defaultValue = "10") Integer limit) {
        LOGGER.info(String.format("GET /search called with query: '%s', skipRowCount: %d, limit: %d",
                query,skipRowCount,limit));
        SearchResult<ResultSearch> cachedResults = searchLocationService.search(query,skipRowCount,limit);
        SearchResult<ResultSearch> response = SearchResult.<ResultSearch>builder()
                .results(searchLocationService.augmentLocationStatus(cachedResults.getResults()))
                .resultCount(cachedResults.getResultCount()).build();
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/fetch/{sourceId}")
    public ResultSearchFull fetchResultSearch(@PathVariable(SOURCE_ID_PARAM_NAME) String sourceId) {
        LOGGER.info(String.format("GET /fetch/%s called", sourceId));
        ResultSearchFull response = searchLocationService.fetchResultSearch(sourceId);
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping("/search-attribution")
    @Cacheable("searchAttributionCache")
    public LookupAttribution attribution(){
        LOGGER.info("GET /search-attribution called");
        LookupAttribution response = searchLocationService.attribution();
        LOGGER.info(String.format("response: '%s'",response));
        return response;
    }
}
