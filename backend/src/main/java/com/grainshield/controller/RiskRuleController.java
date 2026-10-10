package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.RuleRequest;
import com.grainshield.entity.RiskRule;
import com.grainshield.repository.RiskRuleRepository;
import com.grainshield.service.OperationLogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RiskRuleController {
    private final RiskRuleRepository repository;
    private final OperationLogService logService;
    @GetMapping public ApiResponse<List<RiskRule>> list(){return ApiResponse.ok(repository.findAll());}
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public ApiResponse<RiskRule> create(@Valid @RequestBody RuleRequest r){RiskRule e=new RiskRule();apply(e,r);e=repository.save(e);logService.log("风险规则","新增规则",e.getName());return ApiResponse.ok(e);}
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<RiskRule> update(@PathVariable Long id,@Valid @RequestBody RuleRequest r){RiskRule e=repository.findById(id).orElseThrow(()->new EntityNotFoundException("规则不存在"));apply(e,r);e=repository.save(e);logService.log("风险规则","修改规则",e.getName());return ApiResponse.ok(e);}
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<Void> delete(@PathVariable Long id){repository.deleteById(id);logService.log("风险规则","删除规则","ID="+id);return ApiResponse.ok("删除成功");}
    private void apply(RiskRule e,RuleRequest r){e.setName(r.name());e.setRiskType(r.riskType());e.setMetric(r.metric());e.setOperator(r.operator().toUpperCase());e.setThresholdValue(r.thresholdValue());e.setThresholdText(r.thresholdText());e.setAlarmLevel(r.alarmLevel());e.setSuggestion(r.suggestion());e.setEnabled(r.enabled()==null||r.enabled());}
}
