package org.voyager.api.model.entity;

import lombok.EqualsAndHashCode;
import org.voyager.commons.model.airline.Airline;
import java.io.Serializable;

@EqualsAndHashCode
public class AirlineAirportId implements Serializable {
    private String iata;
    private Airline airline;
}
