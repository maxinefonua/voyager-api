package org.voyager.api.service;

import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.api.model.response.SearchResponse;

public interface PathSearchService {
    SearchResponse searchPaths(PathSearchRequest pathSearchRequest);
}
