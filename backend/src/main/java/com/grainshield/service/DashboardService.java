package com.grainshield.service;

import com.grainshield.dto.BusinessDtos.DashboardData;
import com.grainshield.enums.*;
import com.grainshield.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SupplyNodeRepository nodeRepository;private final LogisticsRouteRepository routeRepository;private final InventoryRepository inventoryRepository;private final VehicleRepository vehicleRepository;private final RiskAlertRepository alertRepository;private final DispatchPlanRepository planRepository;private final ViewMapper mapper;
    public DashboardData get(){
        Map<String,Long> kpis=new LinkedHashMap<>();kpis.put("nodes",nodeRepository.count());kpis.put("routes",routeRepository.count());kpis.put("openAlerts",alertRepository.countByStatusIn(List.of(AlertStatus.NEW,AlertStatus.CONFIRMED,AlertStatus.ANALYZING)));kpis.put("criticalAlerts",alertRepository.countByLevel(AlarmLevel.CRITICAL));kpis.put("plans",planRepository.count());kpis.put("availableVehicles",vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).stream().count());
        List<Map<String,Object>> distribution=new ArrayList<>();for(RiskType t:RiskType.values())distribution.add(Map.of("name",t.name(),"value",alertRepository.countByRiskType(t)));
        List<Map<String,Object>> trend=new ArrayList<>();for(int i=6;i>=0;i--){LocalDate day=LocalDate.now().minusDays(i);trend.add(Map.of("date",day.toString(),"count",alertRepository.countByAlarmTimeBetween(day.atStartOfDay(),day.plusDays(1).atStartOfDay())));}
        return new DashboardData(kpis,distribution,trend,nodeRepository.findAll().stream().map(mapper::node).toList(),routeRepository.findAll().stream().map(mapper::route).toList(),alertRepository.findTop20ByOrderByAlarmTimeDesc().stream().map(mapper::alert).toList());
    }
}
