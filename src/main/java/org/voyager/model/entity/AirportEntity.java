package org.voyager.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.voyager.model.airport.AirportType;

import java.time.ZoneId;
import java.util.TimeZone;

@Entity
@Table(name="airports")
@Getter @Setter @Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class AirportEntity {
    @Id @Column(name = "iata", length = 3,
            columnDefinition = "bpchar")
    String iata;
    String name;
    @Column(length = 50)
    String city;
    @Column(name="subd",length = 50)
    String subdivision;
    @Column(name="country",length = 2,
            columnDefinition = "bpchar")
    String countryCode;
    @Column(name = "lon")
    Double longitude;
    @Column(name = "lat")
    Double latitude;
    @Enumerated(EnumType.STRING)
    AirportType type;
    @Column(name = "tz")
    ZoneId zoneId;
}