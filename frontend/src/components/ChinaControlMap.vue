<template>
  <div ref="el" class="china-map"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import chinaGeoJson from '../assets/maps/china.json'

const props = defineProps({
  mode: { type: String, default: 'macro' },
  provinces: { type: Array, default: () => [] },
  selectedProvince: { type: String, default: '' },
  selectedRouteId: { type: String, default: '' },
  nodes: { type: Array, default: () => [] },
  routes: { type: Array, default: () => [] }
})

const emit = defineEmits(['select-province', 'select-route'])
const el = ref()
let chart
const mapName = 'china-grain-control'

const routeColors = {
  normal: '#16a34a',
  transit: '#0f766e',
  delay: '#d97706',
  abnormal: '#dc2626'
}

const nodeColors = {
  warehouse: '#166534',
  transit: '#0f766e',
  processing: '#d97706',
  port: '#2563eb',
  rail: '#64748b',
  vehicle: '#0ea5e9'
}

function draw() {
  if (!chart) return
  props.mode === 'dispatch' ? drawDispatch() : drawMacro()
}

function drawMacro() {
  const data = props.provinces.map((item) => {
    const selected = props.selectedProvince && (item.displayName === props.selectedProvince || item.name === props.selectedProvince)
    return {
      ...item,
      name: item.mapName || item.name,
      value: item.output,
      itemStyle: selected
        ? {
            areaColor: '#166534',
            borderColor: '#0f5132',
            borderWidth: 1.5,
            shadowBlur: 16,
            shadowColor: 'rgba(22,101,52,.22)'
          }
        : undefined,
      label: selected
        ? {
            show: true,
            color: '#fff',
            fontWeight: 800
          }
        : undefined
    }
  })
  const max = Math.max(...data.map((item) => item.value || 0), 1)

  chart.setOption({
    backgroundColor: 'transparent',
    animationDuration: 600,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(16,24,40,.94)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        const item = params.data
        if (!item) return `${params.name}<br/>暂无公开数据`
        const area = item.area ? `${item.area} 万亩` : '待接入省级公开明细'
        const yieldText = item.yield ? `${item.yield} kg/亩` : '待接入省级公开明细'
        return [
          `${item.displayName || item.name}`,
          `夏粮产量：${item.output} 万吨`,
          `全国排名：第 ${item.rank} 位`,
          `播种面积：${area}`,
          `单产：${yieldText}`
        ].join('<br/>')
      }
    },
    visualMap: {
      min: 0,
      max,
      left: 18,
      bottom: 18,
      orient: 'horizontal',
      itemWidth: 14,
      itemHeight: 90,
      calculable: false,
      text: ['高', '低'],
      textStyle: { color: '#667085', fontSize: 11 },
      inRange: {
        color: ['#eef4ee', '#cdebc8', '#76c884', '#2f8f4e', '#166534']
      }
    },
    geo: {
      map: mapName,
      roam: true,
      zoom: 1.12,
      layoutCenter: ['50%', '52%'],
      layoutSize: '96%',
      itemStyle: { areaColor: '#eef4ee', borderColor: '#d0d5dd', borderWidth: 1 },
      emphasis: { itemStyle: { areaColor: '#cdebc8', borderColor: '#16a34a' }, label: { color: '#101828' } },
      label: { show: false }
    },
    series: [
      {
        name: '2026夏粮产量',
        type: 'map',
        map: mapName,
        geoIndex: 0,
        data,
        emphasis: {
          label: { show: true, color: '#101828', fontWeight: 700 }
        }
      }
    ]
  }, true)
}

