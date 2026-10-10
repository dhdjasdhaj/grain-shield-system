package com.grainshield.repository;

import com.grainshield.entity.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {
    List<RiskRule> findByEnabledTrue();
}
