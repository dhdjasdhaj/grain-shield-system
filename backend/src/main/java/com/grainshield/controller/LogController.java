package com.grainshield.controller;
import com.grainshield.dto.ApiResponse;
import com.grainshield.entity.OperationLog;
import com.grainshield.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/logs") @RequiredArgsConstructor
public class LogController {private final OperationLogRepository repository;@GetMapping public ApiResponse<List<OperationLog>> list(){return ApiResponse.ok(repository.findTop200ByOrderByOperationTimeDesc());}}
