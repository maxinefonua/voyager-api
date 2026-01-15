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
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.CachedPathResults;
import org.voyager.api.model.path.CachedSearchResponse;
import org.voyager.api.model.path.Session;
import org.voyager.api.model.response.SearchResponse;
import org.voyager.api.model.response.SearchStatus;
import org.voyager.api.service.PathConversionService;
import org.voyager.api.service.QuickPathSearchService;
import org.voyager.api.service.ComprehensivePathSearchService;
import org.voyager.api.service.PathSearchService;
import org.voyager.api.manager.SearchSessionManager;
import org.voyager.commons.model.path.Path;
import java.util.List;
import java.util.ArrayList;

@Service
public class PathSearchServiceImpl implements PathSearchService {
    @Autowired
    QuickPathSearchService quickPathSearchService;
    @Autowired
    ComprehensivePathSearchService comprehensivePathSearchService;
    @Autowired
    PathConversionService pathConversionService;
    @Autowired
    SearchSessionManager sessionManager;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    TaskExecutor taskExecutor;

    private static final Logger LOGGER = LoggerFactory.getLogger(PathSearchServiceImpl.class);

    @Override
    public SearchResponse searchPaths(PathSearchRequest request) {
        // check response cache
        String responseCacheKey = CacheKeyUtils.generateResponseKey(request);
        String pathCacheKey = CacheKeyUtils.generatePathKey(request);
        String conversionCacheKey = CacheKeyUtils.generateConversionKey(request);

        // check response cache
        CachedSearchResponse cachedSearchResponse = getCachedSearchResponse(responseCacheKey);
        if (cachedSearchResponse == null) {
            cachedSearchResponse = handleFirstRequestSearch(request,pathCacheKey,conversionCacheKey);
            cacheResponse(responseCacheKey,cachedSearchResponse);
        } else if (!cachedSearchResponse.getStatus().equals(SearchStatus.COMPLETE)) {
            cachedSearchResponse = handleExistingResponse(
                    cachedSearchResponse,request,pathCacheKey,conversionCacheKey);
            cacheResponse(responseCacheKey,cachedSearchResponse);
        }
        List<PathDetailed> paginated = paginate(
                cachedSearchResponse.getContent(), request.getSkip(), request.getSize());
        // has more and paginated size < getContent
        return SearchResponse.builder()
                .content(paginated)
                .status(cachedSearchResponse.getStatus())
                .hasMore(cachedSearchResponse.getStatus().equals(SearchStatus.SEARCHING))
                .totalFound(cachedSearchResponse.getContent().size())
                .size(paginated.size())
                .build();
    }

    private CachedSearchResponse handleExistingResponse(
            CachedSearchResponse cachedSearchResponse, PathSearchRequest request, String pathCacheKey,
            String conversionCacheKey) {
        CachedPathResults cachedPathResults = getCachedPathResults(pathCacheKey);
        if (cachedPathResults == null) {
            LOGGER.error("handleExistingResponse get cached pathCacheKey: {} returned null",
                    pathCacheKey);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                     MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        } else if (cachedPathResults.getStatus().equals(SearchStatus.SEARCHING)) {
            cachedPathResults = fetchActivePathResults(cachedPathResults,pathCacheKey);
            if (cachedPathResults.getStatus().equals(SearchStatus.COMPLETE)) {
                cachedSearchResponse.setStatus(SearchStatus.CONVERTING);
            } else {
                cachedSearchResponse.setStatus(cachedSearchResponse.getStatus());
            }
            cachePaths(pathCacheKey,cachedPathResults);
        }

        if (cachedSearchResponse.getConverting() == 0) {
            int converted = cachedSearchResponse.getConverted();
            int totalConnections = cachedPathResults.getConnections().size();
            if (converted < totalConnections) {
                List<Path> nextBatch = cachedPathResults.getConnections().subList(converted, totalConnections);
                String batchConversionKey = String.format("%s-%d",conversionCacheKey,converted);
                sessionManager.createConversionSession(
                        request,batchConversionKey,converted);
                startBackgroundConversion(nextBatch,request,batchConversionKey);
                cachedSearchResponse.setConverting(nextBatch.size());
                if (cachedPathResults.getStatus().equals(SearchStatus.COMPLETE)) {
                    cachedSearchResponse.setStatus(SearchStatus.CONVERTING);
                }
            }
            return cachedSearchResponse;
        } else {
            return handleActiveConversionResults(cachedSearchResponse,cachedPathResults,conversionCacheKey,request);
        }
    }

