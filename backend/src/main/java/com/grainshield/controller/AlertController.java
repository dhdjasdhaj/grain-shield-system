package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.*;
import com.grainshield.entity.RiskAlert;
import com.grainshield.entity.SysUser;
import com.grainshield.enums.*;
import com.grainshield.repository.RiskAlertRepository;
import com.grainshield.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final RiskAlertRepository repository;
    private final RiskScanService scanService;
    private final CurrentUserService currentUserService;
    private final OperationLogService logService;
    private final ViewMapper mapper;

    @GetMapping
    public ApiResponse<List<AlertView>> list(@RequestParam(required=false) AlarmLevel level,@RequestParam(required=false) RiskType riskType,@RequestParam(required=false) AlertStatus status){
        var stream=repository.findAll().stream();SysUser user=currentUserService.requireCurrentUser();if(user.getRole()==Role.NODE_MANAGER){Set<Long> allowed=user.getManagedNodes().stream().map(n->n.getId()).collect(Collectors.toSet());stream=stream.filter(a->(a.getNode()!=null&&allowed.contains(a.getNode().getId()))||(a.getRoute()!=null&&(allowed.contains(a.getRoute().getStartNode().getId())||allowed.contains(a.getRoute().getEndNode().getId()))));}if(level!=null)stream=stream.filter(a->a.getLevel()==level);if(riskType!=null)stream=stream.filter(a->a.getRiskType()==riskType);if(status!=null)stream=stream.filter(a->a.getStatus()==status);return ApiResponse.ok(stream.sorted(Comparator.comparing(RiskAlert::getAlarmTime).reversed()).map(mapper::alert).toList());
    }
    @PostMapping("/scan") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')") public ApiResponse<ScanResult> scan(){return ApiResponse.ok("风险扫描完成",scanService.scanAll(currentUserService.username()));}
    @PutMapping("/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ApiResponse<AlertView> handle(@PathVariable Long id,@Valid @RequestBody AlertHandleRequest req){RiskAlert a=repository.findById(id).orElseThrow(()->new EntityNotFoundException("告警不存在"));a.setStatus(req.status());a.setHandledBy(currentUserService.username());a.setHandledAt(LocalDateTime.now());a=repository.save(a);logService.log("风险告警","更新告警状态",a.getCode()+" -> "+req.status());return ApiResponse.ok(mapper.alert(a));}
}
