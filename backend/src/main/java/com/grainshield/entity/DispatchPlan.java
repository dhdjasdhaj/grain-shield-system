package com.grainshield.entity;

import com.grainshield.enums.PlanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dispatch_plan")
public class DispatchPlan extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "alert_id", nullable = false)
    private RiskAlert alert;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "source_node_id", nullable = false)
    private SupplyNode sourceNode;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "target_node_id", nullable = false)
    private SupplyNode targetNode;

    @Column(nullable = false, length = 40)
    private String grainType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal suggestedQuantity;

    @Column(nullable = false, length = 800)
    private String routePath;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(nullable = false, length = 1000)
    private String recommendReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 800)
    private String adjustmentNote;
}
