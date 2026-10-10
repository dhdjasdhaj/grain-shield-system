<template>
  <div class="page-shell dashboard-page">
    <section class="command-intro">
      <div>
        <div class="eyebrow">
          <span class="live-dot"></span>
          Grain Supply Chain Control Tower
          <b>{{ lastUpdated || '--:--:--' }}</b>
        </div>
        <h1>粮食供应链综合态势</h1>
        <p>
          从全国粮食生产格局、粮食实时流向、仓容压力、市场价格和风险事件五个层面，形成面向调度人员的综合研判视图。
        </p>
      </div>
      <div class="intro-actions">
        <el-button type="primary" :loading="scanning" @click="scan">
          <el-icon><Aim /></el-icon>
          扫描风险
        </el-button>
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新数据</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article class="glass-card realtime-card">
        <div class="section-head">
          <div>
            <span class="source-pill system">Grain Shield · 系统实时数据</span>
            <h2>系统实时指标</h2>
          </div>
          <span class="mini-time">{{ lastUpdated || '等待刷新' }}</span>
        </div>
        <div class="realtime-kpis">
          <div v-for="(metric, index) in systemKpis" :key="metric.key" class="metric-card" :class="metric.tone" :style="{ '--delay': `${index * 60}ms` }">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}<em>{{ metric.unit }}</em></strong>
            <small>{{ metric.note }}</small>
          </div>
        </div>
      </article>

      <article class="glass-card macro-card">
        <div class="section-head">
          <div>
            <span class="source-pill official">官方公开数据</span>
            <h2>全国宏观粮情</h2>
          </div>
          <span class="mini-time">国家统计局 · 2026-07-10</span>
        </div>
        <div class="macro-kpis">
          <div v-for="item in macroCards" :key="item.label" class="macro-item" :class="item.tone">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}<em>{{ item.unit }}</em></strong>
            <small>{{ item.change }}</small>
          </div>
        </div>
      </article>
    </section>

    <section class="map-grid">
      <article class="glass-card map-card">
        <div class="section-head map-head">
          <div>
            <span class="source-pill" :class="mapMode === 'macro' ? 'official' : 'demo'">
              {{ mapMode === 'macro' ? '国家统计局 · 2026-07-10' : 'Demo Data / 模拟调度数据' }}
            </span>
            <h2>全国粮食地图</h2>
          </div>
          <div class="segmented">
            <button :class="{ active: mapMode === 'macro' }" @click="mapMode = 'macro'">宏观粮情</button>
            <button :class="{ active: mapMode === 'dispatch' }" @click="mapMode = 'dispatch'">实时调度</button>
          </div>
        </div>
        <div class="map-stage">
          <ChinaControlMap
            :mode="mapMode"
            :provinces="summerProvinces"
            :selected-province="selectedProvince"
            :nodes="nationalDispatchNodes"
            :routes="nationalDispatchRoutes"
            :selected-route-id="selectedRoute?.id"
            @select-province="selectProvince"
            @select-route="selectRoute"
          />
        </div>
      </article>

      <aside class="glass-card map-insight">
        <template v-if="mapMode === 'macro'">
          <span class="source-pill official">官方公开数据</span>
          <h2>{{ selectedProvinceData.displayName }}夏粮生产</h2>
          <div class="rank-badge">全国第 {{ selectedProvinceData.rank }} 位</div>
          <div class="province-stats">
            <div>
              <span>夏粮产量</span>
              <strong>{{ selectedProvinceData.output }}<em>万吨</em></strong>
            </div>
            <div>
              <span>播种面积</span>
              <strong>{{ selectedProvinceData.area }}<em>万亩</em></strong>
            </div>
            <div>
              <span>单位面积产量</span>
              <strong>{{ selectedProvinceData.yield }}<em>kg/亩</em></strong>
            </div>
          </div>
          <p class="panel-note">点击地图省份可固定选中，并联动此侧栏。数据日期：2026-07-10。</p>
        </template>

        <template v-else>
          <span class="source-pill demo">Demo Data / 模拟调度数据</span>
          <h2>{{ selectedRoute.name }}</h2>
          <div class="route-status" :class="selectedRoute.status">{{ routeStatusText(selectedRoute.status) }}</div>
          <dl class="route-detail">
            <div><dt>调度任务</dt><dd>{{ selectedRoute.task }}</dd></div>
            <div><dt>粮食品种</dt><dd>{{ selectedRoute.grain }}</dd></div>
            <div><dt>运输数量</dt><dd>{{ selectedRoute.quantity }} 吨</dd></div>
            <div><dt>起点</dt><dd>{{ selectedRoute.start }}</dd></div>
            <div><dt>终点</dt><dd>{{ selectedRoute.end }}</dd></div>
            <div><dt>车辆</dt><dd>{{ selectedRoute.vehicle }}</dd></div>
            <div><dt>预计到达</dt><dd>{{ selectedRoute.eta }}</dd></div>
            <div><dt>风险等级</dt><dd>{{ selectedRoute.risk }}</dd></div>
          </dl>
          <div class="progress-line">
            <span>运输进度</span>
            <b>{{ selectedRoute.progress }}%</b>
            <i><em :style="{ width: `${selectedRoute.progress}%` }"></em></i>
          </div>
        </template>
      </aside>
    </section>

    <section class="analytics-layout">
      <article class="glass-card chart-panel structure-panel">
        <div class="section-head">
          <div>
            <span class="source-pill official">国家统计局 · 2025年粮食产量</span>
            <h2>全国粮食生产结构</h2>
          </div>
          <strong class="big-number">71487.6 万吨</strong>
        </div>
        <div ref="structureEl" class="chart-box"></div>
      </article>

      <article class="glass-card chart-panel sankey-panel">
        <div class="section-head">
          <div>
            <span class="source-pill demo">Demo Data / 模拟流向结构</span>
            <h2>粮食调拨流向</h2>
          </div>
          <span class="mini-time">生产 → 收购 → 仓储 → 调拨 → 终端</span>
        </div>
        <div ref="sankeyEl" class="chart-box"></div>
      </article>
    </section>

    <section class="monitor-grid">
      <article class="glass-card purchase-card">
        <div class="section-head">
          <div>
            <span class="source-pill official">国家粮食和物资储备局 · 2026-06-30</span>
            <h2>夏粮收购监测</h2>
          </div>
        </div>
        <div class="purchase-list">
          <div v-for="item in purchaseProgress" :key="item.name" class="purchase-item">
            <div class="purchase-copy">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}<em>万吨</em></strong>
              <small>{{ item.note }}</small>
            </div>
            <div class="liquid-meter" :style="{ '--level': `${item.percent}%`, '--tone': item.color }">
              <span>{{ item.percent }}%</span>
            </div>
          </div>
        </div>
      </article>

      <article class="glass-card market-card">
        <div class="section-head">
          <div>
            <span class="source-pill demo">国家粮食和物资储备局 · Demo Data / 模拟行情数据</span>
            <h2>全国主要粮食品种行情</h2>
          </div>
          <div class="mini-tabs">
            <button v-for="period in pricePeriods" :key="period.key" :class="{ active: pricePeriod === period.key }" @click="pricePeriod = period.key">
              {{ period.label }}
            </button>
          </div>
        </div>
        <div class="price-tabs">
          <button v-for="item in priceTabs" :key="item.key" :class="{ active: priceTab === item.key }" @click="priceTab = item.key">
            {{ item.label }}
          </button>
        </div>
        <div class="price-body">
          <div class="latest-price">
            <span>当前价格</span>
            <strong>{{ currentPrice.current }}<em>元/公斤</em></strong>
            <small :class="currentPrice.change >= 0 ? 'up' : 'down'">
              {{ currentPrice.change >= 0 ? '↑' : '↓' }} 周变化 {{ Math.abs(currentPrice.change).toFixed(2) }}
            </small>
          </div>
          <div ref="priceEl" class="price-chart"></div>
        </div>
      </article>

      <article class="glass-card index-card">
        <div class="section-head">
          <div>
            <span class="source-pill official">粮食价格指数 · 2026-07-27</span>
            <h2>粮食市场景气度</h2>
          </div>
        </div>
        <div class="index-grid">
          <div ref="indexGaugeEl" class="index-gauge"></div>
          <div class="index-copy">
            <span>粮食价格指数</span>
            <strong>109.39</strong>
            <small class="down">趋势 -0.39</small>
            <div ref="indexSparkEl" class="sparkline"></div>
          </div>
        </div>
      </article>
    </section>

    <section class="risk-layout">
      <article class="glass-card risk-card">
        <div class="section-head">
          <div>
            <span class="source-pill system">系统测算 · Grain Shield</span>
            <h2>粮食供应链风险指数</h2>
          </div>
          <strong class="risk-score" :class="riskTone">{{ riskScore }}</strong>
        </div>
        <div class="risk-content">
          <div ref="riskRadarEl" class="radar-box"></div>
          <div class="risk-timeline">
            <div v-for="event in riskEvents" :key="event.id" class="risk-event" :class="riskClass(event.level)">
              <span>{{ event.time }}</span>
              <b>{{ event.title }}</b>
              <em>{{ event.detail }}</em>
              <i>{{ event.level }}</i>
            </div>
          </div>
        </div>
      </article>

      <article class="glass-card insight-card">
        <div class="section-head">
          <div>
            <span class="source-pill official">农业农村部 CASDE · 2026-08</span>
            <h2>本期粮食供需研判</h2>
          </div>
        </div>
        <div class="insight-list">
          <div v-for="item in insightCards" :key="item.crop" class="insight-item">
            <span>{{ item.crop }}</span>
            <strong>{{ item.status }}</strong>
            <div class="tag-row">
              <i v-for="tag in item.tags" :key="tag">{{ tag }}</i>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="operations-layout">
      <article class="glass-card calendar-card">
        <div class="section-head">
          <div>
            <span class="source-pill demo">按当前月份动态生成 · 2026年8月下旬</span>
            <h2>当前农时</h2>
          </div>
        </div>
        <div class="calendar-regions">
          <button v-for="item in calendarRegions" :key="item.region" class="region-card" @click="focusRegion(item)">
            <span>{{ item.region }}</span>
            <b>{{ item.main }}</b>
            <em>{{ item.extra }}</em>
          </button>
        </div>
      </article>

      <article class="glass-card warehouse-card">
        <div class="section-head">
          <div>
            <span class="source-pill system">Grain Shield · 系统实时数据</span>
            <h2>粮库仓容</h2>
          </div>
          <span class="mini-time">容量矩阵 + Bullet Chart</span>
        </div>
        <div class="warehouse-grid">
          <div v-for="item in warehouseRows" :key="item.name" class="warehouse-item" :class="item.level">
            <div class="warehouse-top">
              <b>{{ item.name }}</b>
              <span>{{ item.status }}</span>
            </div>
            <div class="capacity-matrix">
              <i v-for="cell in 20" :key="cell" :class="{ filled: cell <= item.filledCells }"></i>
            </div>
            <div class="bullet">
              <i><em :style="{ width: `${item.rate}%` }"></em></i>
              <b>{{ item.rate }}%</b>
            </div>
            <dl>
              <div><dt>库存</dt><dd>{{ formatNumber(item.stock) }} / {{ formatNumber(item.capacity) }} 吨</dd></div>
              <div><dt>主要粮种</dt><dd>{{ item.grain }}</dd></div>
              <div><dt>温湿度</dt><dd>{{ item.temperature }}℃ · {{ item.humidity }}%</dd></div>
            </dl>
          </div>
        </div>
      </article>
    </section>

    <section class="dispatch-layout">
      <article class="glass-card dispatch-table-card">
        <div class="section-head">
          <div>
            <span class="source-pill demo">Demo Data / 模拟调度数据</span>
            <h2>调度任务联动</h2>
          </div>
          <span class="mini-time">点击任务可联动全国调度地图</span>
        </div>
        <div class="dispatch-table">
          <button
            v-for="route in nationalDispatchRoutes"
            :key="route.id"
            :class="{ active: selectedRoute?.id === route.id }"
            @click="selectRoute(route)"
          >
            <span>{{ route.task }}</span>
            <b>{{ route.start }} → {{ route.end }}</b>
            <em>{{ route.grain }} · {{ route.quantity }} 吨 · {{ route.vehicle }}</em>
            <i :class="route.status">{{ routeStatusText(route.status) }}</i>
          </button>
        </div>
      </article>

      <article class="glass-card timeline-card">
        <div class="section-head">
          <div>
            <span class="source-pill demo">{{ selectedRoute.name }}</span>
            <h2>任务执行 Timeline</h2>
          </div>
        </div>
        <div class="task-timeline">
          <div v-for="step in taskTimeline" :key="step.name" class="task-step" :class="{ done: step.done, current: step.current }">
            <i></i>
            <span>{{ step.name }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Aim, Box, Connection, Guide, Refresh, TrendCharts, Van, Warning } from '@element-plus/icons-vue'
