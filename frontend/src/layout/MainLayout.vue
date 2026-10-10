<template>
  <div class="app-layout">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="brand"><AppLogo :compact="collapsed" /></div>
      <el-scrollbar class="menu-scroll">
        <el-menu :default-active="$route.path" router class="side-menu" :collapse="collapsed">
          <el-menu-item index="/dashboard"><el-icon><DataLine /></el-icon><template #title>全局态势</template></el-menu-item>
          <el-menu-item index="/basic-data"><el-icon><Coin /></el-icon><template #title>基础数据</template></el-menu-item>
          <el-menu-item index="/rules"><el-icon><SetUp /></el-icon><template #title>风险规则</template></el-menu-item>
          <el-menu-item index="/state-data"><el-icon><Connection /></el-icon><template #title>模拟状态数据</template></el-menu-item>
          <el-menu-item index="/alerts"><el-icon><Bell /></el-icon><template #title>风险告警</template></el-menu-item>
          <el-menu-item index="/dispatch"><el-icon><Guide /></el-icon><template #title>应急调度</template></el-menu-item>
          <template v-if="auth.isAdmin">
            <div class="menu-group" v-if="!collapsed">系统管理</div>
            <el-menu-item index="/users"><el-icon><UserFilled /></el-icon><template #title>用户权限</template></el-menu-item>
            <el-menu-item index="/configs"><el-icon><Tools /></el-icon><template #title>系统配置</template></el-menu-item>
            <el-menu-item index="/logs"><el-icon><Document /></el-icon><template #title>操作日志</template></el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <button class="collapse-btn" @click="collapsed=!collapsed"><el-icon><Fold v-if="!collapsed"/><Expand v-else/></el-icon></button>
    </aside>
    <section class="main-area">
      <header class="topbar">
        <div class="top-copy"><div class="crumb">智慧粮食调度平台 / <strong>{{ $route.meta.title }}</strong></div><div class="today">{{ today }}</div></div>
        <div class="top-actions">
          <el-input v-model="keyword" class="top-search" placeholder="搜索仓库、线路、调度任务" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-tag class="region-tag" effect="plain">武汉市</el-tag>
          <el-tooltip content="预警中心"><el-button circle @click="$router.push('/alerts')"><el-icon><Bell /></el-icon></el-button></el-tooltip>
          <el-tooltip content="接口文档"><el-button circle @click="openDocs"><el-icon><Link /></el-icon></el-button></el-tooltip>
          <el-dropdown @command="handleCommand">
            <div class="user-chip"><div class="avatar">{{ auth.user?.realName?.slice(0,1) }}</div><div class="user-meta"><b>{{ auth.user?.realName }}</b><span>{{ roleLabel }}</span></div><el-icon><ArrowDown /></el-icon></div>
            <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </header>
      <main class="content"><router-view /></main>
    </section>
    <AiAssistant />
  </div>
</template>
<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import AppLogo from '../components/AppLogo.vue'
import AiAssistant from '../components/AiAssistant.vue'
import { useAuthStore } from '../stores/auth'
const collapsed=ref(false);const keyword=ref('');const auth=useAuthStore();const router=useRouter()
const today=dayjs().format('YYYY年MM月DD日 dddd').replace('Monday','星期一').replace('Tuesday','星期二').replace('Wednesday','星期三').replace('Thursday','星期四').replace('Friday','星期五').replace('Saturday','星期六').replace('Sunday','星期日')
const roleLabel=computed(()=>({ADMIN:'系统管理员',DISPATCHER:'市级调度指挥员',NODE_MANAGER:'节点管理员'})[auth.user?.role]||'用户')
function handleCommand(command){if(command==='logout'){auth.logout();router.replace('/login')}}
function openDocs(){window.open('/swagger-ui/index.html','_blank')}
</script>
<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  overflow: hidden;
  background: #f7faf5;
}

.sidebar {
  width: 290px;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 20;
  border-right: 1px solid #e4e7ec;
  background: #fff;
  transition: width .25s ease;
}

.sidebar.collapsed {
  width: 78px;
}

.brand {
  height: 76px;
  padding: 17px 20px;
  border-bottom: 1px solid #e4e7ec;
}

