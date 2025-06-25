package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.voyager.model.Airline;

@Entity
@Table(name="airline_airports")
@Data @Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AirlineAirportEntity {
    @Id
    @Column(name = "iata", length = 3,
            columnDefinition = "bpchar")
    String iata;

    @Enumerated(EnumType.STRING)
    Airline airline;

    @Column(name = "active")
    Boolean isActive;
}
