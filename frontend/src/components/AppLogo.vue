<template>
  <div class="logo-wrap" :class="{ compact, light }">
    <div class="mark">
      <svg viewBox="0 0 64 64" aria-hidden="true">
        <path d="M32 5 53 13v16c0 14-8.4 24.9-21 30C19.4 53.9 11 43 11 29V13z" fill="currentColor" opacity=".18"/>
        <path d="M32 8 50 15v14c0 11.7-6.7 21.2-18 26.2C20.7 50.2 14 40.7 14 29V15z" fill="none" stroke="currentColor" stroke-width="3"/>
        <path d="M24 38c7-1 11-6 12-15 5 6 5 14-1 19-4 4-9 4-13 1 2 0 4-2 5-4-2 1-4 1-6 0 2-1 3-3 3-5z" fill="currentColor"/>
      </svg>
    </div>
    <div v-if="!compact" class="logo-copy">
      <strong>Grain Shield</strong>
      <span>粮安智盾 · 风险预警与调度</span>
    </div>
  </div>
</template>
<script setup>defineProps({ compact: Boolean, light: Boolean })</script>
<style scoped>
.logo-wrap{display:flex;align-items:center;gap:12px;color:#1f2937}.mark{width:43px;height:43px;display:grid;place-items:center;color:#166534;background:linear-gradient(135deg,#ecfdf3,#dff4d6);border:1px solid rgba(22,101,52,.12);border-radius:12px;box-shadow:0 14px 28px rgba(22,101,52,.12)}.mark svg{width:34px;height:34px}.logo-copy{display:flex;flex-direction:column;min-width:0}.logo-copy strong{font-size:18px;letter-spacing:0}.logo-copy span{font-size:10px;color:#6f806f;margin-top:2px;white-space:nowrap}.compact .mark{width:40px;height:40px}.light{color:#fff}.light .mark{background:rgba(255,255,255,.14);border-color:rgba(255,255,255,.2);color:#d9f99d;box-shadow:none}.light .logo-copy span{color:rgba(255,255,255,.7)}
</style>
