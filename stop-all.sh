#!/bin/bash
#
# SmartHome 微服务一键停止脚本
#

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PORTS=(8761 8080 8081 8082 8083 8084 8085 8086 9094)
NAMES=("eureka-server" "api-gateway" "device-service" "user-service" "scene-service" "edge-service" "analytics-service" "multimodal-service" "edge-grpc")

echo -e "${CYAN}============================================${NC}"
echo -e "${CYAN}   🛑 SmartHome 微服务系统停止中...${NC}"
echo -e "${CYAN}============================================${NC}"
echo ""

STOPPED=0
for i in "${!PORTS[@]}"; do
    port="${PORTS[$i]}"
    name="${NAMES[$i]}"
    pid=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        echo -e "  ${YELLOW}停止 $name (:$port) PID=$pid${NC}"
        kill -15 $pid 2>/dev/null || true
        STOPPED=$((STOPPED + 1))
    else
        echo -e "  ${GREEN}  $name (:$port) 未运行${NC}"
    fi
done

sleep 2

# 强制清理残留
for i in "${!PORTS[@]}"; do
    port="${PORTS[$i]}"
    pid=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        echo -e "  ${RED}强制终止 :$port PID=$pid${NC}"
        kill -9 $pid 2>/dev/null || true
    fi
done

echo ""
echo -e "${GREEN}  ✅ 已停止 $STOPPED 个服务${NC}"
echo ""