package com.grainshield.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(name = "uk_inventory_node_grain", columnNames = {"node_id", "grain_type"}))
public class Inventory extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id", nullable = false)
    private SupplyNode node;

    @NotBlank
    @Column(name = "grain_type", nullable = false, length = 40)
    private String grainType;

    @Column(nullable = false, length = 20)
    private String unit = "吨";

    @DecimalMin("0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @DecimalMin("0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal availableStock = BigDecimal.ZERO;

    @DecimalMin("0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal safetyStock = BigDecimal.ZERO;

    @DecimalMin("0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyConsumption = BigDecimal.ZERO;
}
