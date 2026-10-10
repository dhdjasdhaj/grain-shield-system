<template>
  <div class="login-page">
    <section class="showcase">
      <AppLogo light />
      <div class="showcase-copy">
        <span>GRAIN SHIELD SYSTEM</span>
        <h1>把粮食供应链<br>装进一块动态屏幕</h1>
        <p>实时掌握库存、粮情、交通、气象与调度建议，让风险从发现到处置都有迹可循。</p>
      </div>
      <div class="phone-stack" aria-hidden="true">
        <div class="phone phone-back"></div>
        <div class="phone phone-front">
          <div class="phone-bar"></div>
          <div class="phone-screen">
            <i v-for="n in 18" :key="n"></i>
          </div>
          <div class="phone-stats">
            <span>LIVE</span>
            <b>Risk 73</b>
          </div>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-title">
          <span>欢迎回来</span>
          <h2>登录 Grain Shield</h2>
        </div>
        <el-form @keyup.enter="submit">
          <el-form-item>
            <el-input v-model="form.username" size="large" prefix-icon="User" placeholder="账号" />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              size="large"
              prefix-icon="Key"
              type="password"
              show-password
              placeholder="密码"
            />
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">
            进入系统
            <el-icon><Right /></el-icon>
          </el-button>
        </el-form>
        <div class="demo-title">演示账号</div>
        <div class="demo-list">
          <button v-for="a in accounts" :key="a.username" @click="pick(a)">
            <b>{{ a.label }}</b>
            <span>{{ a.username }} / {{ a.password }}</span>
          </button>
        </div>
      </div>
      <footer>课程原型系统 · 模拟业务数据 · 调度建议仅供人工决策参考</footer>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLogo from '../components/AppLogo.vue'
import { useAuthStore } from '../stores/auth'

const form = reactive({ username: 'dispatcher', password: 'dispatch123' })
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()
const accounts = [
  { label: '系统管理员', username: 'admin', password: 'admin123' },
  { label: '调度指挥员', username: 'dispatcher', password: 'dispatch123' },
  { label: '节点管理员', username: 'node', password: 'node123' }
]

function pick(account) {
  form.username = account.username
  form.password = account.password
}

async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(420px, .85fr);
  background:
    radial-gradient(circle at 15% 10%, rgba(245, 250, 238, .68), transparent 34%),
    linear-gradient(135deg, #91bb7f 0%, #c6ddba 48%, #a5c895 100%);
}

.showcase {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  padding: 48px 7vw 58px;
  background:
    radial-gradient(circle at 72% 20%, rgba(217, 249, 157, .25), transparent 32%),
    linear-gradient(90deg, rgba(255,255,255,.08) 1px, transparent 1px),
    linear-gradient(0deg, rgba(255,255,255,.07) 1px, transparent 1px),
    linear-gradient(135deg, #166534 0%, #3f7d36 58%, #85b866 100%);
  background-size: 62px 62px;
  color: #fff;
  overflow: hidden;
}

.showcase-copy {
  max-width: 720px;
  position: relative;
  z-index: 2;
}

.showcase-copy span {
  color: #d9f99d;
  font-size: 12px;
  letter-spacing: .22em;
  font-weight: 800;
}

.showcase-copy h1 {
  margin: 24px 0 20px;
  color: #fff;
  font-size: clamp(44px, 5.4vw, 76px);
  line-height: 1;
  letter-spacing: 0;
  font-weight: 920;
}

.showcase-copy p {
  max-width: 560px;
  margin: 0;
  color: rgba(255, 255, 255, .66);
  font-size: 15px;
  line-height: 1.9;
}

.phone-stack {
  position: absolute;
  right: 7%;
  bottom: 5%;
  width: 300px;
  height: 430px;
  perspective: 1000px;
}

.phone {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255,255,255,.62);
  border-radius: 38px;
  background: rgba(255,255,255,.2);
  box-shadow: 0 44px 80px rgba(20,70,36,.28);
  backdrop-filter: blur(18px);
}

