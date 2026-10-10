package com.grainshield.repository;

import com.grainshield.entity.RiskAlert;
import com.grainshield.enums.AlertStatus;
import com.grainshield.enums.AlarmLevel;
import com.grainshield.enums.RiskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    List<RiskAlert> findTop20ByOrderByAlarmTimeDesc();
    long countByStatusIn(List<AlertStatus> statuses);
    long countByLevel(AlarmLevel level);
    long countByRiskType(RiskType riskType);
    long countByAlarmTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("select count(a) > 0 from RiskAlert a where a.rule.id = :ruleId " +
            "and ((:nodeId is null and a.node is null) or a.node.id = :nodeId) " +
            "and ((:routeId is null and a.route is null) or a.route.id = :routeId) " +
            "and a.status in :statuses")
    boolean hasOpenAlert(@Param("ruleId") Long ruleId,
                         @Param("nodeId") Long nodeId,
                         @Param("routeId") Long routeId,
                         @Param("statuses") List<AlertStatus> statuses);
}
