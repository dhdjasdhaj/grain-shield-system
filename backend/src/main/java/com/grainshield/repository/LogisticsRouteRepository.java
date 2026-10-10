package com.grainshield.repository;

import com.grainshield.entity.LogisticsRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogisticsRouteRepository extends JpaRepository<LogisticsRoute, Long> {
    List<LogisticsRoute> findByStartNodeIdOrEndNodeId(Long startNodeId, Long endNodeId);
}
