package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.entity.SystemConfig;
import com.grainshield.repository.SystemConfigRepository;
import com.grainshield.service.OperationLogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {
    private final SystemConfigRepository repository;
    private final OperationLogService logService;
    @GetMapping public ApiResponse<List<SystemConfig>> list(){return ApiResponse.ok(repository.findAll());}
    @PostMapping public ApiResponse<SystemConfig> create(@Valid @RequestBody SystemConfig c){c.setId(null);SystemConfig saved=repository.save(c);logService.log("系统配置","新增配置",saved.getConfigKey());return ApiResponse.ok(saved);}
    @PutMapping("/{id}") public ApiResponse<SystemConfig> update(@PathVariable Long id,@Valid @RequestBody SystemConfig c){SystemConfig old=repository.findById(id).orElseThrow(()->new EntityNotFoundException("配置不存在"));old.setConfigKey(c.getConfigKey());old.setConfigValue(c.getConfigValue());old.setDescription(c.getDescription());old.setEnabled(c.isEnabled());old=repository.save(old);logService.log("系统配置","修改配置",old.getConfigKey());return ApiResponse.ok(old);}
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id){repository.deleteById(id);return ApiResponse.ok("删除成功");}
}