import api from '../api/client'
import ChinaControlMap from '../components/ChinaControlMap.vue'

const data = reactive({
  kpis: {},
  riskDistribution: [],
  trend: [],
  nodes: [],
  routes: [],
  latestAlerts: []
})

const animatedKpis = reactive({})
const inventories = ref([])
const plans = ref([])
const vehicles = ref([])
const iotRows = ref([])
const scanning = ref(false)
const loading = ref(false)
const lastUpdated = ref('')
const mapMode = ref('macro')
const selectedProvince = ref('河南')
const priceTab = ref('wheat')
const pricePeriod = ref('half')

const structureEl = ref()
const sankeyEl = ref()
const priceEl = ref()
const indexGaugeEl = ref()
const indexSparkEl = ref()
const riskRadarEl = ref()
let structureChart
let sankeyChart
let priceChart
let indexGaugeChart
let indexSparkChart
let riskRadarChart
let refreshTimer
let animationFrame

const labels = {
  INVENTORY: '库存风险',
  TEMPERATURE: '高温风险',
  HUMIDITY: '高湿风险',
  TRAFFIC: '运输阻断',
  WEATHER: '气象异常',
  REGIONAL_SUPPLY: '区域供应'
}

const macroCards = [
  { label: '2026全国夏粮产量', value: '15074.6', unit: '万吨', change: '同比 +0.7%', tone: 'green' },
  { label: '2026小麦产量', value: '13895.2', unit: '万吨', change: '同比 +0.6%', tone: 'forest' },
  { label: '夏粮播种面积', value: '39798.6', unit: '万亩', change: '同比 -0.2%', tone: 'gold' },
  { label: '夏粮平均单产', value: '378.8', unit: 'kg/亩', change: '同比 +0.8%', tone: 'blue' }
]

