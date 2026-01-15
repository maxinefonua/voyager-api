package org.voyager.api.model.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import java.util.List;

@Data @Builder
public class AirportQuery {
    @Min(1)
    @Max(1000)
    private int size;
    @Min(0)
    private int page;
    @Pattern(regexp = Regex.COUNTRY_CODE)
    private String country;
    private List<AirportType> airportTypeList;
    private List<Airline> airlineList;
}
