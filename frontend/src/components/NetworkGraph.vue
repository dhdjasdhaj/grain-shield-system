<template>
  <div ref="el" class="network"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  routes: { type: Array, default: () => [] }
})

const el = ref()
let chart
let activeTimer
let activeIndex = 0

const nodeColors = { WAREHOUSE: '#166534', PROCESSING: '#eab308', DISTRIBUTION: '#3b82f6', LOGISTICS: '#22c55e' }
const routeColors = { NORMAL: '#68b85f', CONGESTED: '#eab308', CLOSED: '#ef4444' }
const statusText = { NORMAL: '正常', CONGESTED: '拥堵', CLOSED: '封闭', WARNING: '预警', OFFLINE: '离线' }
const typeText = { WAREHOUSE: '粮库', PROCESSING: '加工中心', DISTRIBUTION: '配送中心' }

function normalizeNodes() {
  if (!props.nodes.length) return []
  const lngs = props.nodes.map((node) => Number(node.longitude || 0))
  const lats = props.nodes.map((node) => Number(node.latitude || 0))
  const minLng = Math.min(...lngs)
  const maxLng = Math.max(...lngs)
  const minLat = Math.min(...lats)
  const maxLat = Math.max(...lats)
  const lngSpan = Math.max(maxLng - minLng, 0.01)
  const latSpan = Math.max(maxLat - minLat, 0.01)

  return props.nodes.map((node, index) => {
    const hasGeo = node.longitude != null && node.latitude != null
    const x = hasGeo ? 95 + ((Number(node.longitude) - minLng) / lngSpan) * 810 : 180 + index * 90
    const y = hasGeo ? 80 + (1 - (Number(node.latitude) - minLat) / latSpan) * 420 : 130 + (index % 3) * 120
    const color = nodeColors[node.type] || '#166534'
    return {
      id: String(node.id),
      name: node.name,
      value: [x, y, node.capacity || 0],
      node,
      symbol: node.type === 'WAREHOUSE' ? 'roundRect' : node.type === 'PROCESSING' ? 'diamond' : 'circle',
      symbolSize: node.type === 'WAREHOUSE' ? 28 : 23,
      itemStyle: { color, borderColor: '#fff', borderWidth: 2, shadowBlur: 16, shadowColor: `${color}66` }
    }
  })
}

function makeRouteData(nodeMap) {
  return props.routes
    .map((route) => {
      const start = nodeMap.get(String(route.startNodeId))
      const end = nodeMap.get(String(route.endNodeId))
      if (!start || !end) return null
      const color = routeColors[route.status] || '#68b85f'
      return {
        name: route.name,
        coords: [start.value.slice(0, 2), end.value.slice(0, 2)],
        route,
        lineStyle: {
          color,
          width: route.status === 'CLOSED' ? 3.2 : route.status === 'CONGESTED' ? 2.7 : 2,
          type: route.status === 'CLOSED' ? 'dashed' : 'solid',
          curveness: route.status === 'CLOSED' ? .18 : .1,
          opacity: route.status === 'NORMAL' ? .72 : .95,
          shadowBlur: route.status === 'NORMAL' ? 7 : 12,
          shadowColor: `${color}88`
        }
      }
    })
    .filter(Boolean)
}

function draw() {
  if (!chart) return
  const normalized = normalizeNodes()
  const nodeMap = new Map(normalized.map((node) => [node.id, node]))
  const routes = makeRouteData(nodeMap)
  const activeNodes = normalized.filter((node) => {
    const linkedRisk = props.routes.some((route) =>
      route.status !== 'NORMAL' && [String(route.startNodeId), String(route.endNodeId)].includes(node.id)
    )
    return node.node.status !== 'NORMAL' || linkedRisk
  })

  chart.setOption({
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(31, 41, 55, .94)',
      borderColor: 'rgba(255, 255, 255, .08)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        if (params.componentSubType === 'lines') {
          const route = params.data.route
          return `${route.name}<br/>${route.distanceKm} km · ${statusText[route.status] || route.status}`
        }
        const node = params.data.node
        return `${node.name}<br/>${typeText[node.type] || node.type} · ${statusText[node.status] || node.status}<br/>${node.region || ''}`
      }
    },
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { min: 0, max: 1000, show: false, type: 'value' },
    yAxis: { min: 0, max: 580, show: false, type: 'value' },
    graphic: [
      {
        type: 'rect',
        left: 0,
        top: 0,
        right: 0,
        bottom: 0,
        shape: { width: 1000, height: 580 },
        style: {
          fill: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
            { offset: 0, color: 'rgba(249, 252, 247, .98)' },
            { offset: 1, color: 'rgba(233, 244, 228, .98)' }
          ])
        },
        silent: true
      },
      ...[120, 260, 400, 540, 680, 820].map((x) => ({
        type: 'line',
        shape: { x1: x, y1: 42, x2: x + 80, y2: 540 },
        style: { stroke: 'rgba(22,101,52,.08)', lineWidth: 1 },
        silent: true
      }))
    ],
    series: [
      {
        name: '运输流向',
        type: 'lines',
        coordinateSystem: 'cartesian2d',
        zlevel: 2,
        data: routes,
        effect: { show: true, period: 4.2, trailLength: .24, symbol: 'circle', symbolSize: 5, color: '#166534' },
        emphasis: { disabled: false, lineStyle: { width: 4 } }
      },
      {
        name: '风险节点',
        type: 'effectScatter',
        coordinateSystem: 'cartesian2d',
        zlevel: 4,
        data: activeNodes,
        rippleEffect: { brushType: 'stroke', scale: 4.2, period: 3 },
        showEffectOn: 'render',
        symbolSize: (value, params) => params.data.symbolSize + 5,
        itemStyle: { color: (params) => params.data.itemStyle.color },
        label: { show: false }
      },
      {
        name: '节点',
        type: 'scatter',
        coordinateSystem: 'cartesian2d',
        zlevel: 5,
        data: normalized,
        symbolSize: (value, params) => params.data.symbolSize,
        symbol: (value, params) => params.data.symbol,
        label: {
          show: true,
          formatter: '{b}',
          position: 'bottom',
          distance: 8,
          color: '#395342',
          fontSize: 11,
          width: 96,
          overflow: 'truncate'
        },
        emphasis: { scale: 1.25, label: { color: '#166534', fontWeight: 700 } }
      }
    ]
  })
  startHighlight(normalized.length)
}

function startHighlight(total) {
  window.clearInterval(activeTimer)
  if (!total) return
  activeTimer = window.setInterval(() => {
    chart?.dispatchAction({ type: 'downplay', seriesIndex: 2 })
    chart?.dispatchAction({ type: 'highlight', seriesIndex: 2, dataIndex: activeIndex % total })
    activeIndex += 1
  }, 1800)
}

function resize() {
  chart?.resize()
}

onMounted(() => {
  chart = echarts.init(el.value)
  draw()
  window.addEventListener('resize', resize)
})

watch(() => [props.nodes, props.routes], draw, { deep: true })

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
  background: #edf6e9;
}
</style>
