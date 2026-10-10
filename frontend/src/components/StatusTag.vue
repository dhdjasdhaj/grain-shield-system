<template><el-tag :type="type" :effect="effect" round>{{ label }}</el-tag></template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ value: [String, Boolean], map: Object, effect: { type: String, default: 'light' } })
const defaults = {
  NORMAL:['正常','success'], WARNING:['预警','warning'], OFFLINE:['离线','info'],
  CONGESTED:['拥堵','warning'], CLOSED:['封闭','danger'], AVAILABLE:['可用','success'], IN_TRANSIT:['运输中','primary'], MAINTENANCE:['维护','warning'],
  LOW:['低','info'], MEDIUM:['中','warning'], HIGH:['高','danger'], CRITICAL:['紧急','danger'],
  NEW:['待处理','danger'], CONFIRMED:['已确认','warning'], IGNORED:['已忽略','info'], ANALYZING:['分析中','primary'],
  DRAFT:['待审核','warning'], ADJUSTED:['已调整','primary'], REJECTED:['已驳回','info'], COMPLETED:['已完成','success'],
  ADMIN:['系统管理员','danger'], DISPATCHER:['调度指挥员','primary'], NODE_MANAGER:['节点管理员','success'],
  true:['启用','success'], false:['停用','info']
}
const item = computed(() => (props.map || defaults)[String(props.value)] || [String(props.value ?? '-'),'info'])
const label = computed(() => item.value[0])
const type = computed(() => item.value[1])
</script>