const summerProvinces = [
  { name: '河南', mapName: '河南省', displayName: '河南', output: 3776.4, rank: 1, area: 8526.3, yield: 442.9 },
  { name: '山东', mapName: '山东省', displayName: '山东', output: 2751.0, rank: 2, area: 6052.8, yield: 454.5 },
  { name: '安徽', mapName: '安徽省', displayName: '安徽', output: 1777.2, rank: 3, area: 4353.8, yield: 408.2 },
  { name: '河北', mapName: '河北省', displayName: '河北', output: 1544.2, rank: 4, area: 3440.9, yield: 448.8 },
  { name: '江苏', mapName: '江苏省', displayName: '江苏', output: 1445.9, rank: 5, area: 3753.6, yield: 385.2 },
  { name: '新疆', mapName: '新疆维吾尔自治区', displayName: '新疆', output: 641.7, rank: 6, area: 1544.6, yield: 415.4 },
  { name: '湖北', mapName: '湖北省', displayName: '湖北', output: 493.2, rank: 7, area: 1980.0, yield: 249.1 },
  { name: '四川', mapName: '四川省', displayName: '四川', output: 483.9, rank: 8, area: 1716.9, yield: 281.8 },
  { name: '陕西', mapName: '陕西省', displayName: '陕西', output: 472.9, rank: 9, area: 1594.1, yield: 296.6 },
  { name: '甘肃', mapName: '甘肃省', displayName: '甘肃', output: 340.2, rank: 10, area: 1254.8, yield: 271.1 }
]

const grainStructure = [
  { name: '玉米', value: 30123.5, color: '#166534' },
  { name: '稻谷', value: 20904.1, color: '#22c55e' },
  { name: '小麦', value: 14007.2, color: '#eaaa08' },
  { name: '薯类', value: 3073.7, color: '#0f766e' },
  { name: '豆类', value: 2393.2, color: '#84cc16' }
]

const purchaseProgress = [
  { name: '小麦累计收购', value: 5593, percent: 82, note: '全国各类粮食经营主体', color: '#166534' },
  { name: '油菜籽累计收购', value: 371, percent: 64, note: '截至2026年6月30日', color: '#eaaa08' }
]

const priceTabs = [
  { key: 'wheat', label: '小麦' },
  { key: 'corn', label: '玉米' },
  { key: 'rice', label: '稻谷' },
  { key: 'soybean', label: '大豆' }
]

const pricePeriods = [
  { key: 'month', label: '1个月', points: 5 },
  { key: 'quarter', label: '3个月', points: 8 },
  { key: 'half', label: '半年', points: 12 },
  { key: 'year', label: '1年', points: 18 }
]

const priceData = {
  wheat: {
    label: '小麦',
    current: 2.46,
    change: -0.03,
    series: [
      ['03-23', 2.52, 0.01], ['04-06', 2.51, -0.01], ['04-20', 2.50, -0.01], ['05-04', 2.49, -0.01],
      ['05-18', 2.48, -0.01], ['06-01', 2.47, -0.01], ['06-15', 2.45, -0.02], ['06-29', 2.46, 0.01],
      ['07-13', 2.47, 0.01], ['07-27', 2.46, -0.01], ['08-10', 2.45, -0.01], ['08-23', 2.46, 0.01]
    ]
  },
  corn: {
    label: '玉米',
    current: 2.38,
    change: 0.02,
    series: [
      ['03-23', 2.31, -0.02], ['04-06', 2.33, 0.02], ['04-20', 2.35, 0.02], ['05-04', 2.36, 0.01],
      ['05-18', 2.37, 0.01], ['06-01', 2.36, -0.01], ['06-15', 2.37, 0.01], ['06-29', 2.38, 0.01],
      ['07-13', 2.39, 0.01], ['07-27', 2.40, 0.01], ['08-10', 2.36, -0.04], ['08-23', 2.38, 0.02]
    ]
  },
  rice: {
    label: '稻谷',
    current: 2.82,
    change: 0.01,
    series: [
      ['03-23', 2.78, 0.00], ['04-06', 2.79, 0.01], ['04-20', 2.80, 0.01], ['05-04', 2.81, 0.01],
      ['05-18', 2.81, 0.00], ['06-01', 2.82, 0.01], ['06-15', 2.81, -0.01], ['06-29', 2.82, 0.01],
      ['07-13', 2.83, 0.01], ['07-27', 2.82, -0.01], ['08-10', 2.82, 0.00], ['08-23', 2.82, 0.00]
    ]
  },
  soybean: {
    label: '大豆',
    current: 4.72,
    change: -0.05,
    series: [
      ['03-23', 4.88, 0.02], ['04-06', 4.86, -0.02], ['04-20', 4.84, -0.02], ['05-04', 4.82, -0.02],
      ['05-18', 4.80, -0.02], ['06-01', 4.77, -0.03], ['06-15', 4.75, -0.02], ['06-29', 4.74, -0.01],
      ['07-13', 4.76, 0.02], ['07-27', 4.75, -0.01], ['08-10', 4.77, 0.02], ['08-23', 4.72, -0.05]
    ]
  }
}

const nationalDispatchNodes = [
  { id: 'henan_wh', name: '河南主产区粮库', type: 'warehouse', lng: 113.62, lat: 34.75, value: 3200, label: '粮库 · 小麦' },
  { id: 'shandong_wh', name: '山东鲁西储备库', type: 'warehouse', lng: 117.0, lat: 36.65, value: 2100, label: '粮库 · 小麦' },
  { id: 'wuhan_hub', name: '武汉中转中心', type: 'transit', lng: 114.31, lat: 30.52, value: 1600, label: '中转中心' },
  { id: 'changsha_proc', name: '长沙粮油加工企业', type: 'processing', lng: 112.94, lat: 28.23, value: 900, label: '加工企业' },
  { id: 'nanjing_wh', name: '南京储备节点', type: 'warehouse', lng: 118.78, lat: 32.04, value: 1200, label: '储备节点' },
  { id: 'lianyungang_port', name: '连云港粮食港口', type: 'port', lng: 119.22, lat: 34.60, value: 800, label: '港口' },
  { id: 'xian_rail', name: '西安铁路节点', type: 'rail', lng: 108.94, lat: 34.34, value: 700, label: '铁路节点' },
  { id: 'chengdu_wh', name: '成都应急储备库', type: 'warehouse', lng: 104.06, lat: 30.67, value: 900, label: '粮库 · 稻谷' }
]

