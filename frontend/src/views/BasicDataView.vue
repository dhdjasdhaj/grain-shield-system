<template>
  <div class="page-shell">
    <div class="page-title-row"><div><h1 class="page-title">基础数据与资源管理</h1><p class="page-subtitle">统一维护供应链节点、粮食库存、运输线路与车辆资源，为风险判断和调度建议提供可信数据底座</p></div></div>
    <section class="glass-card map-card">
      <div class="card-head">
        <div><div class="card-title">仓库与道路运输地图</div><div class="small">粮库、加工中心、配送中心按真实经纬度展示，运输线路以动态飞线呈现</div></div>
      </div>
      <div class="warehouse-map"><WuhanMapGraph :nodes="nodes" :routes="routes"/></div>
    </section>
    <el-tabs v-model="tab" class="data-tabs">
      <el-tab-pane label="节点档案" name="nodes"><CrudManager title="供应链节点" endpoint="/nodes" :columns="nodeColumns" :fields="nodeFields" :can-create="auth.isAdmin" :can-edit="auth.isAdmin" :can-delete="auth.isAdmin" :defaults="{type:'WAREHOUSE',status:'NORMAL'}" @changed="loadOptions"/></el-tab-pane>
      <el-tab-pane label="库存台账" name="inventory"><CrudManager title="粮食品种与库存" endpoint="/inventories" :columns="inventoryColumns" :fields="inventoryFields" :can-create="auth.user?.role!=='DISPATCHER'" :can-edit="auth.user?.role!=='DISPATCHER'" :can-delete="auth.isAdmin" :defaults="{unit:'吨',currentStock:0,availableStock:0,safetyStock:0,dailyConsumption:0}"/></el-tab-pane>
      <el-tab-pane label="运输线路" name="routes"><CrudManager title="运输线路" endpoint="/routes" :columns="routeColumns" :fields="routeFields" :can-create="auth.isAdmin" :can-edit="auth.isAdmin" :can-delete="auth.isAdmin" :defaults="{status:'NORMAL',riskLevel:'LOW'}" @changed="loadOptions"/></el-tab-pane>
      <el-tab-pane label="车辆资源" name="vehicles"><CrudManager title="运输车辆" endpoint="/vehicles" :columns="vehicleColumns" :fields="vehicleFields" :can-create="auth.isAdmin" :can-edit="auth.isAdmin" :can-delete="auth.isAdmin" :defaults="{status:'AVAILABLE'}"/></el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup>
