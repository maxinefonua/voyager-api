package org.voyager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;

@Entity
@Table(name = "delta") @Getter
public class Delta {
    @Id @Column(length = 3, columnDefinition = "bpchar")
    String iata;
    @Enumerated(EnumType.STRING)
    Status status;
    @Column(name="hub")
    Boolean isHub;
}
