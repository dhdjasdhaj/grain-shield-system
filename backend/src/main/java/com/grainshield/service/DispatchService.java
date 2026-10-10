package com.grainshield.service;

import com.grainshield.dto.BusinessDtos.*;
import com.grainshield.entity.*;
import com.grainshield.enums.*;
import com.grainshield.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DispatchService {
    private final RiskAlertRepository alertRepository;
    private final DispatchPlanRepository planRepository;
    private final InventoryRepository inventoryRepository;
    private final LogisticsRouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final ExecutionFeedbackRepository feedbackRepository;
    private final CurrentUserService currentUserService;
    private final OperationLogService logService;
    private final ViewMapper mapper;

    @Transactional
    public PlanView generate(GeneratePlanRequest req) {
        RiskAlert alert=alertRepository.findById(req.alertId()).orElseThrow(()->new EntityNotFoundException("告警不存在"));
        SupplyNode target=alert.getNode()!=null?alert.getNode():(alert.getRoute()!=null?alert.getRoute().getEndNode():null);
        if(target==null)throw new IllegalArgumentException("该告警未关联目标节点，无法生成调度建议");
        List<Inventory> targetInventories=inventoryRepository.findByNodeId(target.getId());
        String grain=(req.grainType()==null||req.grainType().isBlank())?(targetInventories.isEmpty()?"稻谷":targetInventories.getFirst().getGrainType()):req.grainType();
        Inventory targetInventory=inventoryRepository.findByNodeIdAndGrainTypeIgnoreCase(target.getId(),grain).orElse(null);
        BigDecimal shortage=req.demand();
        if(shortage==null||shortage.compareTo(BigDecimal.ZERO)<=0){
            shortage=targetInventory==null?BigDecimal.valueOf(100):targetInventory.getSafetyStock().subtract(targetInventory.getAvailableStock()).max(BigDecimal.ZERO);
            if(shortage.compareTo(BigDecimal.ZERO)==0)shortage=BigDecimal.valueOf(100);
        }
        List<LogisticsRoute> routes=routeRepository.findAll();
        Candidate best=null;
        for(Inventory inv:inventoryRepository.findByGrainTypeIgnoreCase(grain)){
            if(inv.getNode().getId().equals(target.getId()))continue;
            BigDecimal surplus=inv.getAvailableStock().subtract(inv.getSafetyStock());
            if(surplus.compareTo(BigDecimal.ZERO)<=0)continue;
            PathResult path=shortestPath(inv.getNode(),target,routes);
            if(path==null)continue;
            Candidate c=new Candidate(inv,surplus,path);
            if(best==null||c.path.weight<best.path.weight)best=c;
        }
        if(best==null)throw new IllegalStateException("当前无库存充足且路线可通行的备选粮库");
        Candidate selected=best;
        Vehicle vehicle=vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).stream()
                .sorted(Comparator.comparing((Vehicle v)->v.getCurrentNode()!=null&&v.getCurrentNode().getId().equals(selected.inventory.getNode().getId())?0:1)
                        .thenComparing(Vehicle::getLoadCapacity,Comparator.reverseOrder())).findFirst().orElse(null);
        BigDecimal quantity=shortage.min(best.surplus).setScale(2,RoundingMode.HALF_UP);
        DispatchPlan plan=new DispatchPlan();plan.setCode("DP"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+UUID.randomUUID().toString().substring(0,4).toUpperCase());plan.setAlert(alert);plan.setSourceNode(best.inventory.getNode());plan.setTargetNode(target);plan.setGrainType(grain);plan.setSuggestedQuantity(quantity);plan.setRoutePath(best.path.pathText);plan.setDistanceKm(BigDecimal.valueOf(best.path.distance).setScale(2,RoundingMode.HALF_UP));plan.setVehicle(vehicle);
        String vehicleText=vehicle==null?"当前无空闲车辆，需人工协调运力":"优先使用车辆"+vehicle.getPlateNumber()+"（载重"+vehicle.getLoadCapacity()+"吨）";
        plan.setRecommendReason("起运节点可用库存"+best.inventory.getAvailableStock()+"吨，扣除安全库存后富余"+best.surplus+"吨；目标节点存在约"+shortage+"吨缺口；推荐路径避开封闭线路，综合距离"+plan.getDistanceKm()+"公里；"+vehicleText+"。本方案仅供人工决策参考。");
        plan.setStatus(PlanStatus.DRAFT);plan.setCreatedBy(currentUserService.username());plan=planRepository.save(plan);
        alert.setStatus(AlertStatus.ANALYZING);alert.setHandledBy(currentUserService.username());alert.setHandledAt(LocalDateTime.now());alertRepository.save(alert);
        logService.log("应急调度","生成调度建议",plan.getCode()+"，"+plan.getSourceNode().getName()+"→"+plan.getTargetNode().getName());return mapper.plan(plan);
    }

    public List<PlanView> list(){return planRepository.findAllByOrderByCreatedAtDesc().stream().map(mapper::plan).toList();}

    @Transactional
    public PlanView changeStatus(Long id,PlanStatus status){DispatchPlan p=planRepository.findById(id).orElseThrow(()->new EntityNotFoundException("方案不存在"));p.setStatus(status);p=planRepository.save(p);logService.log("应急调度","更新方案状态",p.getCode()+" -> "+status);return mapper.plan(p);}

    @Transactional
    public PlanView adjust(Long id,AdjustPlanRequest req){DispatchPlan p=planRepository.findById(id).orElseThrow(()->new EntityNotFoundException("方案不存在"));if(req.suggestedQuantity()!=null&&req.suggestedQuantity().compareTo(BigDecimal.ZERO)>0)p.setSuggestedQuantity(req.suggestedQuantity());if(req.vehicleId()!=null)p.setVehicle(vehicleRepository.findById(req.vehicleId()).orElseThrow(()->new EntityNotFoundException("车辆不存在")));p.setAdjustmentNote(req.adjustmentNote());p.setStatus(PlanStatus.ADJUSTED);p=planRepository.save(p);logService.log("应急调度","人工调整方案",p.getCode());return mapper.plan(p);}

    @Transactional
    public FeedbackView feedback(Long planId,FeedbackRequest req){DispatchPlan p=planRepository.findById(planId).orElseThrow(()->new EntityNotFoundException("方案不存在"));ExecutionFeedback f=new ExecutionFeedback();f.setPlan(p);f.setFeedbackType(req.feedbackType());f.setContent(req.content());f.setExecutionStatus(req.executionStatus());f.setFeedbackBy(currentUserService.username());f.setFeedbackTime(LocalDateTime.now());f=feedbackRepository.save(f);if("已完成".equals(req.executionStatus())||"COMPLETED".equalsIgnoreCase(req.executionStatus())){p.setStatus(PlanStatus.COMPLETED);planRepository.save(p);}logService.log("执行反馈","提交反馈",p.getCode()+" / "+req.executionStatus());return mapper.feedback(f);}

    public List<FeedbackView> feedbackList(Long planId){return feedbackRepository.findByPlanIdOrderByFeedbackTimeDesc(planId).stream().map(mapper::feedback).toList();}

    public String exportCsv(Long id){DispatchPlan p=planRepository.findById(id).orElseThrow(()->new EntityNotFoundException("方案不存在"));return "调拨参考单编号,关联告警,起运节点,目标节点,粮食品种,建议数量(吨),参考路径,参考距离(公里),车辆,方案状态,推荐理由\n"+csv(p.getCode())+","+csv(p.getAlert().getCode())+","+csv(p.getSourceNode().getName())+","+csv(p.getTargetNode().getName())+","+csv(p.getGrainType())+","+p.getSuggestedQuantity()+","+csv(p.getRoutePath())+","+p.getDistanceKm()+","+csv(p.getVehicle()==null?"待协调":p.getVehicle().getPlateNumber())+","+p.getStatus()+","+csv(p.getRecommendReason())+"\n";}
    private String csv(String s){return "\""+(s==null?"":s.replace("\"","\"\""))+"\"";}

    private PathResult shortestPath(SupplyNode source,SupplyNode target,List<LogisticsRoute> routes){
        Map<Long,List<Edge>> graph=new HashMap<>();for(LogisticsRoute r:routes){if(r.getStatus()==RouteStatus.CLOSED)continue;double factor=r.getStatus()==RouteStatus.CONGESTED?3.0:1.0;double d=r.getDistanceKm().doubleValue();graph.computeIfAbsent(r.getStartNode().getId(),k->new ArrayList<>()).add(new Edge(r.getEndNode(),r,d,d*factor));graph.computeIfAbsent(r.getEndNode().getId(),k->new ArrayList<>()).add(new Edge(r.getStartNode(),r,d,d*factor));}
        Map<Long,Double> dist=new HashMap<>();Map<Long,Edge> prev=new HashMap<>();PriorityQueue<State> pq=new PriorityQueue<>(Comparator.comparingDouble(State::distance));dist.put(source.getId(),0d);pq.add(new State(source.getId(),0));
        while(!pq.isEmpty()){State cur=pq.poll();if(cur.distance>dist.getOrDefault(cur.nodeId,Double.MAX_VALUE))continue;if(cur.nodeId.equals(target.getId()))break;for(Edge e:graph.getOrDefault(cur.nodeId,List.of())){double nd=cur.distance+e.weight;if(nd<dist.getOrDefault(e.to.getId(),Double.MAX_VALUE)){dist.put(e.to.getId(),nd);prev.put(e.to.getId(),e.withFrom(cur.nodeId));pq.add(new State(e.to.getId(),nd));}}}
        if(!dist.containsKey(target.getId()))return null;List<String> names=new ArrayList<>();double physical=0;Long cursor=target.getId();names.add(target.getName());while(!cursor.equals(source.getId())){Edge e=prev.get(cursor);if(e==null)return null;physical+=e.distance;names.add(e.route.getName());SupplyNode from=e.route.getStartNode().getId().equals(e.fromId)?e.route.getStartNode():e.route.getEndNode();names.add(from.getName());cursor=e.fromId;}Collections.reverse(names);return new PathResult(String.join(" → ",names),physical,dist.get(target.getId()));
    }
    private record Candidate(Inventory inventory,BigDecimal surplus,PathResult path){}
    private record PathResult(String pathText,double distance,double weight){}
    private record State(Long nodeId,double distance){}
    private record Edge(SupplyNode to,LogisticsRoute route,double distance,double weight,Long fromId){Edge(SupplyNode to,LogisticsRoute route,double distance,double weight){this(to,route,distance,weight,null);}Edge withFrom(Long id){return new Edge(to,route,distance,weight,id);}}
}
