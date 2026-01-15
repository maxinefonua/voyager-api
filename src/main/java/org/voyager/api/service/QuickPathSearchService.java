package org.voyager.api.service;

import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.commons.model.path.Path;
import java.util.List;
import java.util.function.Consumer;

public interface QuickPathSearchService {
    List<Path> findQuickPaths(PathSearchRequest request);
    void streamDirectPaths(PathSearchRequest request, Consumer<Path> pathConsumer);
}
