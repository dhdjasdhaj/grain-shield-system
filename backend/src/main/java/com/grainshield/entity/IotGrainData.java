package com.grainshield.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "iot_grain_data", indexes = @Index(name = "idx_iot_node_time", columnList = "node_id,collected_at"))
public class IotGrainData extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id", nullable = false)
    private SupplyNode node;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;
}
