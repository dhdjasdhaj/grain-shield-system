package com.grainshield.repository;

import com.grainshield.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByNodeId(Long nodeId);
    List<Inventory> findByGrainTypeIgnoreCase(String grainType);
    Optional<Inventory> findByNodeIdAndGrainTypeIgnoreCase(Long nodeId, String grainType);
}
