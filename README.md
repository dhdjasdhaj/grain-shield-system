# Grain Shield 粮食供应链网络风险预警与智能调度系统

这是依据课程报告开发的完整前后端信息系统源代码。系统面向武汉市粮食供应链课程原型场景，形成“基础数据维护 → 模拟状态接入 → 风险规则扫描 → 告警生成与处理 → 调度建议 → 人工确认/调整 → 执行反馈与留痕”的完整闭环。

## 1. 技术栈

- 前端：Vue 3、Vite、Element Plus、ECharts、Pinia、Vue Router、Axios
- 后端：Java 21、Spring Boot 3、Spring Security、JWT、Spring Data JPA
- 数据库：MySQL 8；本地开发也可直接使用内置 H2 文件数据库
- 部署：Docker Compose、Nginx
- 接口文档：Swagger / OpenAPI

## 2. 已实现功能

### 全局态势与可视化

- 节点、线路、待处置告警、紧急风险、调度建议、可用车辆等 KPI
- 供应链网络态势图：粮库、加工企业、配送点及运输线路关系展示
- 正常、拥堵、封闭线路分色展示
- 风险类型分布、近 7 日告警趋势、最新告警列表

### 基础数据与系统管理

- 供应链节点增删改查
- 粮食品种与库存台账管理
- 运输线路与通行状态管理
- 运输车辆和载重状态管理
- 用户、角色和用户—节点授权关系
- 系统参数与模拟外部接口配置
- 关键操作日志审计

### 模拟数据接入

- IoT 粮情温湿度记录
- 气象灾害与预警等级记录
- 道路正常、拥堵、封闭状态记录
- 交通状态录入后同步更新对应运输线路状态

### 风险预警

- 配置化风险规则：风险类型、指标、运算符、数值/文本阈值、等级、处置建议
- 库存不足、仓储高温、高湿、道路封闭、红色气象预警自动扫描
- 自动生成告警编号、触发详情、影响节点/线路和处置建议
- 告警确认、忽略、关闭、转入调度分析
- 相同规则与对象存在未关闭告警时避免重复生成

### 应急调度辅助

- 根据告警自动识别目标节点和粮食品种
- 计算目标库存缺口
- 按“可用库存扣除安全库存后的富余量”筛选备选粮库
- 以正常=1、拥堵=3、封闭=不可达为线路权重执行 Dijkstra 路径计算
- 推荐起运节点、目标节点、数量、参考路线、距离、车辆和可解释理由
- 人工确认、调整、驳回方案
- 导出带 UTF-8 BOM 的 CSV 调拨参考单
- 记录出库、运输、到达和异常等执行反馈

### 权限控制

| 演示账号 | 密码 | 权限 |
|---|---|---|
| `admin` | `admin123` | 系统管理员：全部功能 |
| `dispatcher` | `dispatch123` | 市级调度指挥员：态势、告警、调度 |
| `node` | `node123` | 节点管理员：授权节点库存与粮情维护、查看业务信息 |

## 3. 最快启动方式：Docker Compose

电脑需要安装 Docker Desktop。进入项目根目录执行：

```bash
docker compose up -d --build
```

Windows 也可以双击或在命令行运行：

```bat
start.bat
```

启动后访问：

- 系统前端：`http://localhost:8088`
- 后端接口：`http://localhost:8080/api`
- Swagger：`http://localhost:8088/swagger-ui/index.html`
- MySQL：`localhost:3307`，数据库 `grain_shield`，用户 `grain`，密码 `grain123456`

停止系统：

```bash
docker compose down
```

清空数据库并恢复初始演示数据：

```bash
docker compose down -v
docker compose up -d --build
```

## 4. 本地开发运行

### 后端

要求 Java 21、Maven 3.9+。默认使用 H2 文件数据库，不要求预先安装 MySQL。

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动建表并写入武汉市演示节点、库存、线路、车辆、规则、模拟状态和初始告警。H2 控制台为 `http://localhost:8080/h2-console`，JDBC 地址：

```text
jdbc:h2:file:./data/grainshield
```

需要改用本地 MySQL 时，可通过环境变量或编辑 `application-docker.yml` 配置数据库，并激活 `docker` profile。

### 前端

要求 Node.js 20+。

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。Vite 已把 `/api` 代理到 `http://localhost:8080`。

## 5. 项目结构

```text
grain-shield-system/
├─ backend/                       Spring Boot 后端
│  ├─ src/main/java/com/grainshield/
│  │  ├─ config/                  安全、数据初始化、JPA 配置
│  │  ├─ controller/              REST API
│  │  ├─ dto/                     前后端传输对象
│  │  ├─ entity/                  数据实体
│  │  ├─ enums/                   业务枚举
│  │  ├─ repository/              JPA 数据访问
│  │  ├─ security/                JWT 认证
│  │  └─ service/                 风险扫描、调度算法、日志等
│  └─ src/main/resources/         H2/MySQL 配置
├─ frontend/                      Vue 3 前端
│  ├─ src/components/             通用表格、状态标签、网络图
│  ├─ src/views/                  各功能页面
│  ├─ src/stores/                 登录状态
│  └─ src/router/                 路由及角色守卫
├─ database/                      数据库说明脚本
├─ docker-compose.yml             一键部署
├─ SYSTEM_MAPPING.md              报告需求—代码映射
└─ README.md
```

## 6. 核心接口

- `POST /api/auth/login` 登录
- `GET /api/dashboard` 态势首页
- `/api/nodes` 节点管理
- `/api/inventories` 库存管理
- `/api/routes` 线路管理
- `/api/vehicles` 车辆管理
- `/api/rules` 风险规则管理
- `/api/state/iot`、`/weather`、`/traffic` 模拟状态数据
- `POST /api/alerts/scan` 执行风险扫描
- `PUT /api/alerts/{id}/status` 处理告警
- `POST /api/dispatch/generate` 生成调度建议
- `PUT /api/dispatch/{id}/adjust` 人工调整方案
- `GET /api/dispatch/{id}/export` 导出调拨参考单
- `POST /api/dispatch/{id}/feedback` 提交执行反馈

## 7. 课程演示建议流程

1. 使用 `dispatcher` 登录，查看全局态势和初始告警。
2. 切换 `admin`，在“模拟状态数据”录入新的温湿度、气象或交通异常。
3. 返回“风险告警”，点击“执行风险扫描”。
4. 确认新告警并点击“调度分析”。
5. 系统自动筛选库存富余粮库、排除封闭线路、计算参考路径并生成建议。
6. 调度员确认或调整方案，导出 CSV 调拨参考单。
7. 节点管理员或调度员填写出库、运输、到达反馈。
8. 管理员查看操作日志，展示全过程可追溯。

## 8. 设计边界

该系统严格保持课程报告中的原型定位：

- 使用模拟数据，不声称已接入真实 IoT、气象、交通或 GPS 平台。
- 调度建议只作为数据辅助和人工决策参考，不自动形成正式行政命令。
- 网络图和路径计算是轻量化演示，不包含高精度 GIS、实时轨迹和生产级集群能力。
- 后续可以接入真实 API、消息队列、Redis 规则缓存、地图服务和多车辆组合优化。
