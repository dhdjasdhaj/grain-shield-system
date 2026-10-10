# 报告需求与代码实现映射

| 报告子系统/业务过程 | 实现页面 | 后端核心接口/服务 |
|---|---|---|
| S1 基础数据与系统管理 | 基础数据、用户权限、系统配置、操作日志 | `/api/nodes`、`/api/inventories`、`/api/routes`、`/api/vehicles`、`/api/users`、`/api/configs`、`/api/logs` |
| 模拟状态数据接入 | 模拟状态数据 | `/api/state/iot`、`/api/state/weather`、`/api/state/traffic` |
| S2 风险预警 | 风险规则、风险告警、全局态势 | `RiskScanService`、`/api/rules`、`/api/alerts/scan`、`/api/alerts/{id}/status` |
| 仓储/库存风险识别 | 风险告警 | 库存阈值、温度、湿度规则扫描 |
| 交通/气象风险识别 | 风险告警 | 道路状态、气象预警文本规则扫描 |
| S3 影响评估与调度建议 | 应急调度 | `DispatchService.generate`、Dijkstra 可行路径计算、富余库存筛选 |
| 人工确认与调整 | 应急调度 | 方案确认、调整、驳回、CSV 调拨参考单导出 |
| 执行反馈与结果留痕 | 应急调度、操作日志 | `/api/dispatch/{id}/feedback`、`execution_feedback`、`operation_log` |
| GIS/网络态势展示 | 全局态势 | ECharts 供应链网络图，节点/线路按状态着色 |
| 三类角色权限 | 全系统 | JWT、Spring Security、ADMIN / DISPATCHER / NODE_MANAGER |

## 实现边界

系统严格按课程原型定位使用模拟业务数据。调度方案是辅助决策记录，不代表正式行政命令；真实 IoT、气象、交通和 GPS 接口保留在系统配置与扩展点中，未伪装为真实接入。
