package org.voyager.api.service;

import jakarta.validation.constraints.NotEmpty;
import org.voyager.api.model.path.PathDetailed;
import org.voyager.commons.validate.annotations.ValidAirportCode;

import java.util.List;
import java.util.Set;

public interface FlightSearchService {
    List<PathDetailed> findAllFlights(@NotEmpty Set<@ValidAirportCode String> originSet, @NotEmpty Set<@ValidAirportCode String> destinationSet);
    List<PathDetailed> findDirectFlights(@NotEmpty Set<@ValidAirportCode String> originSet, @NotEmpty Set<@ValidAirportCode String> destinationSet);
}
