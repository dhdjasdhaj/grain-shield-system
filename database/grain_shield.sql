-- Grain Shield MySQL 8 数据库初始化参考脚本
-- 正常运行时 Spring Data JPA 会自动建表，并由 SeedDataConfig 写入演示数据。
CREATE DATABASE IF NOT EXISTS grain_shield DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE grain_shield;

-- 核心数据表清单：
-- sys_user / user_node / supply_node / inventory / logistics_route / vehicle
-- risk_rule / iot_grain_data / weather_data / traffic_data / risk_alert
-- dispatch_plan / execution_feedback / operation_log / sys_config

-- 如需完全重置演示数据，建议执行：
-- docker compose down -v
-- docker compose up -d --build
