package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.VehicleRequest;
import com.grainshield.dto.BusinessDtos.VehicleView;
import com.grainshield.entity.Vehicle;
import com.grainshield.enums.VehicleStatus;
import com.grainshield.repository.SupplyNodeRepository;
import com.grainshield.repository.VehicleRepository;
import com.grainshield.service.OperationLogService;
import com.grainshield.service.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleRepository repository;
    private final SupplyNodeRepository nodeRepository;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @GetMapping public ApiResponse<List<VehicleView>> list() { return ApiResponse.ok(repository.findAll().stream().map(mapper::vehicle).toList()); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleView> create(@Valid @RequestBody VehicleRequest r) { Vehicle v = new Vehicle(); apply(v,r); v=repository.save(v); logService.log("车辆管理","新增车辆",v.getPlateNumber()); return ApiResponse.ok(mapper.vehicle(v)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VehicleView> update(@PathVariable Long id,@Valid @RequestBody VehicleRequest r) { Vehicle v=repository.findById(id).orElseThrow(()->new EntityNotFoundException("车辆不存在")); apply(v,r); v=repository.save(v); logService.log("车辆管理","修改车辆",v.getPlateNumber()); return ApiResponse.ok(mapper.vehicle(v)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id){repository.deleteById(id);logService.log("车辆管理","删除车辆","ID="+id);return ApiResponse.ok("删除成功");}
    private void apply(Vehicle v, VehicleRequest r){v.setPlateNumber(r.plateNumber());v.setLoadCapacity(r.loadCapacity());v.setCurrentNode(r.currentNodeId()==null?null:nodeRepository.findById(r.currentNodeId()).orElseThrow(()->new EntityNotFoundException("节点不存在")));v.setCurrentLocation(r.currentLocation());v.setStatus(r.status()==null? VehicleStatus.AVAILABLE:r.status());}
}
