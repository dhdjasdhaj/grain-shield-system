package com.grainshield.controller;
import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.DashboardData;
import com.grainshield.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
public class DashboardController {private final DashboardService service;@GetMapping public ApiResponse<DashboardData> get(){return ApiResponse.ok(service.get());}}