.menu-scroll {
  flex: 1;
}

.side-menu {
  border: 0;
  background: transparent;
  padding: 16px 20px;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #344054;
  --el-menu-hover-bg-color: #f2f7ef;
  --el-menu-active-color: #166534;
}

.side-menu:not(.el-menu--collapse) {
  width: 250px;
}

.side-menu :deep(.el-menu-item) {
  height: 40px;
  margin: 6px 0;
  border-radius: 8px;
  font-weight: 700;
  transition: transform .2s ease, background .2s ease, color .2s ease;
}

.side-menu :deep(.el-menu-item:hover) {
  transform: translateX(3px);
}

.side-menu :deep(.el-menu-item.is-active) {
  position: relative;
  background: #eef7ed;
  color: #166534;
  box-shadow: none;
}

.side-menu :deep(.el-menu-item.is-active::before) {
  content: "";
  position: absolute;
  left: 9px;
  top: 50%;
  width: 4px;
  height: 18px;
  border-radius: 999px;
  background: #22c55e;
  transform: translateY(-50%);
}

.menu-group {
  padding: 22px 14px 8px;
  color: #98a2b3;
  font-size: 11px;
  letter-spacing: .14em;
}

.collapse-btn {
  height: 50px;
  border: 0;
  border-top: 1px solid #e4e7ec;
  background: transparent;
  color: #667085;
  cursor: pointer;
  font-size: 18px;
}

.collapse-btn:hover {
  background: #f8fbf6;
}

.main-area {
  min-width: 0;
  min-height: 100vh;
  flex: 1;
  transition: margin-left .25s ease;
  background: #f7faf5;
}

.collapsed + .main-area {
  margin-left: 0;
}

.topbar {
  height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 0 28px;
  border-bottom: 1px solid #e4e7ec;
  background: #fff;
}

.crumb {
  color: #64748b;
  font-size: 14px;
}

.crumb strong {
  color: #1f2937;
}

.today {
  margin-top: 5px;
  color: #98a2b3;
  font-size: 11px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.top-search {
  width: min(34vw, 430px);
}

.region-tag {
  height: 32px;
  border-color: #b9dec0;
  background: #eef7ed;
  color: #166534;
  font-weight: 700;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 9px 5px 5px;
  border: 1px solid #e4e7ec;
  border-radius: 999px;
  background: #fff;
  cursor: pointer;
}

.user-chip:hover {
  border-color: #b9dec0;
  background: #f8fbf6;
}

.avatar {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #166534, #22c55e);
  color: white;
  font-weight: 800;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-meta b {
  color: #1f2937;
  font-size: 13px;
}

.user-meta span {
  margin-top: 2px;
  color: #667085;
  font-size: 10px;
}

.content {
  min-height: calc(100vh - 76px);
}

@media (max-width: 760px) {
  .app-layout {
    min-height: 100vh;
    margin: 0;
    border-radius: 0;
  }

  .sidebar {
    width: 78px;
    flex: 0 0 78px;
    overflow: hidden;
  }

  .main-area {
    margin-left: 0;
  }

  .brand {
    display: flex;
    justify-content: center;
    padding: 18px 17px;
  }

  .brand :deep(.logo-copy) {
    display: none;
  }

  .side-menu,
  .side-menu:not(.el-menu--collapse) {
    width: 78px;
    padding: 12px 6px;
  }

  .side-menu :deep(.el-menu-item) {
    width: 52px;
    justify-content: center;
    margin: 8px auto;
    padding: 0 !important;
  }

  .side-menu :deep(.el-menu-item .el-icon) {
    margin-right: 0;
  }

  .side-menu :deep(.el-menu-item span) {
    display: none;
  }

  .side-menu :deep(.el-menu-item.is-active::before) {
    left: 3px;
  }

  .topbar {
    height: 68px;
    justify-content: flex-end;
    padding: 0 14px;
  }

  .top-actions {
    width: 100%;
    justify-content: flex-end;
    gap: 8px;
  }

  .user-chip {
    padding: 4px;
  }

  .user-chip > .el-icon {
    display: none;
  }

  .top-copy,
  .top-search,
  .region-tag {
    display: none;
  }

  .user-meta {
    display: none;
  }
}
</style>
