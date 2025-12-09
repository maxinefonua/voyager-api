package org.voyager.api.service;

import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import java.util.List;

public interface QuickPathSearchService {
    List<PathDetailed> findQuickPaths(PathSearchRequest request);
}
