package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name="routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Integer id;

    @Column(name = "from",length = 3,columnDefinition = "bpchar")
    String fromAirport;

    @Column(name = "to",length = 3,columnDefinition = "bpchar")
    String toAirport;

    @Column(name = "is_delta")
    Boolean isDelta;
}
