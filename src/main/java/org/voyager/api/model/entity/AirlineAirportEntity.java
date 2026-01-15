package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.*;
import org.voyager.commons.model.airline.Airline;

@Entity
@Table(name="airline_airports")
@Getter
@Builder(toBuilder = true)
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
    @Setter
    Boolean isActive;
}