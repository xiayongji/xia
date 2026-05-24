#!/bin/bash
#
# SmartHome 微服务一键启动脚本
# 启动顺序: Eureka → 所有业务服务并行
#

set -e

# ============================================================
# 环境配置
# ============================================================
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

PROJECT_ROOT="/workspace/smart-home-microservices"
LOG_DIR="$PROJECT_ROOT/logs"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

mkdir -p "$LOG_DIR"

echo -e "${CYAN}============================================${NC}"
echo -e "${CYAN}   🏠 SmartHome 微服务系统启动中...${NC}"
echo -e "${CYAN}============================================${NC}"
echo ""

kill_old() {
    local port=$1
    local pid=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        echo -e "  ${YELLOW}⚠ 端口 $port 被占用, 终止 PID=$pid${NC}"
        kill -9 $pid 2>/dev/null || true
        sleep 1
    fi
}

wait_for() {
    local name=$1
    local port=$2
    local max=${3:-60}
    local waited=0
    echo -ne "${YELLOW}  ⏳ 等待 $name (:$port)...${NC}"
    while ! curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; do
        sleep 2
        waited=$((waited + 2))
        if [ $waited -ge $max ]; then
            echo -e " ${RED}超时${NC}"
            return 1
        fi
    done
    echo -e " ${GREEN}✓${NC}"
}

start_service() {
    local name=$1
    local dir=$2
    local port=$3
    
    kill_old "$port"
    echo -e "${BLUE}  🚀 启动 $name (:$port)${NC}"
    nohup mvn spring-boot:run -f "$PROJECT_ROOT/$dir/pom.xml" \
        > "$LOG_DIR/$name.log" 2>&1 &
}

# ============================================================
# 1. Eureka 注册中心
# ============================================================
echo -e "${GREEN}[1/3] 启动 Eureka 注册中心...${NC}"
start_service "eureka-server" "eureka-server" "8761"
wait_for "eureka-server" "8761" 90
echo ""

# ============================================================
# 2. 业务服务并行启动
# ============================================================
echo -e "${GREEN}[2/3] 启动业务服务...${NC}"
start_service "device-service"   "device-service"   "8081"
start_service "user-service"     "user-service"     "8082"
start_service "scene-service"    "scene-service"    "8083"
start_service "edge-service"     "edge-service"     "8084"
start_service "analytics-service" "analytics-service" "8085"
start_service "multimodal-service" "multimodal-service" "8086"

# 网关最后启动
sleep 3
start_service "api-gateway"      "api-gateway"      "8080"
echo ""

# ============================================================
# 3. 状态汇总
# ============================================================
echo -e "${GREEN}[3/3] 服务状态检查...${NC}"
sleep 8

check() {
    local name=$1 port=$2
    if lsof -ti :$port > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ $name (:$port)${NC}"
    else
        echo -e "  ${RED}❌ $name (:$port)${NC}"
    fi
}

check "eureka-server"      8761
check "api-gateway"        8080
check "device-service"     8081
check "user-service"       8082
check "scene-service"      8083
check "edge-service"       8084
check "analytics-service"  8085
check "multimodal-service" 8086

echo ""
echo -e "${CYAN}============================================${NC}"
echo -e "${GREEN}  🎉 智能家居微服务系统已启动!${NC}"
echo -e "${CYAN}============================================${NC}"
echo ""
echo -e "  📡 网关入口:   ${BLUE}http://localhost:8080${NC}"
echo -e "  🔍 注册中心:   ${BLUE}http://localhost:8761${NC}"
echo -e "  📋 日志目录:   ${BLUE}$LOG_DIR${NC}"
echo -e "  🛑 停止命令:   ${YELLOW}bash ~/Desktop/stop-all.sh${NC}"
echo ""