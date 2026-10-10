<template>
  <div ref="el" class="network"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import wuhanGeoJson from '../assets/maps/wuhan.json'

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  routes: { type: Array, default: () => [] },
  highlightedRoute: { type: Object, default: null }
})

const el = ref()
let chart
let activeTimer
let activeIndex = 0

const mapName = 'wuhan-grain-shield'
const nodeColors = { WAREHOUSE: '#166534', PROCESSING: '#eab308', DISTRIBUTION: '#3b82f6', LOGISTICS: '#22c55e' }
const nodeSymbols = { WAREHOUSE: 'pin', PROCESSING: 'diamond', DISTRIBUTION: 'circle' }
const routeColors = { NORMAL: '#22c55e', CONGESTED: '#eab308', CLOSED: '#ef4444' }
const statusText = { NORMAL: '正常', CONGESTED: '拥堵', CLOSED: '封闭', WARNING: '预警', OFFLINE: '离线' }
const typeText = { WAREHOUSE: '粮库', PROCESSING: '加工中心', DISTRIBUTION: '配送中心' }

function makeNodeData() {
  return props.nodes
    .filter((node) => node.longitude != null && node.latitude != null)
    .map((node) => {
      const color = nodeColors[node.type] || '#166534'
      return {
        id: String(node.id),
        name: node.name,
        value: [Number(node.longitude), Number(node.latitude), node.capacity || 0],
        node,
        symbol: nodeSymbols[node.type] || 'circle',
        symbolSize: node.type === 'WAREHOUSE' ? 44 : node.type === 'PROCESSING' ? 25 : 24,
        itemStyle: { color, borderColor: '#fff', borderWidth: 2, shadowBlur: 18, shadowColor: `${color}88` }
      }
    })
}

function isHighlightedRoute(route) {
  if (!props.highlightedRoute) return false
  const sourceId = props.highlightedRoute.id ?? props.highlightedRoute.routeId
  return sourceId != null && String(sourceId) === String(route.id)
}

function makeRouteData(nodeMap) {
  return props.routes
    .map((route) => {
      const start = nodeMap.get(String(route.startNodeId))
      const end = nodeMap.get(String(route.endNodeId))
      if (!start || !end) return null
      const highlighted = isHighlightedRoute(route)
      const color = highlighted ? '#166534' : routeColors[route.status] || routeColors.NORMAL
      return {
        name: route.name,
        coords: [start.value.slice(0, 2), end.value.slice(0, 2)],
        route,
        lineStyle: {
          color,
          width: highlighted ? 5 : route.status === 'CLOSED' ? 3.2 : route.status === 'CONGESTED' ? 2.8 : 2.2,
          type: route.status === 'CLOSED' ? 'dashed' : 'solid',
          curveness: highlighted ? .32 : route.status === 'CLOSED' ? .28 : .22,
          opacity: highlighted ? 1 : route.status === 'NORMAL' ? .68 : .95,
          shadowBlur: highlighted ? 24 : route.status === 'NORMAL' ? 7 : 15,
          shadowColor: highlighted ? 'rgba(22,101,52,.32)' : `${color}99`
        }
      }
    })
    .filter(Boolean)
}

function draw() {
  if (!chart) return
  const nodes = makeNodeData()
  const nodeMap = new Map(nodes.map((node) => [node.id, node]))
  const routes = makeRouteData(nodeMap)
  const riskNodes = nodes.filter((node) => {
    const linkedRouteRisk = props.routes.some((route) =>
      route.status !== 'NORMAL' && [String(route.startNodeId), String(route.endNodeId)].includes(node.id)
    )
    return node.node.status !== 'NORMAL' || linkedRouteRisk
  })

  chart.setOption({
    backgroundColor: 'transparent',
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(31, 41, 55, .94)',
      borderColor: 'rgba(255, 255, 255, .08)',
      borderWidth: 1,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        if (params.componentSubType === 'lines') {
          const route = params.data.route
          return `${route.name}<br/>${route.distanceKm} km · ${statusText[route.status] || route.status}`
        }
        if (params.componentSubType === 'effectScatter' || params.componentSubType === 'scatter') {
          const node = params.data.node
          return `${node.name}<br/>${typeText[node.type] || node.type} · ${statusText[node.status] || node.status}<br/>${node.region || ''}`
        }
        return params.name
      }
    },
    geo: {
      map: mapName,
      roam: true,
      zoom: 1.08,
      aspectScale: .95,
      layoutCenter: ['50%', '52%'],
      layoutSize: '97%',
      itemStyle: { areaColor: '#e8f1e4', borderColor: '#c9d9c4', borderWidth: 1 },
      emphasis: { itemStyle: { areaColor: '#d9edd3', borderColor: '#22c55e' }, label: { color: '#166534' } },
      label: { show: true, color: '#789176', fontSize: 10 }
    },
    series: [
      { name: '武汉市', type: 'map', map: mapName, geoIndex: 0, zlevel: 0, data: [], silent: true },
      {
        name: '运输路线',
        type: 'lines',
        coordinateSystem: 'geo',
        zlevel: 2,
        data: routes,
        effect: { show: true, period: 3.2, trailLength: .34, symbol: 'arrow', symbolSize: 8, color: '#ffffff' },
        blendMode: 'source-over',
        emphasis: { lineStyle: { width: 4.4 } }
      },
      {
        name: '风险波纹',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        zlevel: 4,
        data: riskNodes,
        rippleEffect: { brushType: 'stroke', scale: 4.5, period: 2.8 },
        showEffectOn: 'render',
        symbol: 'circle',
        symbolSize: 22,
        itemStyle: { color: '#f59e0b' },
        label: { show: false }
      },
      {
        name: '供应链节点',
        type: 'scatter',
        coordinateSystem: 'geo',
        zlevel: 5,
        data: nodes,
        symbol: (value, params) => params.data.symbol,
        symbolSize: (value, params) => params.data.symbolSize,
        label: {
          show: true,
          formatter: (params) => params.data.name,
          position: 'right',
          color: '#1f2937',
          fontSize: 11,
          fontWeight: 700,
          textBorderColor: 'rgba(255,255,255,.82)',
          textBorderWidth: 4,
          width: 112,
          overflow: 'truncate'
        },
        emphasis: { scale: 1.24, label: { color: '#166534' } }
      }
    ]
  })

  startHighlight(nodes.length)
}

function startHighlight(total) {
  window.clearInterval(activeTimer)
  if (!total) return
  activeTimer = window.setInterval(() => {
    chart?.dispatchAction({ type: 'downplay', seriesIndex: 3 })
    chart?.dispatchAction({ type: 'highlight', seriesIndex: 3, dataIndex: activeIndex % total })
    activeIndex += 1
  }, 1800)
}

function resize() {
  chart?.resize()
}

onMounted(() => {
  echarts.registerMap(mapName, wuhanGeoJson)
  chart = echarts.init(el.value)
  draw()
  window.addEventListener('resize', resize)
})

watch(() => [props.nodes, props.routes, props.highlightedRoute], draw, { deep: true })

onBeforeUnmount(() => {
  window.clearInterval(activeTimer)
  window.removeEventListener('resize', resize)
  chart?.dispose()
})
</script>

<style scoped>
.network {
  height: 100%;
  min-height: 360px;
  overflow: hidden;
  border-radius: 12px;
  background:
    linear-gradient(90deg, rgba(22,101,52,.06) 1px, transparent 1px),
    linear-gradient(0deg, rgba(22,101,52,.045) 1px, transparent 1px),
    #f1f7ee;
  background-size: 44px 44px;
}
</style>
