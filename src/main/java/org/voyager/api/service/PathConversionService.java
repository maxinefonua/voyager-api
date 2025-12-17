package org.voyager.api.service;

import org.voyager.api.model.path.PathDetailed;
import org.voyager.api.model.path.PathSearchRequest;
import org.voyager.commons.model.path.Path;

import java.util.List;
import java.util.function.Consumer;

public interface PathConversionService {
     void convertStreaming(List<Path> pathList, PathSearchRequest pathSearchRequest,
                          Consumer<PathDetailed> pathConsumer);
}
