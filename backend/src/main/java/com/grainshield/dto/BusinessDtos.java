package com.grainshield.dto;

import com.grainshield.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class BusinessDtos {
    private BusinessDtos() {}

    public record NodeRequest(@NotBlank String code, @NotBlank String name, @NotNull NodeType type,
                              @NotBlank String region, String address, @PositiveOrZero BigDecimal capacity,
                              BigDecimal longitude, BigDecimal latitude, String managerName, String phone,
                              NodeStatus status) {}
    public record NodeView(Long id, String code, String name, NodeType type, String region, String address,
                           BigDecimal capacity, BigDecimal longitude, BigDecimal latitude, String managerName,
                           String phone, NodeStatus status) {}

    public record InventoryRequest(@NotNull Long nodeId, @NotBlank String grainType, String unit,
                                   @PositiveOrZero BigDecimal currentStock,
                                   @PositiveOrZero BigDecimal availableStock,
                                   @PositiveOrZero BigDecimal safetyStock,
                                   @PositiveOrZero BigDecimal dailyConsumption) {}
    public record InventoryView(Long id, Long nodeId, String nodeName, String grainType, String unit,
                                BigDecimal currentStock, BigDecimal availableStock, BigDecimal safetyStock,
                                BigDecimal dailyConsumption, BigDecimal bufferDays, LocalDateTime updatedAt) {}

    public record RouteRequest(@NotBlank String code, @NotBlank String name, @NotNull Long startNodeId,
                               @NotNull Long endNodeId, @Positive BigDecimal distanceKm,
                               RouteStatus status, AlarmLevel riskLevel, String remark) {}
    public record RouteView(Long id, String code, String name, Long startNodeId, String startNodeName,
                            Long endNodeId, String endNodeName, BigDecimal distanceKm,
                            RouteStatus status, AlarmLevel riskLevel, String remark) {}

    public record VehicleRequest(@NotBlank String plateNumber, @Positive BigDecimal loadCapacity,
                                 Long currentNodeId, String currentLocation, VehicleStatus status) {}
    public record VehicleView(Long id, String plateNumber, BigDecimal loadCapacity, Long currentNodeId,
                              String currentNodeName, String currentLocation, VehicleStatus status) {}

    public record RuleRequest(@NotBlank String name, @NotNull RiskType riskType, @NotBlank String metric,
                              @NotBlank String operator, BigDecimal thresholdValue, String thresholdText,
                              @NotNull AlarmLevel alarmLevel, @NotBlank String suggestion, Boolean enabled) {}

    public record IotRequest(@NotNull Long nodeId, @NotNull BigDecimal temperature,
                             @NotNull BigDecimal humidity, LocalDateTime collectedAt) {}
    public record IotView(Long id, Long nodeId, String nodeName, BigDecimal temperature,
                          BigDecimal humidity, LocalDateTime collectedAt) {}

    public record WeatherRequest(@NotBlank String region, @NotBlank String disasterType,
                                 @NotBlank String warningLevel, BigDecimal rainfall,
                                 String windLevel, LocalDateTime publishedAt) {}

    public record TrafficRequest(@NotNull Long routeId, String roadName, @NotNull RouteStatus status,
                                 String congestionLevel, LocalDateTime recordedAt) {}
    public record TrafficView(Long id, Long routeId, String routeName, String roadName,
                              RouteStatus status, String congestionLevel, LocalDateTime recordedAt) {}

    public record AlertView(Long id, String code, Long ruleId, String ruleName, Long nodeId, String nodeName,
                            Long routeId, String routeName, RiskType riskType, AlarmLevel level,
                            String detail, String suggestion, AlertStatus status,
                            String handledBy, LocalDateTime handledAt, LocalDateTime alarmTime) {}
    public record AlertHandleRequest(@NotNull AlertStatus status) {}
    public record ScanResult(int scannedRules, int generatedAlerts, List<AlertView> alerts) {}

    public record GeneratePlanRequest(@NotNull Long alertId, String grainType, BigDecimal demand) {}
    public record AdjustPlanRequest(BigDecimal suggestedQuantity, Long vehicleId, String adjustmentNote) {}
    public record PlanView(Long id, String code, Long alertId, String alertCode, Long sourceNodeId,
                           String sourceNodeName, Long targetNodeId, String targetNodeName, String grainType,
                           BigDecimal suggestedQuantity, String routePath, BigDecimal distanceKm,
                           Long vehicleId, String plateNumber, String recommendReason, PlanStatus status,
                           String createdBy, String adjustmentNote, LocalDateTime createdAt) {}
    public record FeedbackRequest(@NotBlank String feedbackType, @NotBlank String content,
                                  @NotBlank String executionStatus) {}
    public record FeedbackView(Long id, Long planId, String feedbackType, String content,
                               String executionStatus, String feedbackBy, LocalDateTime feedbackTime) {}

    public record UserRequest(@NotBlank String username, String password, @NotBlank String realName,
                              @NotNull Role role, Boolean enabled, List<Long> managedNodeIds) {}
    public record UserView(Long id, String username, String realName, Role role,
                           boolean enabled, List<Long> managedNodeIds) {}

    public record DashboardData(Map<String, Long> kpis, List<Map<String, Object>> riskDistribution,
                                List<Map<String, Object>> trend, List<NodeView> nodes,
                                List<RouteView> routes, List<AlertView> latestAlerts) {}
}
