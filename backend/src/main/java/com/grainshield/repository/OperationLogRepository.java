package com.grainshield.repository;

import com.grainshield.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    List<OperationLog> findTop200ByOrderByOperationTimeDesc();
}