    private CachedSearchResponse handleActiveConversionResults(
            CachedSearchResponse cachedSearchResponse, CachedPathResults cachedPathResults, String conversionCacheKey,
            PathSearchRequest request) {
        int converted = cachedSearchResponse.getConverted();
        Option<Session<PathDetailed>> sessionOption = sessionManager.getConversionSession(
                String.format("%s-%d",conversionCacheKey,converted));
        if (sessionOption.isEmpty()) {
            LOGGER.error("handleActiveConversionResults for conversionCacheKey: {} called, but session is empty",
                    conversionCacheKey);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        } else {
            Session<PathDetailed> conversionSession = sessionOption.get();
            SearchStatus status = SearchStatus.SEARCHING;
            int converting = cachedSearchResponse.getConverting();
            if (conversionSession.isSessionComplete()) {
                converted += converting;
                converting = 0;
                status = cachedPathResults.getStatus();
            } else if (conversionSession.isExpired() || conversionSession.isFailed()) {
                status = SearchStatus.FAILED;
                converting = 0;
            }
            if (conversionSession.hasMore()) {
                List<PathDetailed> conversionsFound = conversionSession.getResults();
                if (!conversionsFound.isEmpty()) {
                    cachedSearchResponse.getContent().addAll(conversionsFound);
                }
            }
            if (converting == 0) {
                int totalConnections = cachedPathResults.getConnections().size();
                List<Path> nextBatch = cachedPathResults.getConnections().subList(converted, totalConnections);
                if (!nextBatch.isEmpty()) {
                    String batchConversionKey = String.format("%s-%d", conversionCacheKey, converted);
                    sessionManager.createConversionSession(
                            request, batchConversionKey, converted + cachedPathResults.getDirects().size());
                    startBackgroundConversion(nextBatch, request, batchConversionKey);
                    cachedSearchResponse.setStatus(SearchStatus.CONVERTING);
                    converting = nextBatch.size();
                }
            }
            return CachedSearchResponse.builder()
                    .status(status)
                    .content(cachedSearchResponse.getContent())
                    .converting(converting)
                    .converted(converted)
                    .hasMore(converting == 0 && converted == cachedPathResults.getConnections().size())
                    .build();
        }
    }

    private CachedSearchResponse handleFirstRequestSearch(
            PathSearchRequest request, String pathCacheKey, String conversionCacheKey) {
        CachedPathResults cachedPathResults = getCachedPathResults(pathCacheKey);
        if (cachedPathResults == null) {
            return handleFirstNonExistingPathSearch(request,pathCacheKey,conversionCacheKey);
        } else {
            return handleFirstExistingPathSearch(cachedPathResults,request,pathCacheKey,conversionCacheKey);
        }
    }

    private CachedSearchResponse handleFirstExistingPathSearch(
            CachedPathResults cachedPathResults, PathSearchRequest request, String pathCacheKey,
            String conversionCacheKey) {
        List<PathDetailed> convertedDirectPaths = new ArrayList<>();
        pathConversionService.convertStreaming(cachedPathResults.getDirects(),request,convertedDirectPaths::add);
        if (cachedPathResults.getStatus() == SearchStatus.SEARCHING) {
            cachedPathResults = fetchActivePathResults(cachedPathResults,pathCacheKey);
            cachePaths(pathCacheKey,cachedPathResults);
        }
        List<Path> connectionPaths = cachedPathResults.getConnections();
        String batchConversionKey = String.format("%s-%d",conversionCacheKey,0);
        sessionManager.createConversionSession(request,batchConversionKey,cachedPathResults.getDirects().size());
        startBackgroundConversion(connectionPaths,request,batchConversionKey);
        return CachedSearchResponse.builder()
                .content(convertedDirectPaths)
                .converted(0)
                .hasMore(true)
                .converting(connectionPaths.size())
                .status(SearchStatus.SEARCHING)
                .build();
    }

    private CachedPathResults fetchActivePathResults(CachedPathResults cachedPathResults, String pathCacheKey) {
        Option<Session<Path>> sessionOption = sessionManager.getSearchSession(pathCacheKey);
        if (sessionOption.isEmpty()) {
            LOGGER.error("fetchActivePathResults for pathCacheKey: {} called, but session is empty",
                    pathCacheKey);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
        } else {
            Session<Path> searchSession = sessionOption.get();
            SearchStatus status = SearchStatus.SEARCHING;
            if (searchSession.isSessionComplete()) {
                status = SearchStatus.COMPLETE;
            } else if (searchSession.isExpired() || searchSession.isFailed()) {
                status = SearchStatus.FAILED;
            }
            if (searchSession.hasMore()) {
                List<Path> foundPaths = searchSession.getResults();
                if (!foundPaths.isEmpty()) {
                    cachedPathResults.getConnections().addAll(foundPaths);
                }
            }
            cachedPathResults.setStatus(status);
            return cachedPathResults;
        }
    }

