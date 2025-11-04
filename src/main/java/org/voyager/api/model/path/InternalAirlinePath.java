package org.voyager.api.model.path;

import lombok.Builder;
import lombok.Getter;
import org.voyager.api.model.entity.RouteEntity;
import org.voyager.commons.model.airline.Airline;
import java.util.List;

@Getter @Builder
public class InternalAirlinePath {
    private Airline airline;
    private Double totalDistanceKm;
    private List<RouteEntity> routeList;
}
