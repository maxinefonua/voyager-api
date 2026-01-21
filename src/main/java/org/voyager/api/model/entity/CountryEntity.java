package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.voyager.commons.model.country.Continent;

@Entity
@Getter
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
