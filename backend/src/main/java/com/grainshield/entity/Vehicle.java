package com.grainshield.entity;

import com.grainshield.enums.VehicleStatus;
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
@Table(name = "vehicle")
public class Vehicle extends BaseEntity {
    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String plateNumber;

    @DecimalMin("0.1")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal loadCapacity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_node_id")
    private SupplyNode currentNode;

    @Column(length = 100)
    private String currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.AVAILABLE;
}
