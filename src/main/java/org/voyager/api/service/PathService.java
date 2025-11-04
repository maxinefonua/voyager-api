package org.voyager.api.service;

import org.voyager.api.model.path.PathDetailedResponse;
import org.voyager.api.model.query.PathQuery;

public interface PathService {
    PathDetailedResponse getPathDetailedList(PathQuery pathQuery);
}