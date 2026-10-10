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
@Table(name = "weather_data")
public class WeatherData extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String region;

    @Column(nullable = false, length = 50)
    private String disasterType;

    @Column(nullable = false, length = 20)
    private String warningLevel;

    @Column(precision = 8, scale = 2)
    private BigDecimal rainfall;

    @Column(length = 20)
    private String windLevel;

    @Column(nullable = false)
    private LocalDateTime publishedAt;
}
