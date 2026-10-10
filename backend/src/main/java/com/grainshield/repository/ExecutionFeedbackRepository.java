package com.grainshield.repository;

import com.grainshield.entity.ExecutionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionFeedbackRepository extends JpaRepository<ExecutionFeedback, Long> {
    List<ExecutionFeedback> findByPlanIdOrderByFeedbackTimeDesc(Long planId);
}
