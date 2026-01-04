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
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name="route_sync")
public class RouteSyncEntity {
    @Id
    @Getter
    private Integer routeId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int attempts;

    @Column(name = "last_processed_at")
    private ZonedDateTime lastProcessedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
