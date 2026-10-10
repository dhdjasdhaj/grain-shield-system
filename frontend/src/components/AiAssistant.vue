<template>
  <div class="ai-assistant" :class="{ open }">
    <button class="ai-float" :class="{ pulse: !open }" @click="open = !open" aria-label="打开AI助手">
      <el-icon><ChatDotRound /></el-icon>
      <span>AI</span>
    </button>

    <aside class="ai-panel" aria-label="Grain Shield AI助手">
      <header class="ai-header">
        <div class="ai-title">
          <div class="ai-mark">
            <el-icon><MagicStick /></el-icon>
          </div>
          <div>
            <strong>Grain Shield AI 助手</strong>
            <span :class="{ ready: hasKey }">{{ hasKey ? '模型已配置' : 'AI密钥未配置 · 本地规则助手' }}</span>
          </div>
        </div>
        <button class="icon-button" @click="open = false" aria-label="关闭AI助手">
          <el-icon><Close /></el-icon>
        </button>
      </header>

      <section class="ai-context">
        <span>当前上下文</span>
        <b>{{ routeTitle }}</b>
        <em>可辅助研判风险、解释图表、生成调度处置建议</em>
      </section>

      <div class="quick-grid">
        <button v-for="item in quickPrompts" :key="item" @click="askQuick(item)">
          {{ item }}
        </button>
      </div>

      <section ref="messageBox" class="message-box">
        <article v-for="message in messages" :key="message.id" class="message" :class="message.role">
          <div class="avatar">
            <el-icon v-if="message.role === 'assistant'"><Cpu /></el-icon>
            <span v-else>{{ userInitial }}</span>
          </div>
          <div class="bubble">
            <p>{{ message.content }}</p>
            <small>{{ message.time }}</small>
          </div>
        </article>
      </section>

      <footer class="composer">
        <el-input
          v-model="draft"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          resize="none"
          placeholder="问我：当前风险怎么看？需要优先调度哪条线路？"
          @keydown.enter.exact.prevent="send"
        />
        <div class="composer-actions">
          <span>{{ hasKey ? '将调用已配置模型' : '未配置密钥，先使用本地规则回复' }}</span>
          <el-button type="primary" :loading="sending" @click="send">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </footer>
    </aside>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ChatDotRound, Close, Cpu, MagicStick, Promotion } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { aiAssistantConfig } from '../config/aiAssistant'

const open = ref(false)
const draft = ref('')
const sending = ref(false)
const messageBox = ref()
const route = useRoute()
const auth = useAuthStore()

const hasKey = computed(() => Boolean(aiAssistantConfig.apiKey && aiAssistantConfig.endpoint))
const userInitial = computed(() => auth.user?.realName?.slice(0, 1) || '我')
const routeTitle = computed(() => route.meta?.title || '全局态势')

const quickPrompts = ['解释当前风险', '生成调度建议', '总结粮情要点', '检查仓容压力']

const messages = ref([
  {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: '你好，我是 Grain Shield AI 助手。当前 AI 密钥为空，所以我先以本地规则助手模式工作：可以帮你梳理首页指标、风险事件、仓容压力和调度处置思路。',
    time: currentTime()
  }
])

function askQuick(text) {
  draft.value = text
  send()
}

async function send() {
  const text = draft.value.trim()
  if (!text || sending.value) return
  messages.value.push({
    id: crypto.randomUUID(),
    role: 'user',
    content: text,
    time: currentTime()
  })
  draft.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const answer = hasKey.value ? await callModel(text) : localAnswer(text)
    messages.value.push({
      id: crypto.randomUUID(),
      role: 'assistant',
      content: answer,
      time: currentTime()
    })
  } catch (error) {
    messages.value.push({
      id: crypto.randomUUID(),
      role: 'assistant',
      content: `模型调用失败：${error.message}。你可以先检查 AI 密钥、接口地址或改用本地规则助手。`,
      time: currentTime()
    })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function callModel(text) {
  const response = await fetch(aiAssistantConfig.endpoint, {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      authorization: `Bearer ${aiAssistantConfig.apiKey}`
    },
    body: JSON.stringify({
      model: aiAssistantConfig.model,
      messages: [
        {
          role: 'system',
          content: '你是粮食供应链 Control Tower 的智能助手，回答要聚焦粮情、仓容、风险预警和调度建议。'
        },
        { role: 'user', content: text }
      ]
    })
  })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  const data = await response.json()
  return data.choices?.[0]?.message?.content || '模型已返回，但没有可展示内容。'
}

function localAnswer(text) {
  const question = text.toLowerCase()
  if (question.includes('风险') || question.includes('预警')) {
    return '本地研判：先按严重程度查看风险事件 Timeline，再交叉检查物流线路状态、仓容率和库存缓冲天数。若出现运输阻断或仓容率超过 90%，建议优先生成备选线路和跨库调拨方案。'
  }
  if (question.includes('调度') || question.includes('线路') || question.includes('运输')) {
    return '调度建议：优先保障低库存节点与民生配送节点，避开异常或延迟风险路线；对正在运输中的任务，重点关注车辆当前位置、预计到达时间和当前阶段，必要时切换备用中转中心。'
  }
  if (question.includes('仓容') || question.includes('库存')) {
    return '仓容检查：0-75% 为正常，75-90% 为较高，90-95% 需要注意，超过 95% 进入预警。建议先处理仓容率高且温湿度异常的粮库，再看安全库存低于阈值的节点。'
  }
  if (question.includes('粮情') || question.includes('宏观') || question.includes('产量')) {
    return '粮情要点：先看全国夏粮产量和小麦产量，再看河南、山东、安徽等主产区的省级分布。宏观粮情用于判断供给格局，实时调度地图用于判断粮食正在往哪里流动。'
  }
  return '当前为本地规则助手模式。你可以问我“解释当前风险”“生成调度建议”“检查仓容压力”或“总结粮情要点”。配置 AI 密钥和接口后，可切换为真实模型回复。'
}

