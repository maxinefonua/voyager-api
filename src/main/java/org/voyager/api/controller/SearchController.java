package org.voyager.api.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.voyager.api.service.SearchLocationService;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.result.LookupAttribution;
import org.voyager.commons.model.result.ResultSearch;
import org.voyager.commons.model.response.SearchResult;
import org.voyager.commons.model.result.ResultSearchFull;

@RestController
class SearchController {
    @Autowired
    private SearchLocationService searchLocationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    @GetMapping(Path.Admin.SEARCH)
    public SearchResult<ResultSearch> search(@RequestParam(ParameterNames.QUERY_PARAM_NAME) String query,
                                             @RequestParam(name = ParameterNames.SKIP_ROW_PARAM_NAME,defaultValue = "0") Integer skipRowCount,
                                             @RequestParam(name = ParameterNames.LIMIT_PARAM_NAME,defaultValue = "10") Integer limit) {
        LOGGER.info(String.format("GET /search called with query: '%s', skipRowCount: %d, limit: %d",
                query,skipRowCount,limit));
        SearchResult<ResultSearch> cachedResults = searchLocationService.search(query,skipRowCount,limit);
        SearchResult<ResultSearch> response = SearchResult.<ResultSearch>builder()
                .results(searchLocationService.augmentLocationStatus(cachedResults.getResults()))
                .resultCount(cachedResults.getResultCount()).build();
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.Admin.FETCH_SOURCE_ID)
    public ResultSearchFull fetchResultSearch(@PathVariable(ParameterNames.SOURCE_ID_PARAM_NAME) String sourceId) {
        LOGGER.info("GET {}/{} called",Path.Admin.FETCH,sourceId);
        ResultSearchFull response = searchLocationService.fetchResultSearch(sourceId);
        LOGGER.debug(String.format("response: '%s'",response));
        return response;
    }

    @GetMapping(Path.Admin.ATTRIBUTION)
    @Cacheable("searchAttributionCache")
    public LookupAttribution attribution(){
        LOGGER.info("GET {} called",Path.Admin.ATTRIBUTION);
        LookupAttribution response = searchLocationService.attribution();
        LOGGER.debug("response: {}",response);
        return response;
    }
}
