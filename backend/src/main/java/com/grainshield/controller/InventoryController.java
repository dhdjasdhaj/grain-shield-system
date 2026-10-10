package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.InventoryRequest;
import com.grainshield.dto.BusinessDtos.InventoryView;
import com.grainshield.entity.Inventory;
import com.grainshield.entity.SupplyNode;
import com.grainshield.entity.SysUser;
import com.grainshield.enums.Role;
import com.grainshield.repository.InventoryRepository;
import com.grainshield.repository.SupplyNodeRepository;
import com.grainshield.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryRepository repository;
    private final SupplyNodeRepository nodeRepository;
    private final CurrentUserService currentUserService;
    private final AccessService accessService;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @GetMapping
    public ApiResponse<List<InventoryView>> list(@RequestParam(required = false) Long nodeId) {
        SysUser user = currentUserService.requireCurrentUser();
        List<Inventory> data = nodeId == null ? repository.findAll() : repository.findByNodeId(nodeId);
        if (user.getRole() == Role.NODE_MANAGER) {
            var allowed = user.getManagedNodes().stream().map(SupplyNode::getId).collect(java.util.stream.Collectors.toSet());
            data = data.stream().filter(i -> allowed.contains(i.getNode().getId())).toList();
        }
        return ApiResponse.ok(data.stream().map(mapper::inventory).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','NODE_MANAGER')")
    public ApiResponse<InventoryView> create(@Valid @RequestBody InventoryRequest req) {
        accessService.requireNodeAccess(currentUserService.requireCurrentUser(), req.nodeId());
        Inventory i = new Inventory(); apply(i, req);
        i = repository.save(i);
        logService.log("库存管理", "新增库存", i.getNode().getName() + " / " + i.getGrainType());
        return ApiResponse.ok(mapper.inventory(i));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','NODE_MANAGER')")
    public ApiResponse<InventoryView> update(@PathVariable Long id, @Valid @RequestBody InventoryRequest req) {
        Inventory i = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("库存记录不存在"));
        accessService.requireNodeAccess(currentUserService.requireCurrentUser(), i.getNode().getId());
        accessService.requireNodeAccess(currentUserService.requireCurrentUser(), req.nodeId());
        apply(i, req); i = repository.save(i);
        logService.log("库存管理", "更新库存", i.getNode().getName() + " / " + i.getGrainType() + " 可用库存=" + i.getAvailableStock());
        return ApiResponse.ok(mapper.inventory(i));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        repository.deleteById(id); logService.log("库存管理", "删除库存", "ID=" + id); return ApiResponse.ok("删除成功");
    }

    private void apply(Inventory i, InventoryRequest r) {
        SupplyNode node = nodeRepository.findById(r.nodeId()).orElseThrow(() -> new EntityNotFoundException("节点不存在"));
        i.setNode(node); i.setGrainType(r.grainType()); i.setUnit(r.unit() == null || r.unit().isBlank() ? "吨" : r.unit());
        i.setCurrentStock(nz(r.currentStock())); i.setAvailableStock(nz(r.availableStock()));
        i.setSafetyStock(nz(r.safetyStock())); i.setDailyConsumption(nz(r.dailyConsumption()));
        if (i.getAvailableStock().compareTo(i.getCurrentStock()) > 0) throw new IllegalArgumentException("可用库存不能大于当前库存");
    }
    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
