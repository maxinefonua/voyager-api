package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name="routes")
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Integer id;

    @Column(name = "orgn",length = 3,columnDefinition = "bpchar")
    String origin;

    @Column(name = "dstn",length = 3,columnDefinition = "bpchar")
    String destination;

    @Column(name = "dist", columnDefinition = "numeric")
    @Setter
    private Double distanceKm;
}
