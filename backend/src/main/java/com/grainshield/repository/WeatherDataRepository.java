package com.grainshield.repository;

import com.grainshield.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    List<WeatherData> findTop100ByOrderByPublishedAtDesc();
}