import { onMounted,ref } from 'vue';import api from '../api/client';import CrudManager from '../components/CrudManager.vue';import WuhanMapGraph from '../components/WuhanMapGraph.vue';import { useAuthStore } from '../stores/auth'
const auth=useAuthStore(),tab=ref('nodes'),nodes=ref([]),routes=ref([]);const options=a=>a.map(x=>({label:x.name,value:x.id}));const loadOptions=async()=>{[nodes.value,routes.value]=await Promise.all([api.get('/nodes'),api.get('/routes')])};onMounted(loadOptions)
const enumOpt=(obj)=>Object.entries(obj).map(([value,label])=>({value,label}))
const nodeTypes=enumOpt({WAREHOUSE:'粮库',PROCESSING:'加工企业',DISTRIBUTION:'配送点',LOGISTICS:'物流节点'}),nodeStatuses=enumOpt({NORMAL:'正常',WARNING:'预警',OFFLINE:'离线'}),routeStatuses=enumOpt({NORMAL:'正常',CONGESTED:'拥堵',CLOSED:'封闭'}),riskLevels=enumOpt({LOW:'低',MEDIUM:'中',HIGH:'高',CRITICAL:'紧急'}),vehicleStatuses=enumOpt({AVAILABLE:'可用',IN_TRANSIT:'运输中',MAINTENANCE:'维护',OFFLINE:'离线'})
const nodeColumns=[{prop:'code',label:'节点编号',width:105},{prop:'name',label:'节点名称',width:180},{prop:'type',label:'类型',formatter:v=>({WAREHOUSE:'粮库',PROCESSING:'加工企业',DISTRIBUTION:'配送点',LOGISTICS:'物流节点'})[v]||v},{prop:'region',label:'所属区域'},{prop:'capacity',label:'总库容(吨)'},{prop:'managerName',label:'负责人'},{prop:'phone',label:'联系电话'},{prop:'status',label:'状态',tag:true}]
const nodeFields=[{prop:'code',label:'节点编号',required:true},{prop:'name',label:'节点名称',required:true},{prop:'type',label:'节点类型',type:'select',options:nodeTypes,required:true},{prop:'region',label:'所属区域',required:true},{prop:'address',label:'位置描述',wide:true},{prop:'capacity',label:'总库容(吨)',type:'number'},{prop:'longitude',label:'经度',type:'number',precision:6},{prop:'latitude',label:'纬度',type:'number',precision:6},{prop:'managerName',label:'负责人'},{prop:'phone',label:'联系电话'},{prop:'status',label:'节点状态',type:'select',options:nodeStatuses}]
const inventoryColumns=[{prop:'nodeName',label:'节点',width:180},{prop:'grainType',label:'粮食品种'},{prop:'currentStock',label:'当前库存'},{prop:'availableStock',label:'可用库存'},{prop:'safetyStock',label:'安全库存'},{prop:'dailyConsumption',label:'日均消耗'},{prop:'bufferDays',label:'缓冲天数',formatter:v=>`${v??0} 天`},{prop:'updatedAt',label:'更新时间',width:165,formatter:v=>v?.replace('T',' ')}]
const inventoryFields=[{prop:'nodeId',label:'所属节点',type:'select',options:()=>options(nodes.value),required:true},{prop:'grainType',label:'粮食品种',required:true},{prop:'unit',label:'计量单位'},{prop:'currentStock',label:'当前库存',type:'number'},{prop:'availableStock',label:'可用库存',type:'number'},{prop:'safetyStock',label:'安全库存',type:'number'},{prop:'dailyConsumption',label:'日均消耗',type:'number'}]
const routeColumns=[{prop:'code',label:'线路编号'},{prop:'name',label:'线路名称',width:180},{prop:'startNodeName',label:'起点',width:160},{prop:'endNodeName',label:'终点',width:160},{prop:'distanceKm',label:'距离(km)'},{prop:'status',label:'通行状态',tag:true},{prop:'riskLevel',label:'风险等级',tag:true},{prop:'remark',label:'备注',width:180}]
const routeFields=[{prop:'code',label:'线路编号',required:true},{prop:'name',label:'线路名称',required:true},{prop:'startNodeId',label:'起点',type:'select',options:()=>options(nodes.value),required:true},{prop:'endNodeId',label:'终点',type:'select',options:()=>options(nodes.value),required:true},{prop:'distanceKm',label:'距离(km)',type:'number',required:true},{prop:'status',label:'通行状态',type:'select',options:routeStatuses},{prop:'riskLevel',label:'风险等级',type:'select',options:riskLevels},{prop:'remark',label:'备注',type:'textarea',wide:true}]
const vehicleColumns=[{prop:'plateNumber',label:'车牌号'},{prop:'loadCapacity',label:'载重(吨)'},{prop:'currentNodeName',label:'当前节点',width:170},{prop:'currentLocation',label:'当前位置',width:180},{prop:'status',label:'车辆状态',tag:true}]
const vehicleFields=[{prop:'plateNumber',label:'车牌号',required:true},{prop:'loadCapacity',label:'载重(吨)',type:'number',required:true},{prop:'currentNodeId',label:'当前节点',type:'select',options:()=>options(nodes.value)},{prop:'currentLocation',label:'当前位置'},{prop:'status',label:'车辆状态',type:'select',options:vehicleStatuses}]
</script>
<style scoped>.map-card{margin-bottom:16px;overflow:hidden;background:linear-gradient(180deg,#fff,#f8fbf5)}.warehouse-map{height:430px;padding:0 16px 16px}.small{margin-top:5px;color:#64748b;font-size:12px}.data-tabs :deep(.el-tabs__content){overflow:visible}.data-tabs :deep(.el-tabs__header){margin-bottom:14px}@media(max-width:760px){.warehouse-map{height:340px}}</style>
