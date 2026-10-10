package com.grainshield.service;

import com.grainshield.dto.BusinessDtos.AlertView;
import com.grainshield.dto.BusinessDtos.ScanResult;
import com.grainshield.entity.*;
import com.grainshield.enums.*;
import com.grainshield.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RiskScanService {
    private final RiskRuleRepository ruleRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplyNodeRepository nodeRepository;
    private final LogisticsRouteRepository routeRepository;
    private final IotGrainDataRepository iotRepository;
    private final WeatherDataRepository weatherRepository;
    private final TrafficDataRepository trafficRepository;
    private final RiskAlertRepository alertRepository;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @Value("${app.scan.scheduled:false}") private boolean scheduledEnabled;

    @Transactional
    public ScanResult scanAll(String operator) {
        List<RiskRule> rules = ruleRepository.findByEnabledTrue();
        List<RiskAlert> generated = new ArrayList<>();
        for (RiskRule rule : rules) {
            switch (rule.getRiskType()) {
                case INVENTORY, REGIONAL_SUPPLY -> scanInventory(rule, generated);
                case TEMPERATURE, HUMIDITY -> scanIot(rule, generated);
                case TRAFFIC -> scanTraffic(rule, generated);
                case WEATHER -> scanWeather(rule, generated);
            }
        }
        if (!generated.isEmpty()) logService.log("风险预警", "执行规则扫描", "扫描规则"+rules.size()+"条，新增告警"+generated.size()+"条，执行人="+operator);
        return new ScanResult(rules.size(), generated.size(), generated.stream().map(mapper::alert).toList());
    }

    @Scheduled(cron = "${app.scan.cron:0 */10 * * * *}")
    public void scheduledScan() { if (scheduledEnabled) scanAll("scheduled"); }

    private void scanInventory(RiskRule rule, List<RiskAlert> out) {
        for (Inventory i : inventoryRepository.findAll()) {
            BigDecimal threshold = rule.getThresholdValue() == null ? i.getSafetyStock() : rule.getThresholdValue();
            if (compare(i.getAvailableStock(), rule.getOperator(), threshold)) {
                create(rule, i.getNode(), null,
                        "节点“"+i.getNode().getName()+"”的"+i.getGrainType()+"可用库存为"+i.getAvailableStock()+i.getUnit()+"，触发阈值"+rule.getOperator()+threshold,
                        out);
            }
        }
    }

    private void scanIot(RiskRule rule, List<RiskAlert> out) {
        for (SupplyNode node : nodeRepository.findAll()) {
            iotRepository.findTopByNodeIdOrderByCollectedAtDesc(node.getId()).ifPresent(d -> {
                BigDecimal actual = rule.getRiskType() == RiskType.TEMPERATURE ? d.getTemperature() : d.getHumidity();
                if (rule.getThresholdValue()!=null && compare(actual,rule.getOperator(),rule.getThresholdValue())) {
                    String unit = rule.getRiskType()==RiskType.TEMPERATURE?"℃":"%";
                    create(rule,node,null,"节点“"+node.getName()+"”最新"+(rule.getRiskType()==RiskType.TEMPERATURE?"温度":"湿度")+"为"+actual+unit+"，超过安全阈值",out);
                }
            });
        }
    }

    private void scanTraffic(RiskRule rule, List<RiskAlert> out) {
        for (LogisticsRoute route : routeRepository.findAll()) {
            RouteStatus status = trafficRepository.findTopByRouteIdOrderByRecordedAtDesc(route.getId()).map(TrafficData::getStatus).orElse(route.getStatus());
            String expected = Optional.ofNullable(rule.getThresholdText()).orElse("CLOSED").toUpperCase();
            boolean matched = textCompare(status.name(), rule.getOperator(), expected);
            if (matched) create(rule,null,route,"运输线路“"+route.getName()+"”当前状态为"+status+"，可能造成供应链阻断",out);
        }
    }

    private void scanWeather(RiskRule rule, List<RiskAlert> out) {
        for (WeatherData d : weatherRepository.findTop100ByOrderByPublishedAtDesc()) {
            String target = Optional.ofNullable(rule.getThresholdText()).orElse("红");
            if (textCompare(d.getWarningLevel(),rule.getOperator(),target)) {
                create(rule,null,null,d.getRegion()+"发布"+d.getDisasterType()+d.getWarningLevel()+"预警，可能影响区域粮食运输与供应",out);
            }
        }
    }

    private void create(RiskRule rule, SupplyNode node, LogisticsRoute route, String detail, List<RiskAlert> out) {
        List<AlertStatus> open = List.of(AlertStatus.NEW,AlertStatus.CONFIRMED,AlertStatus.ANALYZING);
        if (alertRepository.hasOpenAlert(rule.getId(),node==null?null:node.getId(),route==null?null:route.getId(),open)) return;
        RiskAlert a=new RiskAlert();a.setCode("AL"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+UUID.randomUUID().toString().substring(0,4).toUpperCase());a.setRule(rule);a.setNode(node);a.setRoute(route);a.setRiskType(rule.getRiskType());a.setLevel(rule.getAlarmLevel());a.setDetail(detail);a.setSuggestion(rule.getSuggestion());a.setStatus(AlertStatus.NEW);a.setAlarmTime(LocalDateTime.now());out.add(alertRepository.save(a));
    }

    private boolean compare(BigDecimal a,String op,BigDecimal b){if(a==null||b==null)return false;int c=a.compareTo(b);return switch(op.toUpperCase()){case "LT","<"->c<0;case "LE","<="->c<=0;case "GT",">"->c>0;case "GE",">="->c>=0;case "EQ","="->c==0;default->false;};}
    private boolean textCompare(String actual,String op,String expected){if(actual==null)return false;return switch(op.toUpperCase()){case "EQ","="->actual.equalsIgnoreCase(expected);case "CONTAINS"->actual.toUpperCase().contains(expected.toUpperCase());case "NE","!="->!actual.equalsIgnoreCase(expected);default->actual.equalsIgnoreCase(expected);};}
}
