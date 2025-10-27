package org.voyager.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.commons.model.airline.Airline;

@Entity
@Table(name="airline")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AirlineEntity {
    @Id @Column(name = "name")
    @Enumerated(EnumType.STRING)
    Airline airline;
}
