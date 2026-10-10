package com.grainshield.service;

import com.grainshield.dto.BusinessDtos.*;
import com.grainshield.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ViewMapper {
    public NodeView node(SupplyNode n) {
        return new NodeView(n.getId(), n.getCode(), n.getName(), n.getType(), n.getRegion(), n.getAddress(),
                n.getCapacity(), n.getLongitude(), n.getLatitude(), n.getManagerName(), n.getPhone(), n.getStatus());
    }

    public InventoryView inventory(Inventory i) {
        BigDecimal bufferDays = BigDecimal.ZERO;
        if (i.getDailyConsumption() != null && i.getDailyConsumption().compareTo(BigDecimal.ZERO) > 0) {
            bufferDays = i.getAvailableStock().divide(i.getDailyConsumption(), 1, RoundingMode.HALF_UP);
        }
        return new InventoryView(i.getId(), i.getNode().getId(), i.getNode().getName(), i.getGrainType(), i.getUnit(),
                i.getCurrentStock(), i.getAvailableStock(), i.getSafetyStock(), i.getDailyConsumption(), bufferDays,
                i.getUpdatedAt());
    }

    public RouteView route(LogisticsRoute r) {
        return new RouteView(r.getId(), r.getCode(), r.getName(), r.getStartNode().getId(), r.getStartNode().getName(),
                r.getEndNode().getId(), r.getEndNode().getName(), r.getDistanceKm(), r.getStatus(), r.getRiskLevel(), r.getRemark());
    }

    public VehicleView vehicle(Vehicle v) {
        return new VehicleView(v.getId(), v.getPlateNumber(), v.getLoadCapacity(),
                v.getCurrentNode() == null ? null : v.getCurrentNode().getId(),
                v.getCurrentNode() == null ? null : v.getCurrentNode().getName(),
                v.getCurrentLocation(), v.getStatus());
    }

    public IotView iot(IotGrainData d) {
        return new IotView(d.getId(), d.getNode().getId(), d.getNode().getName(), d.getTemperature(), d.getHumidity(), d.getCollectedAt());
    }

    public TrafficView traffic(TrafficData d) {
        return new TrafficView(d.getId(), d.getRoute().getId(), d.getRoute().getName(), d.getRoadName(),
                d.getStatus(), d.getCongestionLevel(), d.getRecordedAt());
    }

    public AlertView alert(RiskAlert a) {
        return new AlertView(a.getId(), a.getCode(), a.getRule() == null ? null : a.getRule().getId(),
                a.getRule() == null ? null : a.getRule().getName(), a.getNode() == null ? null : a.getNode().getId(),
                a.getNode() == null ? null : a.getNode().getName(), a.getRoute() == null ? null : a.getRoute().getId(),
                a.getRoute() == null ? null : a.getRoute().getName(), a.getRiskType(), a.getLevel(), a.getDetail(),
                a.getSuggestion(), a.getStatus(), a.getHandledBy(), a.getHandledAt(), a.getAlarmTime());
    }

    public PlanView plan(DispatchPlan p) {
        return new PlanView(p.getId(), p.getCode(), p.getAlert().getId(), p.getAlert().getCode(),
                p.getSourceNode().getId(), p.getSourceNode().getName(), p.getTargetNode().getId(), p.getTargetNode().getName(),
                p.getGrainType(), p.getSuggestedQuantity(), p.getRoutePath(), p.getDistanceKm(),
                p.getVehicle() == null ? null : p.getVehicle().getId(), p.getVehicle() == null ? null : p.getVehicle().getPlateNumber(),
                p.getRecommendReason(), p.getStatus(), p.getCreatedBy(), p.getAdjustmentNote(), p.getCreatedAt());
    }

    public FeedbackView feedback(ExecutionFeedback f) {
        return new FeedbackView(f.getId(), f.getPlan().getId(), f.getFeedbackType(), f.getContent(),
                f.getExecutionStatus(), f.getFeedbackBy(), f.getFeedbackTime());
    }

    public UserView user(SysUser u) {
        List<Long> nodeIds = u.getManagedNodes().stream().map(SupplyNode::getId).sorted().toList();
        return new UserView(u.getId(), u.getUsername(), u.getRealName(), u.getRole(), u.isEnabled(), nodeIds);
    }
}