const nationalDispatchRoutes = [
  {
    id: 'route-001',
    name: '河南小麦入鄂调拨线',
    from: 'henan_wh',
    to: 'wuhan_hub',
    status: 'transit',
    task: 'GS-DP-260823-01',
    grain: '小麦',
    quantity: 820,
    start: '河南主产区粮库',
    end: '武汉中转中心',
    vehicle: '鄂A·GS101',
    progress: 58,
    eta: '2026-08-24 09:30',
    risk: '低'
  },
  {
    id: 'route-002',
    name: '山东储备粮港口转运线',
    from: 'shandong_wh',
    to: 'lianyungang_port',
    status: 'normal',
    task: 'GS-DP-260823-02',
    grain: '小麦',
    quantity: 640,
    start: '山东鲁西储备库',
    end: '连云港粮食港口',
    vehicle: '鲁P·GS318',
    progress: 36,
    eta: '2026-08-24 14:20',
    risk: '低'
  },
  {
    id: 'route-003',
    name: '武汉至长沙加工补给线',
    from: 'wuhan_hub',
    to: 'changsha_proc',
    status: 'delay',
    task: 'GS-DP-260823-03',
    grain: '稻谷',
    quantity: 430,
    start: '武汉中转中心',
    end: '长沙粮油加工企业',
    vehicle: '鄂A·GS404',
    progress: 66,
    eta: '2026-08-23 22:10',
    risk: '中'
  },
  {
    id: 'route-004',
    name: '西安铁路应急补仓线',
    from: 'xian_rail',
    to: 'chengdu_wh',
    status: 'abnormal',
    task: 'GS-DP-260823-04',
    grain: '玉米',
    quantity: 560,
    start: '西安铁路节点',
    end: '成都应急储备库',
    vehicle: '陕A·GS277',
    progress: 42,
    eta: '2026-08-25 08:00',
    risk: '高'
  },
  {
    id: 'route-005',
    name: '江苏储备节点入鄂线',
    from: 'nanjing_wh',
    to: 'wuhan_hub',
    status: 'normal',
    task: 'GS-DP-260823-05',
    grain: '稻谷',
    quantity: 510,
    start: '南京储备节点',
    end: '武汉中转中心',
    vehicle: '苏A·GS566',
    progress: 74,
    eta: '2026-08-23 21:40',
    risk: '低'
  }
]

const insightCards = [
  {
    crop: '玉米',
    status: '阶段性宽松',
    tags: ['南方新粮上市', '贸易商售粮积极', '替代谷物进口增加']
  },
  {
    crop: '大豆',
    status: '供应总体宽松',
    tags: ['东北墒情适宜', '湖北早熟大豆上市', '全球供应宽松']
  }
]

const calendarRegions = [
  { region: '东北', main: '春玉米 · 乳熟', extra: '大豆 · 结荚', province: '黑龙江' },
  { region: '黄淮', main: '夏玉米 · 抽雄', extra: '小麦腾茬区田管', province: '河南' },
  { region: '江南', main: '中稻 · 乳熟', extra: '晚稻 · 分蘖', province: '湖北' },
  { region: '西北', main: '春小麦 · 收尾', extra: '玉米 · 灌浆', province: '新疆' }
]

const selectedRoute = ref(nationalDispatchRoutes[0])

const inventoryStats = computed(() => {
  const total = inventories.value.reduce((sum, item) => sum + Number(item.currentStock || 0), 0)
  const available = inventories.value.reduce((sum, item) => sum + Number(item.availableStock || 0), 0)
  const lowBuffer = inventories.value.filter((item) => Number(item.bufferDays || 999) < 7).length
  return { total, available, lowBuffer }
})

const inTransit = computed(() =>
  plans.value
    .filter((plan) => ['CONFIRMED', 'ADJUSTED'].includes(plan.status))
    .reduce((sum, plan) => sum + Number(plan.suggestedQuantity || 0), 0)
)

const routeSummary = computed(() => ({
  normal: data.routes.filter((route) => route.status === 'NORMAL').length,
  closed: data.routes.filter((route) => route.status === 'CLOSED').length,
  congested: data.routes.filter((route) => route.status === 'CONGESTED').length
}))

const systemTargets = computed(() => ({
  activeTasks: Number(data.kpis?.plans || plans.value.length || 0),
  inTransit: Math.round(inTransit.value),
  vehicles: Number(vehicles.value.length || data.kpis?.availableVehicles || 0),
  capacityAlerts: warehouseRows.value.filter((item) => item.rate >= 90).length,
  riskEvents: Number(data.kpis?.openAlerts || 0)
}))

const systemKpis = computed(() => [
  {
    key: 'activeTasks',
    label: '执行中调度任务',
    value: formatNumber(animatedKpis.activeTasks ?? systemTargets.value.activeTasks),
    unit: '',
    note: '来自调度方案接口',
    tone: 'tone-forest'
  },
  {
    key: 'inTransit',
    label: '在途粮食量',
    value: formatNumber(animatedKpis.inTransit ?? systemTargets.value.inTransit),
    unit: '吨',
    note: '已确认或已调整方案',
    tone: 'tone-blue'
  },
  {
    key: 'vehicles',
    label: '当前运输车辆',
    value: formatNumber(animatedKpis.vehicles ?? systemTargets.value.vehicles),
    unit: '辆',
    note: `${data.kpis?.availableVehicles ?? 0} 辆可调度`,
    tone: 'tone-green'
  },
  {
    key: 'capacityAlerts',
    label: '仓容预警',
    value: formatNumber(animatedKpis.capacityAlerts ?? systemTargets.value.capacityAlerts),
    unit: '处',
    note: '仓容率 ≥ 90%',
    tone: 'tone-gold'
  },
  {
    key: 'riskEvents',
    label: '风险事件',
    value: formatNumber(animatedKpis.riskEvents ?? systemTargets.value.riskEvents),
    unit: '条',
    note: `高风险 ${data.kpis?.criticalAlerts ?? 0} 条`,
    tone: (data.kpis?.criticalAlerts || 0) > 0 ? 'tone-red' : 'tone-orange'
  }
])

const selectedProvinceData = computed(() =>
  summerProvinces.find((item) => item.displayName === selectedProvince.value || item.name === selectedProvince.value) || summerProvinces[0]
)

const currentPrice = computed(() => priceData[priceTab.value])

const priceSeriesPoints = computed(() => {
  const period = pricePeriods.find((item) => item.key === pricePeriod.value) || pricePeriods[2]
  return currentPrice.value.series.slice(-period.points)
})

const riskScore = computed(() => {
  const openAlerts = Number(data.kpis?.openAlerts || 0)
  const critical = Number(data.kpis?.criticalAlerts || 0)
  const routeRisk = routeSummary.value.closed * 16 + routeSummary.value.congested * 7
  const stockRisk = inventoryStats.value.lowBuffer * 8
  return Math.min(100, Math.round(openAlerts * 5 + critical * 18 + routeRisk + stockRisk + 22))
})

const riskTone = computed(() => {
  if (riskScore.value >= 70) return 'danger'
  if (riskScore.value >= 45) return 'warn'
  return 'stable'
})

const riskVector = computed(() => [
  { name: '生产风险', value: 34 },
  { name: '气象风险', value: Math.min(100, 28 + (data.latestAlerts || []).filter((item) => item.riskType === 'WEATHER').length * 12) },
  { name: '库存风险', value: Math.min(100, 30 + inventoryStats.value.lowBuffer * 12) },
  { name: '物流风险', value: Math.min(100, 24 + routeSummary.value.closed * 22 + routeSummary.value.congested * 10) },
  { name: '市场价格风险', value: 42 }
])

