package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.*;
import org.voyager.commons.model.airline.Airline;

@Entity
@Table(name="airline")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AirlineEntity {
    @Id @Column(name = "name")
    @Enumerated(EnumType.STRING)
    Airline airline;
}
