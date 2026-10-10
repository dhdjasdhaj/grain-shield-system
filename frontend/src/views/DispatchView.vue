<template>
  <div class="page-shell">
    <div class="page-title-row">
      <div>
        <h1 class="page-title">应急调度建议</h1>
        <p class="page-subtitle">依据告警、可用库存和线路状态，采用“富余库存优先、就近补给”逻辑生成可解释的辅助决策方案</p>
      </div>
      <div class="toolbar">
        <el-button @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
        <el-button v-if="auth.isDispatcher" type="primary" class="gs-gradient-button" @click="openGenerate()">
          <el-icon><MagicStick /></el-icon>生成调度建议
        </el-button>
      </div>
    </div>

    <el-alert
      title="系统仅生成调度参考建议，不代表正式行政调拨命令；最终是否采用及如何执行由调度人员人工决定。"
      type="warning"
      :closable="false"
      show-icon
      class="notice"
    />

    <section class="dispatch-map-grid">
      <article class="glass-card dispatch-map-card">
        <div class="card-head">
          <div>
            <div class="card-title">粮食调度路线地图</div>
            <div class="small">点击下方方案后，地图会高亮起运节点、目标节点之间的运输线路</div>
          </div>
          <div class="map-legend">
            <span><i class="normal"></i>正常</span>
            <span><i class="warn"></i>拥堵</span>
            <span><i class="danger"></i>封闭</span>
          </div>
        </div>
        <div class="dispatch-map">
          <WuhanMapGraph :nodes="nodes" :routes="routes" :highlighted-route="highlightedRoute" />
        </div>
      </article>

      <aside class="glass-card selected-plan">
        <span>当前地图高亮</span>
        <template v-if="selectedMapPlan">
          <h2>{{ selectedMapPlan.code }}</h2>
          <p>{{ selectedMapPlan.sourceNodeName }} → {{ selectedMapPlan.targetNodeName }}</p>
          <b>{{ selectedMapPlan.grainType }} / {{ selectedMapPlan.suggestedQuantity }} 吨</b>
        </template>
        <template v-else>
          <h2>选择调度方案</h2>
          <p>点击表格中的方案，查看粮食调拨路线在武汉地图上的动态飞线位置。</p>
        </template>
      </aside>
    </section>

    <div class="glass-card plan-card">
      <el-table :data="plans" v-loading="loading" height="430" stripe @row-click="selectForMap">
        <el-table-column prop="code" label="方案编号" width="170" fixed />
        <el-table-column prop="alertCode" label="关联告警" width="165" />
        <el-table-column prop="sourceNodeName" label="起运节点" width="175" />
        <el-table-column prop="targetNodeName" label="目标节点" width="175" />
        <el-table-column prop="grainType" label="粮食品种" width="95" />
        <el-table-column prop="suggestedQuantity" label="建议数量(吨)" width="120" />
        <el-table-column prop="distanceKm" label="距离(km)" width="100" />
        <el-table-column prop="plateNumber" label="建议车辆" width="120">
          <template #default="s">{{ s.row.plateNumber || '待协调' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="s"><StatusTag :value="s.row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="s">
            <el-button link type="primary" @click.stop="show(s.row)">详情</el-button>
            <template v-if="auth.isDispatcher">
              <el-button v-if="['DRAFT','ADJUSTED'].includes(s.row.status)" link type="success" @click.stop="setStatus(s.row,'CONFIRMED')">确认</el-button>
              <el-button v-if="['DRAFT','ADJUSTED'].includes(s.row.status)" link type="warning" @click.stop="openAdjust(s.row)">调整</el-button>
              <el-button v-if="s.row.status !== 'REJECTED'" link type="danger" @click.stop="setStatus(s.row,'REJECTED')">驳回</el-button>
              <el-button link @click.stop="exportPlan(s.row)">导出</el-button>
            </template>
            <el-button v-if="s.row.status === 'CONFIRMED' || s.row.status === 'COMPLETED'" link type="primary" @click.stop="openFeedback(s.row)">反馈</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="generateDialog" title="生成应急调度建议" width="520px">
      <el-form label-position="top">
        <el-form-item label="关联风险告警" required>
          <el-select v-model="generateForm.alertId" filterable style="width:100%">
            <el-option v-for="a in eligibleAlerts" :key="a.id" :label="`${a.code} · ${a.nodeName || a.routeName || a.detail.slice(0,16)}`" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="粮食品种（留空自动判断）"><el-input v-model="generateForm.grainType" placeholder="如：稻谷" /></el-form-item>
        <el-form-item label="目标需求量（吨，留空自动测算缺口）"><el-input-number v-model="generateForm.demand" :min="0" :precision="2" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialog=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="generate">开始计算</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustDialog" title="人工调整调度建议" width="520px">
      <el-form label-position="top">
        <el-form-item label="建议数量(吨)"><el-input-number v-model="adjustForm.suggestedQuantity" :min="1" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="运输车辆">
          <el-select v-model="adjustForm.vehicleId" clearable style="width:100%">
            <el-option v-for="v in vehicles" :key="v.id" :label="`${v.plateNumber} · ${v.loadCapacity}吨 · ${v.status}`" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整说明"><el-input v-model="adjustForm.adjustmentNote" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog=false">取消</el-button>
        <el-button type="primary" @click="adjust">保存调整</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackDialog" title="执行反馈" width="520px">
      <el-form label-position="top">
        <el-form-item label="反馈环节">
          <el-select v-model="feedbackForm.feedbackType" style="width:100%">
            <el-option label="出库" value="出库" />
            <el-option label="运输" value="运输" />
            <el-option label="到达" value="到达" />
            <el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="feedbackForm.executionStatus" style="width:100%">
            <el-option label="待执行" value="待执行" />
            <el-option label="执行中" value="执行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="异常中断" value="异常中断" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈内容"><el-input v-model="feedbackForm.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialog=false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawer" title="调度建议详情" size="560px">
      <template v-if="current">
        <div class="plan-header"><span>辅助决策方案</span><h2>{{ current.code }}</h2><StatusTag :value="current.status" /></div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="关联告警">{{ current.alertCode }}</el-descriptions-item>
          <el-descriptions-item label="调拨方向">{{ current.sourceNodeName }} → {{ current.targetNodeName }}</el-descriptions-item>
          <el-descriptions-item label="粮食与数量">{{ current.grainType }} / {{ current.suggestedQuantity }} 吨</el-descriptions-item>
          <el-descriptions-item label="参考路径">{{ current.routePath }}</el-descriptions-item>
          <el-descriptions-item label="参考距离">{{ current.distanceKm }} 公里</el-descriptions-item>
          <el-descriptions-item label="建议车辆">{{ current.plateNumber || '待人工协调' }}</el-descriptions-item>
          <el-descriptions-item label="推荐理由">{{ current.recommendReason }}</el-descriptions-item>
          <el-descriptions-item label="调整说明">{{ current.adjustmentNote || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h3 class="feedback-title">执行反馈记录</h3>
        <el-timeline>
          <el-timeline-item v-for="f in feedbacks" :key="f.id" :timestamp="fmt(f.feedbackTime)" placement="top">
            <el-card shadow="never"><b>{{ f.feedbackType }} · {{ f.executionStatus }}</b><p>{{ f.content }}</p><small>{{ f.feedbackBy }}</small></el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!feedbacks.length" description="暂无执行反馈" :image-size="70" />
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/client'
import StatusTag from '../components/StatusTag.vue'
import WuhanMapGraph from '../components/WuhanMapGraph.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const plans = ref([])
const alerts = ref([])
const vehicles = ref([])
const nodes = ref([])
const routes = ref([])
const feedbacks = ref([])
const loading = ref(false)
const saving = ref(false)
const generateDialog = ref(false)
const adjustDialog = ref(false)
const feedbackDialog = ref(false)
const drawer = ref(false)
const current = ref(null)
const selected = ref(null)
const selectedMapPlan = ref(null)

const generateForm = reactive({ alertId: null, grainType: '', demand: null })
const adjustForm = reactive({ suggestedQuantity: null, vehicleId: null, adjustmentNote: '' })
const feedbackForm = reactive({ feedbackType: '运输', executionStatus: '执行中', content: '' })

const eligibleAlerts = computed(() => alerts.value.filter((a) => !['CLOSED', 'IGNORED'].includes(a.status)))
const highlightedRoute = computed(() => {
  const plan = selectedMapPlan.value
  if (!plan) return null
  return routes.value.find((item) =>
    plan.routePath?.includes(item.name) ||
    (item.startNodeName === plan.sourceNodeName && item.endNodeName === plan.targetNodeName) ||
    (item.startNodeName === plan.targetNodeName && item.endNodeName === plan.sourceNodeName)
  ) || null
})

const fmt = (value) => value?.replace('T', ' ').slice(0, 19) || '-'

async function load() {
  loading.value = true
  try {
    ;[plans.value, alerts.value, vehicles.value, nodes.value, routes.value] = await Promise.all([
      api.get('/dispatch'),
      api.get('/alerts'),
      api.get('/vehicles'),
      api.get('/nodes'),
      api.get('/routes')
    ])
    if (!selectedMapPlan.value && plans.value.length) selectedMapPlan.value = plans.value[0]
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function selectForMap(row) {
  selectedMapPlan.value = row
}

function openGenerate(id) {
  generateForm.alertId = Number(id || route.query.alertId) || null
  generateForm.grainType = ''
  generateForm.demand = null
  generateDialog.value = true
}

async function generate() {
  if (!generateForm.alertId) return ElMessage.warning('请选择关联告警')
  saving.value = true
  try {
    await api.post('/dispatch/generate', generateForm)
    ElMessage.success('调度建议生成成功')
    generateDialog.value = false
    router.replace('/dispatch')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

async function setStatus(row, status) {
  try {
    await ElMessageBox.confirm(`确认将方案状态更新为 ${status}？`, '操作确认')
    await api.put(`/dispatch/${row.id}/status`, null, { params: { status } })
    ElMessage.success('状态已更新')
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message)
  }
}

function openAdjust(row) {
  selected.value = row
  Object.assign(adjustForm, { suggestedQuantity: row.suggestedQuantity, vehicleId: row.vehicleId, adjustmentNote: row.adjustmentNote || '' })
  adjustDialog.value = true
}

async function adjust() {
  try {
    await api.put(`/dispatch/${selected.value.id}/adjust`, adjustForm)
    ElMessage.success('方案已调整')
    adjustDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function openFeedback(row) {
  selected.value = row
  Object.assign(feedbackForm, { feedbackType: '运输', executionStatus: '执行中', content: '' })
  feedbackDialog.value = true
}

async function submitFeedback() {
  if (!feedbackForm.content) return ElMessage.warning('请填写反馈内容')
  try {
    await api.post(`/dispatch/${selected.value.id}/feedback`, feedbackForm)
    ElMessage.success('反馈已提交')
    feedbackDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function show(row) {
  current.value = row
  selectedMapPlan.value = row
  drawer.value = true
  try {
    feedbacks.value = await api.get(`/dispatch/${row.id}/feedback`)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function exportPlan(row) {
  try {
    const response = await api.get(`/dispatch/${row.id}/export`, { responseType: 'blob' })
    const url = URL.createObjectURL(response.data)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `${row.code}-调拨参考单.csv`
    anchor.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(async () => {
  await load()
  if (route.query.alertId) openGenerate(route.query.alertId)
})

watch(() => route.query.alertId, (id) => {
  if (id) openGenerate(id)
})
</script>

<style scoped>
.notice { margin-bottom: 14px; }
.dispatch-map-grid { display: grid; grid-template-columns: minmax(0,1fr) 280px; gap: 16px; margin-bottom: 16px; }
.dispatch-map-card { overflow: hidden; }
.dispatch-map { height: 390px; padding: 0 16px 16px; }
.small { margin-top: 5px; color: #64748b; font-size: 12px; }
.map-legend { display: flex; gap: 12px; color: #64748b; font-size: 12px; flex-wrap: wrap; }
.map-legend i { display: inline-block; width: 9px; height: 9px; margin-right: 5px; border-radius: 50%; }
.map-legend .normal { background: #22c55e; }
.map-legend .warn { background: #eab308; }
.map-legend .danger { background: #ef4444; }
.selected-plan { position:relative; padding: 22px; align-self: stretch; overflow:hidden; background:linear-gradient(160deg,#ffffff,#eef8e9); }
.selected-plan::after { content:""; position:absolute; right:-45px; bottom:-45px; width:150px; height:150px; border-radius:50%; background:rgba(34,197,94,.12); }
.selected-plan span { color: #166534; font-size: 12px; font-weight: 800; }
.selected-plan h2 { margin: 12px 0 10px; color: #1f2937; font-size: 24px; }
.selected-plan p { margin: 0 0 14px; color: #64748b; line-height: 1.7; }
.selected-plan b { color: #166534; }
.plan-card { overflow: hidden; }
.plan-header { padding: 22px; border-radius: 14px; background: linear-gradient(135deg,#166534,#22c55e); color: white; margin-bottom: 20px; }
.plan-header span { font-size: 11px; color: #dcfce7; letter-spacing: .12em; }
.plan-header h2 { margin: 8px 0 12px; }
.feedback-title { margin: 28px 0 18px; color: #1f2937; }
.el-card p { color: #62756e; line-height: 1.6; margin: 8px 0; }
.el-card small { color: #93a29c; }
@media(max-width: 920px) { .dispatch-map-grid { grid-template-columns: 1fr; } .dispatch-map { height: 340px; } }
</style>
