package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
