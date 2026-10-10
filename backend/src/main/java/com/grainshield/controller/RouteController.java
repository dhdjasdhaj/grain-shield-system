package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.RouteRequest;
import com.grainshield.dto.BusinessDtos.RouteView;
import com.grainshield.entity.LogisticsRoute;
import com.grainshield.enums.AlarmLevel;
import com.grainshield.enums.RouteStatus;
import com.grainshield.repository.LogisticsRouteRepository;
import com.grainshield.repository.SupplyNodeRepository;
import com.grainshield.service.OperationLogService;
import com.grainshield.service.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private final LogisticsRouteRepository repository;
    private final SupplyNodeRepository nodeRepository;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @GetMapping public ApiResponse<List<RouteView>> list() { return ApiResponse.ok(repository.findAll().stream().map(mapper::route).toList()); }

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RouteView> create(@Valid @RequestBody RouteRequest r) {
        LogisticsRoute e = new LogisticsRoute(); apply(e, r); e = repository.save(e);
        logService.log("线路管理", "新增线路", e.getName()); return ApiResponse.ok(mapper.route(e));
    }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RouteView> update(@PathVariable Long id, @Valid @RequestBody RouteRequest r) {
        LogisticsRoute e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("线路不存在"));
        apply(e, r); e = repository.save(e); logService.log("线路管理", "修改线路", e.getName()); return ApiResponse.ok(mapper.route(e));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) { repository.deleteById(id); logService.log("线路管理", "删除线路", "ID="+id); return ApiResponse.ok("删除成功"); }

    private void apply(LogisticsRoute e, RouteRequest r) {
        if (r.startNodeId().equals(r.endNodeId())) throw new IllegalArgumentException("线路起点与终点不能相同");
        e.setCode(r.code()); e.setName(r.name());
        e.setStartNode(nodeRepository.findById(r.startNodeId()).orElseThrow(() -> new EntityNotFoundException("起点不存在")));
        e.setEndNode(nodeRepository.findById(r.endNodeId()).orElseThrow(() -> new EntityNotFoundException("终点不存在")));
        e.setDistanceKm(r.distanceKm()); e.setStatus(r.status() == null ? RouteStatus.NORMAL : r.status());
        e.setRiskLevel(r.riskLevel() == null ? AlarmLevel.LOW : r.riskLevel()); e.setRemark(r.remark());
    }
}