async function scrollToBottom() {
  await nextTick()
  if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight
}

function currentTime() {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(new Date())
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
  pointer-events: none;
}

.ai-float {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  gap: 0;
  position: absolute;
  right: 0;
  bottom: 0;
  border: 0;
  border-radius: 18px;
  background: linear-gradient(135deg, #166534, #22c55e);
  color: #fff;
  cursor: pointer;
  pointer-events: auto;
  box-shadow: 0 18px 36px rgba(22, 101, 52, .24);
  transition: transform .22s ease, box-shadow .22s ease, opacity .22s ease;
}

.ai-float:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 42px rgba(22, 101, 52, .3);
}

.ai-float .el-icon {
  font-size: 22px;
}

.ai-float span {
  margin-top: -10px;
  font-size: 11px;
  font-weight: 900;
}

.ai-float.pulse::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, .34);
  animation: aiPulse 2.2s ease-out infinite;
}

.ai-panel {
  width: min(410px, calc(100vw - 32px));
  height: min(680px, calc(100vh - 112px));
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr) auto;
  overflow: hidden;
  border: 1px solid #dce8d7;
  border-radius: 18px;
  background: rgba(255, 255, 255, .96);
  box-shadow: 0 28px 70px rgba(16, 24, 40, .16);
  pointer-events: auto;
  opacity: 0;
  visibility: hidden;
  transform: translateY(16px) scale(.98);
  transform-origin: right bottom;
  transition: opacity .22s ease, transform .22s ease, visibility .22s ease;
  backdrop-filter: blur(16px);
}

.open .ai-panel {
  opacity: 1;
  visibility: visible;
  transform: translateY(-74px) scale(1);
}

.open .ai-float {
  opacity: .92;
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 18px;
  border-bottom: 1px solid #edf2ea;
  background:
    linear-gradient(135deg, rgba(238, 247, 237, .9), rgba(255, 255, 255, .95));
}

.ai-title {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-mark {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: #166534;
  color: #fff;
  font-size: 18px;
}

.ai-title strong {
  display: block;
  color: #182230;
  font-size: 15px;
}

.ai-title span {
  display: block;
  margin-top: 4px;
  color: #a16207;
  font-size: 12px;
  font-weight: 700;
}

.ai-title span.ready {
  color: #166534;
}

.icon-button {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 1px solid #e3eadf;
  border-radius: 10px;
  background: #fff;
  color: #667085;
  cursor: pointer;
}

.icon-button:hover {
  border-color: #b9dec0;
  color: #166534;
  background: #f8fbf6;
}

.ai-context {
  display: grid;
  gap: 4px;
  margin: 14px 16px 0;
  padding: 13px;
  border: 1px solid #e7eee3;
  border-radius: 12px;
  background: #f8fbf6;
}

.ai-context span,
.ai-context em {
  color: #667085;
  font-size: 12px;
  font-style: normal;
}

.ai-context b {
  color: #166534;
  font-size: 14px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 12px 16px 0;
}

.quick-grid button {
  min-height: 36px;
  border: 1px solid #e7eee3;
  border-radius: 10px;
  background: #fff;
  color: #344054;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
}

.quick-grid button:hover {
  border-color: #b9dec0;
  color: #166534;
  background: #eef7ed;
}

.message-box {
  min-height: 0;
  display: grid;
  align-content: start;
  gap: 12px;
  overflow: auto;
  padding: 16px;
}

.message {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
}

.message.user {
  grid-template-columns: minmax(0, 1fr) 34px;
}

.message.user .avatar {
  grid-column: 2;
  grid-row: 1;
  background: #0f766e;
}

.message.user .bubble {
  grid-column: 1;
  grid-row: 1;
  justify-self: end;
  background: #166534;
  color: #fff;
}

.avatar {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #eef7ed;
  color: #166534;
  font-size: 15px;
  font-weight: 900;
}

.bubble {
  max-width: 100%;
  padding: 12px 13px;
  border-radius: 14px;
  background: #f8fbf6;
  color: #344054;
}

.bubble p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.bubble small {
  display: block;
  margin-top: 7px;
  color: rgba(102, 112, 133, .78);
  font-size: 10px;
}

.message.user .bubble small {
  color: rgba(255, 255, 255, .68);
}

.composer {
  padding: 14px 16px 16px;
  border-top: 1px solid #edf2ea;
  background: #fff;
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 10px;
}

.composer-actions span {
  color: #98a2b3;
  font-size: 11px;
}

@keyframes aiPulse {
  0% { box-shadow: 0 0 0 0 rgba(34, 197, 94, .34); }
  75% { box-shadow: 0 0 0 16px rgba(34, 197, 94, 0); }
  100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

@media (max-width: 760px) {
  .ai-assistant {
    right: 16px;
    bottom: 16px;
  }

  .ai-panel {
    height: min(620px, calc(100vh - 92px));
  }

  .open .ai-panel {
    transform: translateY(-70px) scale(1);
  }
}
</style>
