package com.grainshield.repository;

import com.grainshield.entity.IotGrainData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IotGrainDataRepository extends JpaRepository<IotGrainData, Long> {
    Optional<IotGrainData> findTopByNodeIdOrderByCollectedAtDesc(Long nodeId);
    List<IotGrainData> findTop100ByOrderByCollectedAtDesc();
}
