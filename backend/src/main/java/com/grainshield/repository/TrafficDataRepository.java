package com.grainshield.repository;

import com.grainshield.entity.TrafficData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {
    Optional<TrafficData> findTopByRouteIdOrderByRecordedAtDesc(Long routeId);
    List<TrafficData> findTop100ByOrderByRecordedAtDesc();
}
