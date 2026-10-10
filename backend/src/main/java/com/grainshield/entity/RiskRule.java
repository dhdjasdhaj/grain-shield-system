package com.grainshield.entity;

import com.grainshield.enums.AlarmLevel;
import com.grainshield.enums.RiskType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "risk_rule")
public class RiskRule extends BaseEntity {
    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RiskType riskType;

    @Column(nullable = false, length = 40)
    private String metric;

    @Column(nullable = false, length = 10)
    private String operator;

    @Column(precision = 12, scale = 2)
    private BigDecimal thresholdValue;

    @Column(length = 100)
    private String thresholdText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmLevel alarmLevel;

    @Column(nullable = false, length = 300)
    private String suggestion;

    @Column(nullable = false)
    private boolean enabled = true;
}
