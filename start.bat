@echo off
docker compose up -d --build
if errorlevel 1 exit /b 1
echo Grain Shield 已启动: http://localhost:8088
echo Swagger 接口文档: http://localhost:8088/swagger-ui/index.html
pause
