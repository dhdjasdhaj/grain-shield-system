package com.grainshield.entity;

import com.grainshield.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "risk_alert", indexes = {@Index(name = "idx_alert_status_time", columnList = "status,alarm_time")})
public class RiskAlert extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_id")
    private RiskRule rule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id")
    private SupplyNode node;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id")
    private LogisticsRoute route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RiskType riskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmLevel level;

    @Column(nullable = false, length = 800)
    private String detail;

    @Column(nullable = false, length = 400)
    private String suggestion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.NEW;

    @Column(length = 50)
    private String handledBy;

    private LocalDateTime handledAt;

    @Column(name = "alarm_time", nullable = false)
    private LocalDateTime alarmTime;
}
