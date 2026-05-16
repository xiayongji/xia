#!/bin/bash
echo "========== 启动智能家居微服务系统 =========="

echo "1. 启动Eureka注册中心..."
cd eureka-server
mvn spring-boot:run &
EUREKA_PID=$!
sleep 15

echo "2. 启动设备管理服务..."
cd ../device-service
mvn spring-boot:run &
DEVICE_PID=$!
sleep 10

echo "3. 启动场景控制服务..."
cd ../scene-service
mvn spring-boot:run &
SCENE_PID=$!
sleep 10

echo "4. 启动用户服务..."
cd ../user-service
mvn spring-boot:run &
USER_PID=$!
sleep 10

echo "5. 启动数据分析服务..."
cd ../analytics-service
mvn spring-boot:run &
ANALYTICS_PID=$!
sleep 10

echo "6. 启动API网关..."
cd ../api-gateway
mvn spring-boot:run &
GATEWAY_PID=$!

echo ""
echo "所有服务已启动！"
echo "Eureka注册中心: http://localhost:8761"
echo "API网关: http://localhost:8080"
echo "设备管理服务: http://localhost:8081"
echo "场景控制服务: http://localhost:8082"
echo "用户服务: http://localhost:8083"
echo "数据分析服务: http://localhost:8084"

echo ""
echo "服务PID列表:"
echo "Eureka: $EUREKA_PID"
echo "Device Service: $DEVICE_PID"
echo "Scene Service: $SCENE_PID"
echo "User Service: $USER_PID"
echo "Analytics Service: $ANALYTICS_PID"
echo "API Gateway: $GATEWAY_PID"

read -p "按Enter键停止所有服务..."

kill $EUREKA_PID $DEVICE_PID $SCENE_PID $USER_PID $ANALYTICS_PID $GATEWAY_PID
echo "所有服务已停止"