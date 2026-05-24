#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_step()  { echo -e "\n${BLUE}[STEP]${NC}  $*"; }

stop_and_remove_all() {
    log_step "停止并移除所有旧的智能家居容器..."
    
    # 停止并移除所有 smart-home 相关容器
    CONTAINERS=$(docker ps -a --filter "name=smart-home" --format "{{.Names}}" 2>/dev/null || true)
    if [ -n "$CONTAINERS" ]; then
        echo "$CONTAINERS" | while read -r container; do
            log_info "停止容器: $container"
            docker stop "$container" 2>/dev/null || true
            docker rm "$container" 2>/dev/null || true
        done
    fi

    # 停止并移除边缘服务相关容器
    CONTAINERS=$(docker ps -a --filter "name=edge" --format "{{.Names}}" 2>/dev/null || true)
    if [ -n "$CONTAINERS" ]; then
        echo "$CONTAINERS" | while read -r container; do
            log_info "停止容器: $container"
            docker stop "$container" 2>/dev/null || true
            docker rm "$container" 2>/dev/null || true
        done
    fi

    log_info "容器清理完成"
}

remove_volumes() {
    log_step "清理旧的数据卷..."
    
    VOLUMES=$(docker volume ls --filter "name=smart-home" --format "{{.Name}}" 2>/dev/null || true)
    if [ -n "$VOLUMES" ]; then
        echo "$VOLUMES" | while read -r volume; do
            log_info "删除卷: $volume"
            docker volume rm "$volume" 2>/dev/null || true
        done
    fi

    VOLUMES=$(docker volume ls --filter "name=edge_" --format "{{.Name}}" 2>/dev/null || true)
    if [ -n "$VOLUMES" ]; then
        echo "$VOLUMES" | while read -r volume; do
            log_info "删除卷: $volume"
            docker volume rm "$volume" 2>/dev/null || true
        done
    fi

    log_info "数据卷清理完成"
}

deploy_edge_only() {
    log_step "部署边缘计算服务..."
    
    # 构建边缘服务镜像
    log_info "构建 edge-service 镜像..."
    docker compose -f docker-compose.pi.edge-only.yml --env-file .env.pi.edge-only build

    # 启动服务
    log_info "启动边缘计算服务..."
    docker compose -f docker-compose.pi.edge-only.yml --env-file .env.pi.edge-only up -d

    log_info "等待服务启动..."
    sleep 30

    # 检查状态
    log_info "服务状态:"
    docker compose -f docker-compose.pi.edge-only.yml --env-file .env.pi.edge-only ps

    echo ""
    log_info "边缘计算服务部署完成！"
    echo ""
    echo "访问地址:"
    echo "  REST API:  http://localhost:8084"
    echo "  gRPC:      localhost:9094"
    echo "  Swagger:   http://localhost:8084/swagger-ui.html"
    echo "  Health:    http://localhost:8084/actuator/health"
}

show_help() {
    echo "Usage: $0 COMMAND"
    echo ""
    echo "Commands:"
    echo "  clean      停止并删除所有智能家居容器和数据卷"
    echo "  deploy     部署最小化边缘计算服务（仅 edge-service）"
    echo "  full       执行完整流程：清理 -> 部署"
    echo "  status     查看当前运行状态"
    echo ""
    echo "示例:"
    echo "  $0 clean    # 清理所有旧服务"
    echo "  $0 deploy   # 部署边缘服务"
    echo "  $0 full     # 一键清理并部署"
}

case "${1:-}" in
    clean)
        stop_and_remove_all
        remove_volumes
        log_info "清理完成！"
        ;;
    deploy)
        deploy_edge_only
        ;;
    full)
        stop_and_remove_all
        remove_volumes
        deploy_edge_only
        ;;
    status)
        log_step "当前运行状态"
        docker compose -f docker-compose.pi.edge-only.yml --env-file .env.pi.edge-only ps 2>/dev/null || docker ps
        ;;
    *)
        show_help
        exit 1
        ;;
esac