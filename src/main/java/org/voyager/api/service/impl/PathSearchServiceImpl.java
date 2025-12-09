package org.voyager.api.service.impl;

import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.voyager.api.cache.CacheKeyUtils;
import org.voyager.api.error.MessageConstants;
import org.voyager.api.model.path.CachedSearchResponse;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.path.SearchSession;
import org.voyager.api.model.response.SearchResponse;
import org.voyager.api.model.response.SearchStatus;
import org.voyager.api.service.QuickPathSearchService;
import org.voyager.api.service.ComprehensivePathSearchService;
import org.voyager.api.service.PathSearchService;
import org.voyager.api.manager.SearchSessionManager;
import java.util.List;
import java.util.ArrayList;

@Service
public class PathSearchServiceImpl implements PathSearchService {
    @Autowired
    QuickPathSearchService quickPathSearchService;

    @Autowired
    ComprehensivePathSearchService comprehensivePathSearchService;

    @Autowired
    SearchSessionManager sessionManager;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    TaskExecutor taskExecutor;

    private static final Logger LOGGER = LoggerFactory.getLogger(PathSearchServiceImpl.class);

    @Override
    public SearchResponse searchPaths(PathSearchRequest request) {
        // check cache
        String cacheKey = CacheKeyUtils.generatePathSearchKey(request);
        CachedSearchResponse cachedSearchResponse = getCachedSearchResponse(cacheKey);
        if (cachedSearchResponse == null) {
            // find direct paths
            List<PathDetailed> quickPaths = quickPathSearchService.findQuickPaths(request);
            cachedSearchResponse = CachedSearchResponse.builder()
                    .content(quickPaths)
                    .hasMore(true)
                    .status(SearchStatus.SEARCHING)
                    .build();
            // cache direct paths
            cacheResponse(cacheKey,cachedSearchResponse);

            //start search
            sessionManager.createSession(request,cacheKey,quickPaths.size());
            startBackgroundSearch(request,cacheKey);

        } else if (cachedSearchResponse.isHasMore()) {
            // fetch more from session
            List<PathDetailed> content = new ArrayList<>(cachedSearchResponse.getContent());
            Option<SearchSession> sessionOption = sessionManager.getSession(cacheKey);
            if (sessionOption.isDefined()) {
                SearchSession session = sessionOption.get();

                // add additional to content
                List<PathDetailed> additionalPaths = session.getPollResults();
                content.addAll(additionalPaths);

                // update cached response
                cachedSearchResponse.setHasMore(session.hasMoreResults());
                cachedSearchResponse.setContent(content);
                SearchStatus status = SearchStatus.SEARCHING;
                if (session.isSearchComplete()) {
                    status = SearchStatus.COMPLETE;
                } else if (session.isFailed() || session.isExpired()) {
                    status = SearchStatus.FAILED;
                }
                cachedSearchResponse.setStatus(status);

                // cacheResponse
                cacheResponse(cacheKey,cachedSearchResponse);
            } else {
                LOGGER.error("cached search response for cacheKey: {} has more, but get session returned null",
                        cacheKey);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
        }
        List<PathDetailed> paginated = paginate(
                cachedSearchResponse.getContent(),request.getSkip(),request.getSize());
        return SearchResponse.builder()
                .content(paginated)
                .status(cachedSearchResponse.getStatus())
                .hasMore(cachedSearchResponse.isHasMore())
                .totalFound(cachedSearchResponse.getContent().size())
                .size(paginated.size())
                .build();
    }

    private void cacheResponse(String cacheKey, CachedSearchResponse cachedPaths) {
        Cache cache = cacheManager.getCache("search_paths");
        assert cache != null;
        cache.put(cacheKey,cachedPaths);
    }

    private CachedSearchResponse getCachedSearchResponse(String cacheKey) {
        Cache cache = cacheManager.getCache("search_paths");
        assert cache != null;
        Object cached = cache.get(cacheKey, Object.class);
        if (cached instanceof CachedSearchResponse cachedSearchResponse) {
            return cachedSearchResponse;
        }
        return null;
    }

    private List<PathDetailed> paginate(List<PathDetailed> content, int skip, int size) {
        int end = Math.min(skip + size, content.size());
        if (skip >= content.size()) {
            return List.of();
        }
        return content.subList(skip, end);
    }

    private void startBackgroundSearch(PathSearchRequest request, String cacheKey) {
        taskExecutor.execute(() -> {
            Option<SearchSession> sessionOption = sessionManager.getSession(cacheKey);
            if (sessionOption.isEmpty()) {
                LOGGER.error("startBackgroundSearch for cacheKey: {} called, but session not created",
                        cacheKey);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
            SearchSession session = sessionOption.get();
            try {
                comprehensivePathSearchService.streamPaths(
                        request,
                        session::addResult
                );
                session.markComplete();
            } catch (Exception e) {
                session.markFailed();
            }
        });
    }
}
