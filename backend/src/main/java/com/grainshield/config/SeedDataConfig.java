package com.grainshield.config;

import com.grainshield.entity.*;
import com.grainshield.enums.*;
import com.grainshield.repository.*;
import com.grainshield.service.RiskScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {
    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner seedData(SysUserRepository users, SupplyNodeRepository nodes, InventoryRepository inventories,
                               LogisticsRouteRepository routes, VehicleRepository vehicles, RiskRuleRepository rules,
                               IotGrainDataRepository iot, WeatherDataRepository weather, TrafficDataRepository traffic,
                               SystemConfigRepository configs, RiskScanService scanService) {
        return args -> {
            if (nodes.count() > 0) return;

            SupplyNode n1=node("WH-001","武汉中央储备粮库",NodeType.WAREHOUSE,"东西湖区","革新大道168号",12000,114.137,30.621);
            SupplyNode n2=node("WH-002","江夏区粮食储备库",NodeType.WAREHOUSE,"江夏区","纸坊街道粮安路8号",8000,114.321,30.375);
            SupplyNode n3=node("WH-003","黄陂区粮食储备库",NodeType.WAREHOUSE,"黄陂区","前川街仓储大道16号",9000,114.376,30.882);
            SupplyNode n4=node("PR-001","武汉粮油加工中心",NodeType.PROCESSING,"蔡甸区","常福工业园粮油路6号",5000,114.030,30.487);
            SupplyNode n5=node("DS-001","武昌民生配送中心",NodeType.DISTRIBUTION,"武昌区","白沙洲大道88号",1800,114.311,30.520);
            SupplyNode n6=node("DS-002","汉口应急配送中心",NodeType.DISTRIBUTION,"江汉区","发展大道99号",1600,114.261,30.612);
            nodes.saveAll(List.of(n1,n2,n3,n4,n5,n6));

            inventories.saveAll(List.of(
                    inventory(n1,"稻谷",4200,3900,800,120), inventory(n1,"小麦",2100,1900,500,80),
                    inventory(n2,"稻谷",3100,2800,700,100), inventory(n2,"小麦",1500,1350,400,65),
                    inventory(n3,"稻谷",3600,3300,750,105), inventory(n3,"玉米",1200,1000,350,55),
                    inventory(n4,"稻谷",900,760,350,90), inventory(n4,"小麦",620,520,300,70),
                    inventory(n5,"稻谷",130,80,200,45), inventory(n5,"小麦",280,240,180,50),
                    inventory(n6,"稻谷",360,310,220,55), inventory(n6,"小麦",95,70,160,42)
            ));

            LogisticsRoute r1=route("R-001","东西湖—江汉干线",n1,n6,28,RouteStatus.NORMAL);
            LogisticsRoute r2=route("R-002","江汉—武昌配送线",n6,n5,18,RouteStatus.CLOSED);
            LogisticsRoute r3=route("R-003","东西湖—蔡甸联络线",n1,n4,35,RouteStatus.NORMAL);
            LogisticsRoute r4=route("R-004","蔡甸—武昌应急线",n4,n5,31,RouteStatus.NORMAL);
            LogisticsRoute r5=route("R-005","江夏—武昌补给线",n2,n5,24,RouteStatus.NORMAL);
            LogisticsRoute r6=route("R-006","黄陂—江汉补给线",n3,n6,39,RouteStatus.CONGESTED);
            LogisticsRoute r7=route("R-007","黄陂—东西湖联络线",n3,n1,42,RouteStatus.NORMAL);
            LogisticsRoute r8=route("R-008","江夏—蔡甸联络线",n2,n4,33,RouteStatus.NORMAL);
            routes.saveAll(List.of(r1,r2,r3,r4,r5,r6,r7,r8));

            vehicles.saveAll(List.of(vehicle("鄂A·GS101",28,n1),vehicle("鄂A·GS202",35,n2),vehicle("鄂A·GS303",30,n3),vehicle("鄂A·GS404",20,n4)));

            RiskRule rr1=rule("库存低于安全阈值",RiskType.INVENTORY,"availableStock","LT",new BigDecimal("120"),null,AlarmLevel.HIGH,"核验目标节点库存与日均消耗，必要时转入调度建议分析。");
            RiskRule rr2=rule("仓储高温预警",RiskType.TEMPERATURE,"temperature","GT",new BigDecimal("30"),null,AlarmLevel.HIGH,"立即检查通风降温设备并开展粮情复核。");
            RiskRule rr3=rule("仓储高湿预警",RiskType.HUMIDITY,"humidity","GT",new BigDecimal("80"),null,AlarmLevel.MEDIUM,"启动除湿与通风作业，持续观察粮情变化。");
            RiskRule rr4=rule("道路封闭阻断预警",RiskType.TRAFFIC,"routeStatus","EQ",null,"CLOSED",AlarmLevel.CRITICAL,"核实封闭范围，排除该线路并生成备选运输方案。");
            RiskRule rr5=rule("红色气象灾害预警",RiskType.WEATHER,"warningLevel","CONTAINS",null,"红",AlarmLevel.CRITICAL,"启动极端天气应急响应，核查受影响区域与运输节点。");
            rules.saveAll(List.of(rr1,rr2,rr3,rr4,rr5));

            iot.saveAll(List.of(iot(n1,24.5,66),iot(n2,31.8,72),iot(n3,26.2,83),iot(n4,25.1,69)));
            WeatherData wd=new WeatherData();wd.setRegion("武汉市江夏区");wd.setDisasterType("暴雨");wd.setWarningLevel("红色");wd.setRainfall(new BigDecimal("92.5"));wd.setWindLevel("7级");wd.setPublishedAt(LocalDateTime.now().minusMinutes(35));weather.save(wd);
            TrafficData td=new TrafficData();td.setRoute(r2);td.setRoadName("二环线武昌段");td.setStatus(RouteStatus.CLOSED);td.setCongestionLevel("封闭施工");td.setRecordedAt(LocalDateTime.now().minusMinutes(20));traffic.save(td);

            if(users.count()==0){SysUser admin=user("admin","系统管理员",Role.ADMIN,"admin123");SysUser dispatcher=user("dispatcher","市级调度指挥员",Role.DISPATCHER,"dispatch123");SysUser manager=user("node","江夏节点管理员",Role.NODE_MANAGER,"node123");manager.setManagedNodes(new HashSet<>(List.of(n2)));users.saveAll(List.of(admin,dispatcher,manager));}

            configs.saveAll(List.of(config("system.name","Grain Shield 粮食供应链网络风险预警与智能调度系统","系统名称"),config("weather.api.enabled","false","课程原型使用模拟气象数据"),config("traffic.api.enabled","false","课程原型使用模拟交通数据"),config("scan.interval.minutes","10","自动风险扫描间隔")));
            scanService.scanAll("system-seed");
        };
    }

    private SupplyNode node(String code,String name,NodeType type,String region,String address,double capacity,double lng,double lat){SupplyNode n=new SupplyNode();n.setCode(code);n.setName(name);n.setType(type);n.setRegion(region);n.setAddress(address);n.setCapacity(BigDecimal.valueOf(capacity));n.setLongitude(BigDecimal.valueOf(lng));n.setLatitude(BigDecimal.valueOf(lat));n.setManagerName("值班负责人");n.setPhone("027-8888"+code.substring(code.length()-3));n.setStatus(NodeStatus.NORMAL);return n;}
    private Inventory inventory(SupplyNode n,String grain,double current,double available,double safety,double daily){Inventory i=new Inventory();i.setNode(n);i.setGrainType(grain);i.setUnit("吨");i.setCurrentStock(BigDecimal.valueOf(current));i.setAvailableStock(BigDecimal.valueOf(available));i.setSafetyStock(BigDecimal.valueOf(safety));i.setDailyConsumption(BigDecimal.valueOf(daily));return i;}
    private LogisticsRoute route(String code,String name,SupplyNode start,SupplyNode end,double distance,RouteStatus status){LogisticsRoute r=new LogisticsRoute();r.setCode(code);r.setName(name);r.setStartNode(start);r.setEndNode(end);r.setDistanceKm(BigDecimal.valueOf(distance));r.setStatus(status);r.setRiskLevel(status==RouteStatus.CLOSED?AlarmLevel.CRITICAL:status==RouteStatus.CONGESTED?AlarmLevel.MEDIUM:AlarmLevel.LOW);r.setRemark("课程演示模拟线路");return r;}
    private Vehicle vehicle(String plate,double capacity,SupplyNode node){Vehicle v=new Vehicle();v.setPlateNumber(plate);v.setLoadCapacity(BigDecimal.valueOf(capacity));v.setCurrentNode(node);v.setCurrentLocation(node.getName());v.setStatus(VehicleStatus.AVAILABLE);return v;}
    private RiskRule rule(String name,RiskType type,String metric,String op,BigDecimal value,String text,AlarmLevel level,String suggestion){RiskRule r=new RiskRule();r.setName(name);r.setRiskType(type);r.setMetric(metric);r.setOperator(op);r.setThresholdValue(value);r.setThresholdText(text);r.setAlarmLevel(level);r.setSuggestion(suggestion);r.setEnabled(true);return r;}
    private IotGrainData iot(SupplyNode node,double temp,double humidity){IotGrainData d=new IotGrainData();d.setNode(node);d.setTemperature(BigDecimal.valueOf(temp));d.setHumidity(BigDecimal.valueOf(humidity));d.setCollectedAt(LocalDateTime.now().minusMinutes((long)(Math.random()*60)));return d;}
    private SysUser user(String username,String realName,Role role,String password){SysUser u=new SysUser();u.setUsername(username);u.setRealName(realName);u.setRole(role);u.setPasswordHash(encoder.encode(password));u.setEnabled(true);return u;}
    private SystemConfig config(String key,String value,String desc){SystemConfig c=new SystemConfig();c.setConfigKey(key);c.setConfigValue(value);c.setDescription(desc);c.setEnabled(true);return c;}
}
