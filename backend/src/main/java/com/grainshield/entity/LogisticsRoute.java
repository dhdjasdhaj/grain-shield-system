package com.grainshield.entity;

import com.grainshield.enums.AlarmLevel;
import com.grainshield.enums.RouteStatus;
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
@Table(name = "logistics_route")
public class LogisticsRoute extends BaseEntity {
    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "start_node_id", nullable = false)
    private SupplyNode startNode;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "end_node_id", nullable = false)
    private SupplyNode endNode;

    @DecimalMin("0.1")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStatus status = RouteStatus.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmLevel riskLevel = AlarmLevel.LOW;

    @Column(length = 300)
    private String remark;
}