    private CachedSearchResponse handleFirstNonExistingPathSearch(
            PathSearchRequest request, String pathCacheKey, String conversionCacheKey) {
        List<Path> directPaths = quickPathSearchService.findQuickPaths(request);
        sessionManager.createSearchSession(request,pathCacheKey,directPaths.size());
        startBackgroundSearch(request,pathCacheKey);
        CachedPathResults cachedPathResults = CachedPathResults.builder()
                .directs(directPaths)
                .status(SearchStatus.SEARCHING)
                .build();
        List<PathDetailed> convertedDirect = new ArrayList<>();
        pathConversionService.convertStreaming(directPaths,request,convertedDirect::add);

        cachedPathResults = fetchActivePathResults(cachedPathResults,pathCacheKey);
        cachePaths(pathCacheKey,cachedPathResults);
        int converted = 0;
        int converting = 0;
        if (!cachedPathResults.getConnections().isEmpty()) {
            List<Path> connections = cachedPathResults.getConnections();
            String batchConversionKey = String.format("%s-%d",conversionCacheKey,0);
            sessionManager.createConversionSession(
                    request,batchConversionKey,converted+cachedPathResults.getDirects().size());
            startBackgroundConversion(connections,request,batchConversionKey);
            converting = connections.size();;
        }

        return CachedSearchResponse.builder()
                .content(convertedDirect)
                .status(SearchStatus.SEARCHING)
                .hasMore(true)
                .converted(converted)
                .converting(converting)
                .build();
    }

    private void startBackgroundConversion(List<Path> connectionPaths, PathSearchRequest request, String conversionCacheKey) {
        taskExecutor.execute(() -> {
            Option<Session<PathDetailed>> sessionOption = sessionManager.getConversionSession(conversionCacheKey);
            if (sessionOption.isEmpty()) {
                LOGGER.error("startBackgroundConversion for conversionCacheKey: {} called, but session not created",
                        conversionCacheKey);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
            Session<PathDetailed> session = sessionOption.get();
            try {
                pathConversionService.convertStreaming(
                        connectionPaths,
                        request,
                        session::addResult
                );
                LOGGER.info("marking complete conversion session of conversionCacheKey {} with {} conversions",
                        conversionCacheKey,session.getTotalFound());
                session.markComplete();
            } catch (Exception e) {
                LOGGER.info("marking failed conversion session of conversionCacheKey {} with {} conversions when failed",
                        conversionCacheKey,session.getTotalFound());
                session.markFailed();
            }
        });
    }

    private void cacheResponse(String responseCacheKey, CachedSearchResponse cachedSearchResponse) {
        Cache cache = cacheManager.getCache("search_response");
        assert cache != null;
        cache.put(responseCacheKey,cachedSearchResponse);
    }

    private CachedSearchResponse getCachedSearchResponse(String cacheKey) {
        Cache cache = cacheManager.getCache("search_response");
        assert cache != null;
        Object cached = cache.get(cacheKey, Object.class);
        if (cached instanceof CachedSearchResponse cachedSearchResponse) {
            return cachedSearchResponse;
        }
        return null;
    }

    private void cachePaths(String pathCacheKey, CachedPathResults cachedPathResults) {
        Cache cache = cacheManager.getCache("path_results");
        assert cache != null;
        cache.put(pathCacheKey,cachedPathResults);
    }

    private CachedPathResults getCachedPathResults(String pathCacheKey) {
        Cache cache = cacheManager.getCache("path_results");
        assert cache != null;
        Object cached = cache.get(pathCacheKey, Object.class);
        if (cached instanceof CachedPathResults cachedPathResults) {
            return cachedPathResults;
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

    private void startBackgroundSearch(PathSearchRequest request, String pathCacheKey) {
        taskExecutor.execute(() -> {
            Option<Session<Path>> sessionOption = sessionManager.getSearchSession(pathCacheKey);
            if (sessionOption.isEmpty()) {
                LOGGER.error("startBackgroundSearch for pathCacheKey: {} called, but session not created",
                        pathCacheKey);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.INTERNAL_SERVICE_ERROR_GENERIC_MESSAGE);
            }
            Session<Path> session = sessionOption.get();
            try {
                comprehensivePathSearchService.streamPaths(
                        request,
                        session::addResult
                );
                LOGGER.info("marking complete search session of pathCacheKey {} with {} paths found",
                        pathCacheKey,session.getTotalFound());
                session.markComplete();
            } catch (Exception e) {
                LOGGER.info("marking failed search session of pathCacheKey {} with {} paths found when failed",
                        pathCacheKey,session.getTotalFound());
                session.markFailed();
            }
        });
    }
}
