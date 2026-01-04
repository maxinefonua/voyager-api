package org.voyager.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.voyager.commons.model.airline.Airline;
import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "flights")
public class FlightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "flight_no")
    String flightNumber;

    @Column(name = "route_id")
    Integer routeId;

    @Column(name = "departure_zdt")
    ZonedDateTime zonedDateTimeDeparture;

    @Column(name = "arrival_zdt")
    ZonedDateTime zonedDateTimeArrival;

    @Enumerated(EnumType.STRING)
    Airline airline;

    @Column(name = "active")
    Boolean isActive;
}
