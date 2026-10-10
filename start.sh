#!/usr/bin/env bash
set -e
docker compose up -d --build
echo "Grain Shield 已启动：http://localhost:8088"
echo "Swagger 接口文档：http://localhost:8088/swagger-ui/index.html"