const riskEvents = computed(() => {
  const live = (data.latestAlerts || []).slice(0, 4).map((alert) => ({
    id: `live-${alert.id}`,
    time: formatAlarmTime(alert.alarmTime),
    title: labels[alert.riskType] || alert.ruleName || '风险告警',
    detail: alert.detail,
    level: levelLabel(alert.level)
  }))
  return [
    ...live,
    { id: 'weather-demo', time: '08:30', title: '气象异常', detail: '江汉平原局地强降雨影响夏粮出库节奏', level: '中' },
    { id: 'price-demo', time: '10:20', title: '粮价异常', detail: '大豆价格连续两周下行，关注采购窗口', level: '低' },
    { id: 'route-demo', time: '12:05', title: '运输延误', detail: '武汉至长沙加工补给线预计延迟 45 分钟', level: '中' },
    { id: 'stock-demo', time: '14:15', title: '仓容过高', detail: '部分储备库仓容率接近 90%', level: '高' }
  ].slice(0, 6)
})

const warehouseRows = computed(() => {
  const warehouseNodes = (data.nodes || [])
    .filter((node) => node.type === 'WAREHOUSE' || String(node.name || '').includes('粮库'))
    .slice(0, 6)

  const fallbackNodes = warehouseNodes.length
    ? warehouseNodes
    : [
        { id: 1, name: '武汉中央储备粮库', capacity: 12000 },
        { id: 2, name: '江夏区粮食储备库', capacity: 8000 },
        { id: 3, name: '黄陂区粮食储备库', capacity: 9000 }
      ]

  return fallbackNodes.map((node, index) => {
    const rows = inventories.value.filter((item) => item.nodeId === node.id || item.nodeName === node.name)
    const stock = rows.reduce((sum, item) => sum + Number(item.currentStock || 0), 0) || [6300, 4600, 4800][index] || 3000
    const capacity = Number(node.capacity || stock * 1.32 || 1)
    const rate = Math.min(100, Math.round((stock / Math.max(capacity, 1)) * 100))
    const iot = iotRows.value.find((item) => item.nodeId === node.id)
    const grain = rows.length ? rows.map((item) => item.grainType).slice(0, 2).join(' / ') : '稻谷 / 小麦'
    const level = capacityLevel(rate)
    return {
      name: node.name,
      stock,
      capacity,
      rate,
      grain,
      temperature: Number(iot?.temperature ?? [24.5, 31.8, 26.2][index] ?? 25.6).toFixed(1),
      humidity: Number(iot?.humidity ?? [66, 72, 83][index] ?? 68).toFixed(0),
      status: level.label,
      level: level.className,
      filledCells: Math.max(1, Math.round(rate / 5))
    }
  })
})

const taskTimeline = computed(() => {
  const steps = ['任务创建', '审批', '装车', '出库', '运输', '到达', '卸货', '入库', '完成']
  const currentIndex = Math.min(steps.length - 1, Math.floor((Number(selectedRoute.value?.progress || 0) / 100) * steps.length))
  return steps.map((name, index) => ({
    name,
    done: index < currentIndex,
    current: index === currentIndex
  }))
})

async function load() {
  loading.value = true
  try {
    const [dashboard, inventoryRows, planRows, vehicleRows, iotData] = await Promise.all([
      api.get('/dashboard'),
      api.get('/inventories').catch(() => []),
      api.get('/dispatch').catch(() => []),
      api.get('/vehicles').catch(() => []),
      api.get('/state/iot').catch(() => [])
    ])
    Object.assign(data, dashboard || {})
    inventories.value = inventoryRows || []
    plans.value = planRows || []
    vehicles.value = vehicleRows || []
    iotRows.value = iotData || []
    animateKpis(systemTargets.value)
    lastUpdated.value = dayjs().format('HH:mm:ss')
    await nextTick()
    draw()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function scan() {
  scanning.value = true
  try {
    const result = await api.post('/alerts/scan')
    ElMessage.success(`扫描完成，新增 ${result.generatedAlerts} 条告警`)
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    scanning.value = false
  }
}

function animateKpis(targets) {
  window.cancelAnimationFrame(animationFrame)
  const startValues = {}
  const targetValues = {}
  Object.entries(targets).forEach(([key, value]) => {
    startValues[key] = Number(animatedKpis[key] || 0)
    targetValues[key] = Number(value || 0)
  })
  const start = performance.now()
  const duration = 780

  function tick(now) {
    const progress = Math.min(1, (now - start) / duration)
    const eased = 1 - Math.pow(1 - progress, 3)
    Object.keys(targetValues).forEach((key) => {
      const from = startValues[key]
      const to = targetValues[key]
      animatedKpis[key] = Math.round(from + (to - from) * eased)
    })
    if (progress < 1) animationFrame = window.requestAnimationFrame(tick)
  }

  animationFrame = window.requestAnimationFrame(tick)
}

function selectProvince(name) {
  selectedProvince.value = name
}

function selectRoute(route) {
  selectedRoute.value = route
  mapMode.value = 'dispatch'
}

function focusRegion(item) {
  selectedProvince.value = item.province
  mapMode.value = 'macro'
}

function capacityLevel(rate) {
  if (rate > 95) return { label: '预警', className: 'danger' }
  if (rate >= 90) return { label: '注意', className: 'warn' }
  if (rate >= 75) return { label: '较高', className: 'high' }
  return { label: '正常', className: 'normal' }
}

function routeStatusText(status) {
  return {
    normal: '正常',
    transit: '运输中',
    delay: '延迟风险',
    abnormal: '异常'
  }[status] || '监测中'
}

function riskClass(level) {
  return { 严重: 'critical', 高: 'high', 中: 'medium', 低: 'low' }[level] || 'medium'
}

function levelLabel(level) {
  return {
    CRITICAL: '严重',
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }[level] || level || '中'
}

function formatAlarmTime(value) {
  return value ? dayjs(value).format('HH:mm') : '--:--'
}

function formatNumber(value) {
  return new Intl.NumberFormat('zh-CN').format(Math.round(Number(value || 0)))
}

function draw() {
  if (!structureChart) return
  drawStructure()
  drawSankey()
  drawPrice()
  drawIndexGauge()
  drawIndexSpark()
  drawRiskRadar()
}

function drawStructure() {
  structureChart.setOption({
    color: grainStructure.map((item) => item.color),
    animationDuration: 800,
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(16,24,40,.94)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        const item = params[0]
        return `${item.name}<br/>产量：${item.value} 万吨<br/>占总产：${((item.value / 71487.6) * 100).toFixed(1)}%`
      }
    },
    grid: { left: 72, right: 30, top: 24, bottom: 32 },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#edf2ea' } },
      axisLabel: { color: '#667085' }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: grainStructure.map((item) => item.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#344054', fontWeight: 700 }
    },
    series: [
      {
        name: '2025粮食产量',
        type: 'bar',
        data: grainStructure.map((item) => ({
          value: item.value,
          itemStyle: { color: item.color, borderRadius: [0, 8, 8, 0] }
        })),
        barWidth: 18,
        label: {
          show: true,
          position: 'right',
          color: '#667085',
          formatter: '{c} 万吨'
        }
      }
    ]
  })
}