function drawDispatch() {
  const routeData = props.routes
    .map((route) => {
      const start = props.nodes.find((node) => node.id === route.from)
      const end = props.nodes.find((node) => node.id === route.to)
      if (!start || !end) return null
      const color = routeColors[route.status] || routeColors.normal
      const selected = props.selectedRouteId && props.selectedRouteId === route.id
      return {
        name: route.name,
        coords: [[start.lng, start.lat], [end.lng, end.lat]],
        route,
        lineStyle: {
          color,
          width: selected ? 4 : route.status === 'abnormal' ? 3 : 2,
          curveness: .22,
          opacity: props.selectedRouteId ? (selected ? .98 : .22) : .72,
          shadowBlur: selected ? 16 : 8,
          shadowColor: `${color}66`
        }
      }
    })
    .filter(Boolean)

  const nodeData = props.nodes.map((node) => ({
    name: node.name,
    value: [node.lng, node.lat, node.value || 1],
    node,
    symbolSize: node.type === 'vehicle' ? 10 : 18,
    itemStyle: {
      color: nodeColors[node.type] || '#166534',
      borderColor: '#fff',
      borderWidth: 2,
      shadowBlur: 10,
      shadowColor: 'rgba(16,24,40,.18)'
    }
  }))

  chart.setOption({
    backgroundColor: 'transparent',
    animationDuration: 700,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(16,24,40,.94)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        if (params.componentSubType === 'lines') {
          const route = params.data.route
          return [
            route.name,
            `调度任务：${route.task}`,
            `粮食品种：${route.grain}`,
            `运输数量：${route.quantity} 吨`,
            `起点：${route.start}`,
            `终点：${route.end}`,
            `车辆：${route.vehicle}`,
            `进度：${route.progress}%`,
            `预计到达：${route.eta}`,
            `风险等级：${route.risk}`
          ].join('<br/>')
        }
        const node = params.data.node
        return `${node.name}<br/>${node.label}<br/>Demo Data / 模拟调度数据`
      }
    },
    geo: {
      map: mapName,
      roam: true,
      zoom: 1.16,
      layoutCenter: ['50%', '52%'],
      layoutSize: '96%',
      itemStyle: { areaColor: '#f2f4f7', borderColor: '#d0d5dd', borderWidth: 1 },
      emphasis: { itemStyle: { areaColor: '#eef4ee', borderColor: '#16a34a' }, label: { color: '#101828' } },
      label: { show: false }
    },
    series: [
      {
        name: '运输线路',
        type: 'lines',
        coordinateSystem: 'geo',
        zlevel: 2,
        data: routeData,
        effect: {
          show: true,
          period: 6.4,
          trailLength: .22,
          symbol: 'circle',
          symbolSize: 5,
          color: '#fff'
        },
        emphasis: { lineStyle: { width: 4, opacity: 1 } }
      },
      {
        name: '车辆光点',
        type: 'lines',
        coordinateSystem: 'geo',
        zlevel: 3,
        data: routeData,
        lineStyle: { opacity: 0 },
        effect: {
          show: true,
          period: 9.2,
          trailLength: .08,
          symbol: 'circle',
          symbolSize: 8,
          color: '#0f766e'
        },
        tooltip: { show: false }
      },
      {
        name: '供应链节点',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        zlevel: 4,
        data: nodeData,
        rippleEffect: { scale: 2.8, brushType: 'stroke', period: 3.4 },
        label: {
          show: true,
          formatter: '{b}',
          position: 'right',
          color: '#344054',
          fontSize: 11,
          fontWeight: 700,
          textBorderColor: '#fff',
          textBorderWidth: 3,
          width: 92,
          overflow: 'truncate'
        },
        emphasis: { scale: 1.18 }
      }
    ]
  }, true)
}

function resize() {
  chart?.resize()
}

onMounted(() => {
  echarts.registerMap(mapName, chinaGeoJson)
  chart = echarts.init(el.value)
  chart.on('click', (params) => {
    if (props.mode === 'macro' && params.componentSubType === 'map') {
      const item = params.data
      if (item) emit('select-province', item.displayName || item.name)
    }
    if (props.mode === 'dispatch' && params.componentSubType === 'lines') {
      emit('select-route', params.data.route)
    }
  })
  draw()
  window.addEventListener('resize', resize)
})

watch(() => [props.mode, props.provinces, props.selectedProvince, props.selectedRouteId, props.nodes, props.routes], draw, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
})
</script>

<style scoped>
.china-map {
  width: 100%;
  height: 100%;
  min-height: 520px;
}
</style>
