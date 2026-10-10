package com.grainshield.entity;

import com.grainshield.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "traffic_data")
public class TrafficData extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id", nullable = false)
    private LogisticsRoute route;

    @Column(nullable = false, length = 100)
    private String roadName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStatus status;

    @Column(length = 20)
    private String congestionLevel;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