function drawSankey() {
  sankeyChart.setOption({
    animationDuration: 420,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(16,24,40,.94)',
      borderWidth: 0,
      textStyle: { color: '#fff' }
    },
    series: [
      {
        type: 'sankey',
        left: 16,
        right: 28,
        top: 16,
        bottom: 22,
        nodeWidth: 13,
        nodeGap: 12,
        emphasis: { focus: 'adjacency' },
        lineStyle: {
          color: 'gradient',
          opacity: .32,
          curveness: .5
        },
        label: {
          color: '#344054',
          fontSize: 11,
          fontWeight: 700
        },
        data: [
          { name: '主产区' },
          { name: '西北产区' },
          { name: '收购主体' },
          { name: '中央储备' },
          { name: '地方仓储' },
          { name: '调拨中心' },
          { name: '加工企业' },
          { name: '储备轮换' },
          { name: '消费区域' }
        ],
        links: [
          { source: '主产区', target: '收购主体', value: 42 },
          { source: '西北产区', target: '收购主体', value: 12 },
          { source: '收购主体', target: '中央储备', value: 18 },
          { source: '收购主体', target: '地方仓储', value: 28 },
          { source: '中央储备', target: '调拨中心', value: 14 },
          { source: '地方仓储', target: '调拨中心', value: 24 },
          { source: '调拨中心', target: '加工企业', value: 16 },
          { source: '调拨中心', target: '储备轮换', value: 10 },
          { source: '调拨中心', target: '消费区域', value: 12 }
        ]
      }
    ]
  })
}

function drawPrice() {
  const points = priceSeriesPoints.value
  priceChart.setOption({
    animationDuration: 820,
    color: ['#0f766e'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(16,24,40,.94)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        const point = points[params[0].dataIndex]
        const change = point[2] >= 0 ? `+${point[2].toFixed(2)}` : point[2].toFixed(2)
        return `${point[0]}<br/>价格：${point[1].toFixed(2)} 元/公斤<br/>环比变化：${change}`
      }
    },
    grid: { left: 34, right: 18, top: 24, bottom: 32 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((item) => item[0]),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#dfe9dc' } },
      axisLabel: { color: '#667085' }
    },
    yAxis: {
      type: 'value',
      scale: true,
      axisLabel: { color: '#667085' },
      splitLine: { lineStyle: { color: '#edf2ea' } }
    },
    series: [
      {
        name: currentPrice.value.label,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: points.map((item) => item[1]),
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(15,118,110,.18)' },
            { offset: 1, color: 'rgba(15,118,110,0)' }
          ])
        }
      }
    ]
  })
}

function drawIndexGauge() {
  indexGaugeChart.setOption({
    animationDuration: 700,
    series: [
      {
        type: 'gauge',
        min: 90,
        max: 120,
        startAngle: 180,
        endAngle: 0,
        center: ['50%', '68%'],
        radius: '108%',
        pointer: { show: false },
        progress: {
          show: true,
          roundCap: true,
          width: 13,
          itemStyle: { color: '#166534' }
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 13,
            color: [[.35, '#e9f2e5'], [.7, '#d8ecce'], [1, '#f5e8bf']]
          }
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        detail: { show: false },
        data: [{ value: 109.39 }]
      }
    ]
  })
}

function drawIndexSpark() {
  indexSparkChart.setOption({
    animationDuration: 700,
    grid: { left: 0, right: 0, top: 8, bottom: 4 },
    xAxis: { type: 'category', show: false, data: ['06-22', '06-29', '07-06', '07-13', '07-20', '07-27'] },
    yAxis: { type: 'value', show: false, scale: true },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: [110.08, 109.96, 109.82, 109.74, 109.78, 109.39],
        lineStyle: { width: 3, color: '#eaaa08' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(234,170,8,.22)' },
            { offset: 1, color: 'rgba(234,170,8,0)' }
          ])
        }
      }
    ]
  })
}

function drawRiskRadar() {
  riskRadarChart.setOption({
    animationDuration: 700,
    tooltip: {
      backgroundColor: 'rgba(16,24,40,.94)',
      borderWidth: 0,
      textStyle: { color: '#fff' }
    },
    radar: {
      radius: '66%',
      center: ['50%', '52%'],
      splitNumber: 4,
      indicator: riskVector.value.map((item) => ({ name: item.name, max: 100 })),
      axisName: { color: '#475467', fontWeight: 700, fontSize: 11 },
      axisLine: { lineStyle: { color: '#dfe9dc' } },
      splitLine: { lineStyle: { color: '#e8f0e4' } },
      splitArea: { areaStyle: { color: ['#ffffff', '#f8fbf6'] } }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: riskVector.value.map((item) => item.value),
            name: '风险指数',
            itemStyle: { color: '#f79009' },
            lineStyle: { width: 3, color: '#f79009' },
            areaStyle: { color: 'rgba(247,144,9,.16)' }
          }
        ]
      }
    ]
  })
}

function resize() {
  structureChart?.resize()
  sankeyChart?.resize()
  priceChart?.resize()
  indexGaugeChart?.resize()
  indexSparkChart?.resize()
  riskRadarChart?.resize()
}

watch([priceTab, pricePeriod], () => drawPrice())
watch(riskVector, () => drawRiskRadar(), { deep: true })

onMounted(() => {
  structureChart = echarts.init(structureEl.value)
  sankeyChart = echarts.init(sankeyEl.value)
  priceChart = echarts.init(priceEl.value)
  indexGaugeChart = echarts.init(indexGaugeEl.value)
  indexSparkChart = echarts.init(indexSparkEl.value)
  riskRadarChart = echarts.init(riskRadarEl.value)
  window.addEventListener('resize', resize)
  draw()
  load()
  refreshTimer = window.setInterval(load, 45000)
})

onBeforeUnmount(() => {
  window.cancelAnimationFrame(animationFrame)
  window.clearInterval(refreshTimer)
  window.removeEventListener('resize', resize)
  structureChart?.dispose()
  sankeyChart?.dispose()
  priceChart?.dispose()
  indexGaugeChart?.dispose()
  indexSparkChart?.dispose()
  riskRadarChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
  background:
    radial-gradient(circle at 18% -10%, rgba(132, 204, 22, .16), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, .82), rgba(247, 250, 245, 0) 330px),
    var(--gs-bg);
}

.command-intro {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 8px 0 4px;
}

.eyebrow {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667085;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
}

.eyebrow b {
  color: #166534;
  letter-spacing: 0;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, .42);
  animation: pulseDot 1.8s ease-out infinite;
}

.command-intro h1 {
  margin: 14px 0 10px;
  color: #182230;
  font-size: clamp(32px, 3.4vw, 48px);
  line-height: 1.12;
  font-weight: 860;
  letter-spacing: 0;
}

.command-intro p {
  max-width: 860px;
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 1.8;
}

.intro-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.summary-grid,
.map-grid,
.analytics-layout,
.monitor-grid,
.risk-layout,
.operations-layout,
.dispatch-layout {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}

.summary-grid {
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, .85fr);
}

.map-grid {
  grid-template-columns: minmax(0, 1.6fr) minmax(330px, .54fr);
}

.analytics-layout {
  grid-template-columns: minmax(400px, .82fr) minmax(0, 1.18fr);
}

.monitor-grid {
  grid-template-columns: minmax(320px, .72fr) minmax(420px, 1fr) minmax(310px, .64fr);
}

.risk-layout {
  grid-template-columns: minmax(0, 1.4fr) minmax(340px, .62fr);
}

.operations-layout {
  grid-template-columns: minmax(330px, .62fr) minmax(0, 1.38fr);
}

.dispatch-layout {
  grid-template-columns: minmax(0, 1.2fr) minmax(380px, .8fr);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 22px 24px 16px;
}

.section-head h2 {
  margin: 8px 0 0;
  color: #182230;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0;
}

.source-pill {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  min-height: 24px;
  padding: 4px 9px;
  border-radius: 999px;
  background: #f4f7f2;
  color: #667085;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.4;
}

