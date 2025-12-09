package org.voyager.api.service;

import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import java.util.function.Consumer;

public interface ComprehensivePathSearchService {
    void streamPaths(PathSearchRequest request, Consumer<PathDetailed> pathConsumer);
}
