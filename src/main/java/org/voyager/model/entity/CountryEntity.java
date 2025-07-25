package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.voyager.model.country.Continent;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "countries")
public class CountryEntity {
    @Id @Column(length = 2, columnDefinition = "bpchar")
    String code;
    String name;
    Long population;

    @Column(name = "area_sq_km", columnDefinition = "real")
    Double areaSqKm;

    @Enumerated(EnumType.STRING)
    Continent continent;

    @Column(name = "currency", length = 3, columnDefinition = "bpchar")
    String currencyCode;

    @Column(name = "capital")
    String capitalCity;

    @Column(name = "lang")
    String[] languages;

    @Column(columnDefinition = "real")
    Double[] bounds;
}
