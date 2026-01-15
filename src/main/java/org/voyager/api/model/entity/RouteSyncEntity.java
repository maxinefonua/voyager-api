package org.voyager.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.*;
import org.voyager.commons.model.route.Status;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Table(name="route_sync")
public class RouteSyncEntity {
    @Id
    private Integer routeId;

    @Enumerated(EnumType.STRING)
    @Setter
    private Status status;

    @Setter
    private int attempts;

    @Column(name = "last_processed_at")
    @Setter
    private ZonedDateTime lastProcessedAt;

    @Column(name = "error_message")
    @Setter
    private String errorMessage;

    @Column(name = "created_at")
    @Setter
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Setter
    private ZonedDateTime updatedAt;
}
