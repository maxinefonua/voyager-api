package org.voyager.api.service;

import org.voyager.api.model.path.PathDetailedResponse;
import org.voyager.api.model.query.PathQuery;
import org.voyager.api.model.response.PagedResponse;
import org.voyager.commons.model.path.Path;
import org.voyager.commons.model.path.airline.AirlinePath;
import org.voyager.commons.model.path.airline.PathAirlineQuery;

public interface PathService {
    PathDetailedResponse getPathDetailedList(PathQuery pathQuery);
    PagedResponse<AirlinePath> getAirlinePathList(PathAirlineQuery pathAirlineQuery);
    PagedResponse<Path> getPathList(PathAirlineQuery pathAirlineQuery);
}