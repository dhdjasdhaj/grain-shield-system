package com.grainshield.repository;

import com.grainshield.entity.SupplyNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyNodeRepository extends JpaRepository<SupplyNode, Long> {
    boolean existsByCode(String code);
}