.source-pill.official {
  background: #eef7ed;
  color: #166534;
}

.source-pill.system {
  background: #ecfdf3;
  color: #087443;
}

.source-pill.demo {
  background: #fff7df;
  color: #a16207;
}

.mini-time {
  color: #98a2b3;
  font-size: 12px;
  white-space: nowrap;
}

.realtime-kpis,
.macro-kpis {
  display: grid;
  gap: 12px;
  padding: 0 20px 20px;
}

.realtime-kpis {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.macro-kpis {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metric-card,
.macro-item {
  min-width: 0;
  display: grid;
  gap: 8px;
  padding: 15px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #fff;
  transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease;
}

.metric-card {
  animation: revealUp .45s ease-out both;
  animation-delay: var(--delay);
}

.metric-card:hover,
.macro-item:hover,
.warehouse-item:hover,
.region-card:hover,
.dispatch-table button:hover {
  transform: translateY(-2px);
  border-color: #b9dec0;
  box-shadow: 0 12px 24px rgba(16, 24, 40, .06);
}

.metric-card span,
.macro-item span {
  color: #667085;
  font-size: 12px;
}

.metric-card strong,
.macro-item strong {
  color: #182230;
  font-size: 30px;
  line-height: 1;
  font-weight: 820;
}

.metric-card em,
.macro-item em,
.province-stats em,
.purchase-copy em,
.latest-price em {
  margin-left: 4px;
  color: #98a2b3;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.metric-card small,
.macro-item small {
  overflow: hidden;
  color: #98a2b3;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.macro-item.green { border-top: 3px solid #22c55e; }
.macro-item.forest { border-top: 3px solid #166534; }
.macro-item.gold { border-top: 3px solid #eaaa08; }
.macro-item.blue { border-top: 3px solid #0f766e; }

.map-card,
.map-insight,
.chart-panel,
.purchase-card,
.market-card,
.index-card,
.risk-card,
.insight-card,
.calendar-card,
.warehouse-card,
.dispatch-table-card,
.timeline-card {
  overflow: hidden;
}

.map-head {
  align-items: center;
}

.segmented,
.mini-tabs,
.price-tabs {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border: 1px solid #e3eadf;
  border-radius: 10px;
  background: #f8fbf6;
}

.segmented button,
.mini-tabs button,
.price-tabs button {
  min-height: 32px;
  padding: 0 12px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #667085;
  font-weight: 800;
  cursor: pointer;
  transition: background .2s ease, color .2s ease, box-shadow .2s ease;
}

.segmented button.active,
.mini-tabs button.active,
.price-tabs button.active {
  background: #fff;
  color: #166534;
  box-shadow: 0 1px 2px rgba(16, 24, 40, .06);
}

.map-stage {
  height: 590px;
  padding: 0 14px 18px;
}

.map-insight {
  padding: 24px;
}

.map-insight h2 {
  margin: 16px 0 12px;
  color: #182230;
  font-size: 24px;
  font-weight: 850;
}

.rank-badge,
.route-status {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background: #eef7ed;
  color: #166534;
  font-size: 13px;
  font-weight: 800;
}

.province-stats {
  display: grid;
  gap: 12px;
  margin-top: 20px;
}

.province-stats div {
  padding: 16px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #f8fbf6;
}

.province-stats span,
.latest-price span,
.purchase-copy span,
.index-copy span {
  display: block;
  color: #667085;
  font-size: 12px;
}

.province-stats strong {
  display: block;
  margin-top: 8px;
  color: #182230;
  font-size: 30px;
  line-height: 1;
}

.panel-note {
  margin: 18px 0 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.8;
}

.route-status.transit { background: #e7f7f4; color: #0f766e; }
.route-status.normal { background: #eef7ed; color: #166534; }
.route-status.delay { background: #fff7df; color: #a16207; }
.route-status.abnormal { background: #fff1f2; color: #c01048; }

.route-detail {
  display: grid;
  gap: 10px;
  margin: 18px 0 0;
}

.route-detail div {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #edf2ea;
}

.route-detail dt {
  color: #98a2b3;
  font-size: 12px;
}

.route-detail dd {
  margin: 0;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
}

.progress-line {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-top: 18px;
}

.progress-line span {
  color: #667085;
  font-size: 12px;
}

.progress-line b {
  color: #166534;
}

.progress-line i {
  grid-column: 1 / -1;
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: #edf2ea;
}

.progress-line em {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #0f766e, #22c55e);
  transition: width .45s ease;
}

.big-number {
  color: #166534;
  font-size: 18px;
  white-space: nowrap;
}

.chart-box {
  height: 360px;
}

.purchase-list {
  display: grid;
  gap: 14px;
  padding: 0 20px 20px;
}

.purchase-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 94px;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #fff;
}

.purchase-copy strong {
  display: block;
  margin-top: 7px;
  color: #182230;
  font-size: 30px;
  line-height: 1;
}

.purchase-copy small {
  display: block;
  margin-top: 7px;
  color: #98a2b3;
}

.liquid-meter {
  width: 86px;
  height: 86px;
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(22, 101, 52, .12);
  border-radius: 50%;
  background: #f8fbf6;
  color: #182230;
  font-weight: 850;
}

.liquid-meter::before {
  content: "";
  position: absolute;
  inset: calc(100% - var(--level)) 0 0;
  background: linear-gradient(180deg, color-mix(in srgb, var(--tone) 52%, white), var(--tone));
  opacity: .88;
  transition: inset .45s ease;
}

.liquid-meter span {
  position: relative;
  z-index: 1;
}

.price-tabs {
  margin: 0 24px 8px;
}

.price-body {
  display: grid;
  grid-template-columns: 170px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  padding: 0 16px 18px 22px;
}

.latest-price {
  min-height: 158px;
  display: grid;
  align-content: center;
  padding: 16px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #f8fbf6;
}

.latest-price strong {
  margin-top: 10px;
  color: #182230;
  font-size: 34px;
  line-height: 1;
}

.latest-price small,
.index-copy small {
  margin-top: 9px;
  font-size: 12px;
  font-weight: 800;
}

.latest-price .up { color: #166534; }
.latest-price .down,
.index-copy .down { color: #c01048; }

.price-chart {
  height: 245px;
}

.index-grid {
  display: grid;
  grid-template-columns: minmax(130px, .8fr) minmax(130px, .7fr);
  gap: 8px;
  align-items: center;
  padding: 0 18px 18px;
}

.index-gauge {
  height: 168px;
}

.index-copy {
  min-width: 0;
  display: grid;
  align-content: center;
}

.index-copy strong {
  margin-top: 8px;
  color: #182230;
  font-size: 42px;
  line-height: 1;
}

.sparkline {
  height: 66px;
  margin-top: 10px;
}

.risk-content {
  display: grid;
  grid-template-columns: minmax(280px, .9fr) minmax(360px, 1fr);
  gap: 8px;
  padding: 0 18px 20px;
}

.radar-box {
  height: 360px;
}

.risk-score {
  min-width: 56px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #eef7ed;
  color: #166534;
  font-size: 20px;
}

.risk-score.warn {
  background: #fff7df;
  color: #a16207;
}

.risk-score.danger {
  background: #fff1f2;
  color: #c01048;
}

.risk-timeline {
  display: grid;
  gap: 10px;
  align-content: start;
  padding-top: 8px;
}

.risk-event {
  position: relative;
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr) 38px;
  gap: 10px;
  align-items: center;
  min-height: 58px;
  padding: 12px;
  border: 1px solid #e7eee3;
  border-left: 4px solid #22c55e;
  border-radius: 12px;
  background: #fff;
}

.risk-event span {
  color: #98a2b3;
  font-size: 12px;
}

.risk-event b {
  color: #182230;
  font-size: 13px;
}

.risk-event em {
  grid-column: 2 / 3;
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.risk-event i {
  grid-row: 1 / 3;
  grid-column: 3;
  justify-self: end;
  padding: 4px 8px;
  border-radius: 999px;
  background: #eef7ed;
  color: #166534;
  font-size: 11px;
  font-style: normal;
  font-weight: 800;
}

.risk-event.medium { border-left-color: #eaaa08; }
.risk-event.medium i { background: #fff7df; color: #a16207; }
.risk-event.high { border-left-color: #f79009; }
.risk-event.high i { background: #fff4e5; color: #b54708; }
.risk-event.critical { border-left-color: #f04438; }
.risk-event.critical i { background: #fff1f2; color: #c01048; }

.insight-list {
  display: grid;
  gap: 14px;
  padding: 0 20px 20px;
}

.insight-item {
  padding: 18px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background:
    linear-gradient(135deg, rgba(238, 247, 237, .86), rgba(255, 255, 255, .92));
}

.insight-item span {
  color: #667085;
  font-size: 12px;
  font-weight: 800;
}

.insight-item strong {
  display: block;
  margin-top: 8px;
  color: #166534;
  font-size: 24px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.tag-row i {
  padding: 6px 9px;
  border-radius: 999px;
  background: #fff;
  color: #475467;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.calendar-regions {
  display: grid;
  gap: 12px;
  padding: 0 20px 20px;
}

.region-card {
  width: 100%;
  display: grid;
  gap: 6px;
  padding: 16px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #fff;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease;
}

.region-card span {
  color: #166534;
  font-size: 13px;
  font-weight: 850;
}

.region-card b {
  color: #182230;
  font-size: 16px;
}

.region-card em {
  color: #667085;
  font-size: 12px;
  font-style: normal;
}

.warehouse-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 0 20px 20px;
}

.warehouse-item {
  min-width: 0;
  padding: 14px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #fff;
  transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease;
}

.warehouse-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.warehouse-top b {
  overflow: hidden;
  color: #182230;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.warehouse-top span {
  flex: 0 0 auto;
  padding: 4px 8px;
  border-radius: 999px;
  background: #eef7ed;
  color: #166534;
  font-size: 11px;
  font-weight: 800;
}

.warehouse-item.high .warehouse-top span { background: #fff7df; color: #a16207; }
.warehouse-item.warn .warehouse-top span { background: #fff4e5; color: #b54708; }
.warehouse-item.danger .warehouse-top span { background: #fff1f2; color: #c01048; }

.capacity-matrix {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 5px;
  margin-top: 14px;
}

.capacity-matrix i {
  aspect-ratio: 1;
  border-radius: 4px;
  background: #edf2ea;
}

.capacity-matrix i.filled {
  background: #22c55e;
}

.warehouse-item.high .capacity-matrix i.filled { background: #eaaa08; }
.warehouse-item.warn .capacity-matrix i.filled { background: #f79009; }
.warehouse-item.danger .capacity-matrix i.filled { background: #f04438; }

.bullet {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 42px;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
}

.bullet i {
  height: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: linear-gradient(90deg, #e5f3df 0 75%, #fff7df 75% 90%, #fff4e5 90% 95%, #fff1f2 95%);
}

.bullet em {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #166534;
}

.warehouse-item.high .bullet em { background: #eaaa08; }
.warehouse-item.warn .bullet em { background: #f79009; }
.warehouse-item.danger .bullet em { background: #f04438; }

.bullet b {
  color: #182230;
  font-size: 12px;
}

.warehouse-item dl {
  display: grid;
  gap: 6px;
  margin: 12px 0 0;
}

.warehouse-item dl div {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  min-width: 0;
}

.warehouse-item dt {
  color: #98a2b3;
  font-size: 11px;
}

.warehouse-item dd {
  overflow: hidden;
  margin: 0;
  color: #475467;
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dispatch-table {
  display: grid;
  gap: 10px;
  padding: 0 20px 20px;
}

.dispatch-table button {
  position: relative;
  display: grid;
  gap: 6px;
  min-height: 76px;
  padding: 14px 102px 14px 16px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #fff;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease;
}

.dispatch-table button.active {
  border-color: #9fd5a7;
  background: #f8fbf6;
}

.dispatch-table span {
  color: #166534;
  font-size: 12px;
  font-weight: 850;
}

.dispatch-table b {
  overflow: hidden;
  color: #182230;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dispatch-table em {
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dispatch-table i {
  position: absolute;
  right: 14px;
  top: 16px;
  padding: 5px 9px;
  border-radius: 999px;
  background: #eef7ed;
  color: #166534;
  font-size: 11px;
  font-style: normal;
  font-weight: 850;
}

.dispatch-table i.transit { background: #e7f7f4; color: #0f766e; }
.dispatch-table i.delay { background: #fff7df; color: #a16207; }
.dispatch-table i.abnormal { background: #fff1f2; color: #c01048; }

.task-timeline {
  display: grid;
  grid-template-columns: repeat(9, minmax(0, 1fr));
  gap: 8px;
  padding: 12px 20px 24px;
}

.task-step {
  display: grid;
  justify-items: center;
  gap: 8px;
  min-width: 0;
  color: #98a2b3;
  font-size: 12px;
  text-align: center;
}

.task-step i {
  width: 16px;
  height: 16px;
  border: 3px solid #dfe9dc;
  border-radius: 50%;
  background: #fff;
}

.task-step.done i {
  border-color: #22c55e;
  background: #22c55e;
}

.task-step.current {
  color: #166534;
  font-weight: 850;
}

.task-step.current i {
  border-color: #166534;
  box-shadow: 0 0 0 6px rgba(34, 197, 94, .14);
}

@keyframes revealUp {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulseDot {
  0% { box-shadow: 0 0 0 0 rgba(34, 197, 94, .42); }
  76% { box-shadow: 0 0 0 12px rgba(34, 197, 94, 0); }
  100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

@media (max-width: 1480px) {
  .summary-grid,
  .map-grid,
  .analytics-layout,
  .monitor-grid,
  .risk-layout,
  .operations-layout,
  .dispatch-layout {
    grid-template-columns: 1fr;
  }

  .realtime-kpis {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .macro-kpis,
  .warehouse-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .command-intro,
  .section-head,
  .map-head,
  .price-body,
  .risk-content,
  .index-grid {
    grid-template-columns: 1fr;
  }

  .command-intro,
  .section-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .map-stage {
    height: 520px;
  }

  .realtime-kpis,
  .macro-kpis,
  .warehouse-grid {
    grid-template-columns: 1fr;
  }

  .task-timeline {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .command-intro h1 {
    font-size: 34px;
  }

  .segmented,
  .mini-tabs,
  .price-tabs {
    width: 100%;
    overflow-x: auto;
  }

  .dispatch-table button {
    padding-right: 16px;
  }

  .dispatch-table i {
    position: static;
    width: fit-content;
  }
}
</style>
