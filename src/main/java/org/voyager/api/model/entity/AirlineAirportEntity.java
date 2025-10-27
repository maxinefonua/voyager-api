package org.voyager.api.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.voyager.commons.model.airline.Airline;

@Entity
@Table(name="airline_airports")
@Data @Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AirlineAirportId.class)
public class AirlineAirportEntity {
    @Id
    @Column(name = "iata", length = 3,
            columnDefinition = "bpchar")
    String iata;

    @Id
    @Enumerated(EnumType.STRING)
    Airline airline;

    @Column(name = "active")
    Boolean isActive;
}