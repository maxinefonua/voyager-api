package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.voyager.model.Airline;

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

    @Column(name = "orgn",length = 3,columnDefinition = "bpchar")
    String origin;

    @Column(name = "dstn",length = 3,columnDefinition = "bpchar")
    String destination;

    @Enumerated(EnumType.STRING)
    Airline airline;
}
