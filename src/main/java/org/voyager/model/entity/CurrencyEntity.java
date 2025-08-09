package org.voyager.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "currencies")
public class CurrencyEntity {
    @Id @Column(name = "code", length = 3,
            columnDefinition = "bpchar")
    String code;
    String name;
    String symbol;

    @Column(name = "usd_rate", columnDefinition = "real")
    Double usdRate;

    @Column(name = "active")
    Boolean isActive;
}
