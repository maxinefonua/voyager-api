package org.voyager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.*;
import org.voyager.model.delta.DeltaStatus;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "delta") @Getter
public class DeltaEntity {
    @Id @Column(length = 3, columnDefinition = "bpchar")
    String iata;
    @Enumerated(EnumType.STRING)
    DeltaStatus status;
    @Column(name="hub")
    Boolean isHub;
}
