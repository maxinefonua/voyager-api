package org.voyager.api.model.query;

import io.vavr.control.Option;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import java.util.Set;

@Getter @Builder
public class PathQuery {
    @NotEmpty
    private Set<@ValidAirportCode String> originSet;

    @NotEmpty
    private Set<@ValidAirportCode String> destinationSet;

    private Option<Airline> airlineOption;

    @NotNull @Min(0)
    private Integer page;

    @NotNull @Min(1) @Max(20)
    private Integer pageSize;
}