.phone-back {
  transform: translate(-54px, 28px) rotate(-8deg);
  opacity: .48;
  background: linear-gradient(145deg, rgba(255,255,255,.3), rgba(236,253,243,.08));
}

.phone-front {
  padding: 18px;
  transform: rotate(5deg);
  animation: floatPhone 5.8s ease-in-out infinite;
  overflow: hidden;
}

.phone-front::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 24%, rgba(255,255,255,.18) 42%, transparent 58%);
  transform: translateX(-130%);
  animation: glint 5s ease-in-out infinite;
}

.phone-bar {
  width: 72px;
  height: 7px;
  margin: 0 auto 18px;
  border-radius: 999px;
  background: rgba(255,255,255,.5);
}

.phone-screen {
  height: 300px;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 9px;
  padding: 18px;
  border-radius: 26px;
  background:
    linear-gradient(110deg, rgba(22,101,52,.18), rgba(132,204,22,.26)),
    #edf7e9;
}

.phone-screen i {
  border-radius: 999px;
  background: #166534;
  opacity: .22;
  animation: dotWave 2.4s ease-in-out infinite;
}

.phone-screen i:nth-child(3n) {
  background: #fff;
  opacity: .26;
  animation-delay: .18s;
}

.phone-screen i:nth-child(4n) {
  background: #eab308;
  opacity: .36;
  animation-delay: .34s;
}

.phone-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 58px;
  color: rgba(255,255,255,.7);
}

.phone-stats b {
  color: #d9f99d;
}

.login-panel {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 34px;
}

.login-card {
  width: min(430px, 100%);
  padding: 38px;
  border: 1px solid rgba(17,17,19,.06);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 28px 80px rgba(31, 70, 43, .16);
  animation: revealUp .55s ease-out both;
}

.login-title span {
  color: #166534;
  font-size: 12px;
  font-weight: 800;
}

.login-title h2 {
  margin: 8px 0 28px;
  color: #1f2937;
  font-size: 30px;
  letter-spacing: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #166534, #22c55e);
  box-shadow: 0 14px 28px rgba(34, 197, 94, .22);
}

.demo-title {
  margin: 25px 0 10px;
  color: #8a8a90;
  font-size: 11px;
}

.demo-list {
  display: grid;
  gap: 8px;
}

.demo-list button {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 12px;
  border: 1px solid #ececef;
  border-radius: 10px;
  background: #fafafa;
  color: #333337;
  cursor: pointer;
  text-align: left;
  transition: transform .18s ease, border-color .18s ease, background .18s ease;
}

.demo-list button:hover {
  transform: translateY(-1px);
  border-color: #b8dfad;
  background: #f8fbf5;
}

.demo-list b {
  font-size: 12px;
}

.demo-list span {
  color: #8a8a90;
  font-size: 10px;
}

.login-panel footer {
  position: absolute;
  bottom: 18px;
  color: #99999f;
  font-size: 10px;
}

@keyframes floatPhone {
  0%, 100% { transform: rotate(5deg) translateY(0); }
  50% { transform: rotate(3deg) translateY(-12px); }
}

@keyframes glint {
  0%, 58% { transform: translateX(-130%); }
  100% { transform: translateX(130%); }
}

@keyframes dotWave {
  0%, 100% { transform: scale(.72); opacity: .16; }
  50% { transform: scale(1.1); opacity: .72; }
}

@keyframes revealUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .showcase {
    min-height: 520px;
  }

  .login-panel {
    min-height: auto;
    padding: 30px 18px 56px;
  }
}

@media (max-width: 620px) {
  .showcase {
    padding: 34px 22px;
  }

  .phone-stack {
    opacity: .5;
    right: -80px;
    transform: scale(.78);
  }

  .showcase-copy h1 {
    font-size: 42px;
  }
}
</style>
