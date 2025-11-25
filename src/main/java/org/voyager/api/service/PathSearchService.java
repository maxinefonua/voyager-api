package org.voyager.api.service;

import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.response.SearchResponse;

public interface PathSearchService {
    boolean isPausedForEnrichment();
    void setPauseForEnrichment(boolean pauseForEnrichment);
    SearchResponse searchPaths(PathSearchRequest pathSearchRequest);
    SearchResponse getMorePaths(String searchId, int size);
}
