package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.voyager.commons.model.airport.AirportType;
import java.time.ZoneId;

@Entity
@Table(name="airports")
@Getter @Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class AirportEntity {
    @Id @Column(name = "iata", length = 3,
            columnDefinition = "bpchar")
    String iata;

    @Setter
    String name;

    @Column(length = 50)
    @Setter
    String city;

    @Column(name="subd",length = 50)
    @Setter
    String subdivision;

    @Column(name="country",length = 2,
            columnDefinition = "bpchar")
    String countryCode;

    @Column(name = "lon",
            columnDefinition = "real")
    Double longitude;

    @Column(name = "lat",
            columnDefinition = "real")
    Double latitude;

    @Enumerated(EnumType.STRING)
    @Setter
    AirportType type;

    @Column(name = "tz")
    ZoneId zoneId;
}