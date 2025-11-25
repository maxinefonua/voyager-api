package org.voyager.api.service;

import io.vavr.control.Option;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;

import java.util.List;

public interface QuickPathSearchService {
    List<PathDetailed> findQuickPaths(PathSearchRequest request, int limit);
}
