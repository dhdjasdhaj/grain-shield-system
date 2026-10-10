package com.grainshield.repository;

import com.grainshield.entity.DispatchPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispatchPlanRepository extends JpaRepository<DispatchPlan, Long> {
    List<DispatchPlan> findAllByOrderByCreatedAtDesc();
}
