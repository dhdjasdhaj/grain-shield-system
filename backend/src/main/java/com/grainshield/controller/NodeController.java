package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.NodeRequest;
import com.grainshield.dto.BusinessDtos.NodeView;
import com.grainshield.entity.SupplyNode;
import com.grainshield.enums.NodeStatus;
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
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {
    private final SupplyNodeRepository repository;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @GetMapping
    public ApiResponse<List<NodeView>> list() {
        return ApiResponse.ok(repository.findAll().stream().map(mapper::node).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NodeView> create(@Valid @RequestBody NodeRequest req) {
        SupplyNode n = new SupplyNode();
        apply(n, req);
        n = repository.save(n);
        logService.log("基础数据", "新增节点", n.getName());
        return ApiResponse.ok(mapper.node(n));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NodeView> update(@PathVariable Long id, @Valid @RequestBody NodeRequest req) {
        SupplyNode n = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("节点不存在"));
        apply(n, req);
        n = repository.save(n);
        logService.log("基础数据", "修改节点", n.getName());
        return ApiResponse.ok(mapper.node(n));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        SupplyNode n = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("节点不存在"));
        repository.delete(n);
        logService.log("基础数据", "删除节点", n.getName());
        return ApiResponse.ok("删除成功");
    }

    private void apply(SupplyNode n, NodeRequest r) {
        n.setCode(r.code()); n.setName(r.name()); n.setType(r.type()); n.setRegion(r.region());
        n.setAddress(r.address()); n.setCapacity(r.capacity()); n.setLongitude(r.longitude()); n.setLatitude(r.latitude());
        n.setManagerName(r.managerName()); n.setPhone(r.phone()); n.setStatus(r.status() == null ? NodeStatus.NORMAL : r.status());
    }
}
