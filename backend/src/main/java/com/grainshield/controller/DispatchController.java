package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.*;
import com.grainshield.enums.PlanStatus;
import com.grainshield.service.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {
    private final DispatchService service;
    @GetMapping public ApiResponse<List<PlanView>> list(){return ApiResponse.ok(service.list());}
    @PostMapping("/generate") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')") public ApiResponse<PlanView> generate(@Valid @RequestBody GeneratePlanRequest r){return ApiResponse.ok("调度建议已生成",service.generate(r));}
    @PutMapping("/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')") public ApiResponse<PlanView> status(@PathVariable Long id,@RequestParam PlanStatus status){return ApiResponse.ok(service.changeStatus(id,status));}
    @PutMapping("/{id}/adjust") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')") public ApiResponse<PlanView> adjust(@PathVariable Long id,@RequestBody AdjustPlanRequest r){return ApiResponse.ok(service.adjust(id,r));}
    @PostMapping("/{id}/feedback") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','NODE_MANAGER')") public ApiResponse<FeedbackView> feedback(@PathVariable Long id,@Valid @RequestBody FeedbackRequest r){return ApiResponse.ok(service.feedback(id,r));}
    @GetMapping("/{id}/feedback") public ApiResponse<List<FeedbackView>> feedbackList(@PathVariable Long id){return ApiResponse.ok(service.feedbackList(id));}
    @GetMapping("/{id}/export") @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')") public ResponseEntity<byte[]> export(@PathVariable Long id){byte[] body=("\uFEFF"+service.exportCsv(id)).getBytes(StandardCharsets.UTF_8);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=dispatch-plan-"+id+".csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(body);}
}
