package com.grainshield.controller;

import com.grainshield.dto.ApiResponse;
import com.grainshield.dto.BusinessDtos.*;
import com.grainshield.entity.*;
import com.grainshield.repository.*;
import com.grainshield.service.AccessService;
import com.grainshield.service.CurrentUserService;
import com.grainshield.service.OperationLogService;
import com.grainshield.service.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateDataController {
    private final IotGrainDataRepository iotRepository;
    private final WeatherDataRepository weatherRepository;
    private final TrafficDataRepository trafficRepository;
    private final SupplyNodeRepository nodeRepository;
    private final LogisticsRouteRepository routeRepository;
    private final CurrentUserService currentUserService;
    private final AccessService accessService;
    private final ViewMapper mapper;
    private final OperationLogService logService;

    @GetMapping("/iot")
    public ApiResponse<List<IotView>> iotList(){return ApiResponse.ok(iotRepository.findTop100ByOrderByCollectedAtDesc().stream().map(mapper::iot).toList());}
    @PostMapping("/iot") @PreAuthorize("hasAnyRole('ADMIN','NODE_MANAGER')")
    public ApiResponse<IotView> addIot(@Valid @RequestBody IotRequest r){
        accessService.requireNodeAccess(currentUserService.requireCurrentUser(),r.nodeId());
        IotGrainData d=new IotGrainData();d.setNode(nodeRepository.findById(r.nodeId()).orElseThrow(()->new EntityNotFoundException("节点不存在")));d.setTemperature(r.temperature());d.setHumidity(r.humidity());d.setCollectedAt(r.collectedAt()==null?LocalDateTime.now():r.collectedAt());d=iotRepository.save(d);logService.log("模拟数据","录入粮情",d.getNode().getName()+" 温度="+d.getTemperature()+" 湿度="+d.getHumidity());return ApiResponse.ok(mapper.iot(d));
    }
    @DeleteMapping("/iot/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<Void> deleteIot(@PathVariable Long id){iotRepository.deleteById(id);return ApiResponse.ok("删除成功");}

    @GetMapping("/weather") public ApiResponse<List<WeatherData>> weatherList(){return ApiResponse.ok(weatherRepository.findTop100ByOrderByPublishedAtDesc());}
    @PostMapping("/weather") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WeatherData> addWeather(@Valid @RequestBody WeatherRequest r){WeatherData d=new WeatherData();d.setRegion(r.region());d.setDisasterType(r.disasterType());d.setWarningLevel(r.warningLevel());d.setRainfall(r.rainfall());d.setWindLevel(r.windLevel());d.setPublishedAt(r.publishedAt()==null?LocalDateTime.now():r.publishedAt());d=weatherRepository.save(d);logService.log("模拟数据","录入气象",d.getRegion()+" "+d.getDisasterType());return ApiResponse.ok(d);}
    @DeleteMapping("/weather/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<Void> deleteWeather(@PathVariable Long id){weatherRepository.deleteById(id);return ApiResponse.ok("删除成功");}

    @GetMapping("/traffic") public ApiResponse<List<TrafficView>> trafficList(){return ApiResponse.ok(trafficRepository.findTop100ByOrderByRecordedAtDesc().stream().map(mapper::traffic).toList());}
    @PostMapping("/traffic") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TrafficView> addTraffic(@Valid @RequestBody TrafficRequest r){LogisticsRoute route=routeRepository.findById(r.routeId()).orElseThrow(()->new EntityNotFoundException("线路不存在"));TrafficData d=new TrafficData();d.setRoute(route);d.setRoadName(r.roadName()==null||r.roadName().isBlank()?route.getName():r.roadName());d.setStatus(r.status());d.setCongestionLevel(r.congestionLevel());d.setRecordedAt(r.recordedAt()==null?LocalDateTime.now():r.recordedAt());d=trafficRepository.save(d);route.setStatus(r.status());routeRepository.save(route);logService.log("模拟数据","更新交通",route.getName()+" 状态="+r.status());return ApiResponse.ok(mapper.traffic(d));}
    @DeleteMapping("/traffic/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<Void> deleteTraffic(@PathVariable Long id){trafficRepository.deleteById(id);return ApiResponse.ok("删除成功");}
}
