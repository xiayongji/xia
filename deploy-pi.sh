#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_step()  { echo -e "\n${BLUE}[STEP]${NC}  $*"; }

ARCH=$(uname -m)
COMPOSE_FILE="docker-compose.pi.yml"
ENV_FILE=".env.pi"

check_raspberry_pi() {
    log_step "Checking Raspberry Pi environment..."

    if [ "$ARCH" != "aarch64" ] && [ "$ARCH" != "armv7l" ]; then
        log_error "This script is designed for Raspberry Pi (ARM64/ARM32)."
        log_error "Detected architecture: $ARCH"
        log_error "For x86_64, use: ./deploy.sh"
        exit 1
    fi
    log_info "Architecture: $ARCH (Raspberry Pi compatible)"

    local model=$(cat /proc/device-tree/model 2>/dev/null || echo "Unknown Pi model")
    log_info "Device: $model"

    local total_mem=$(free -h | awk '/^Mem:/ {print $2}')
    local available_mem=$(free -h | awk '/^Mem:/ {print $7}')
    log_info "Memory: Total=$total_mem, Available=$available_mem"

    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "$COMPOSE_FILE not found!"
        exit 1
    fi

    if [ ! -f "$ENV_FILE" ]; then
        log_error "$ENV_FILE not found! Copy from .env.pi template."
        exit 1
    fi

    local required_cmds=(docker java mvn git curl)
    for cmd in "${required_cmds[@]}"; do
        if ! command -v "$cmd" >/dev/null 2>&1; then
            log_error "$cmd is not installed. Run install_dependencies first."
            exit 1
        fi
    done
    log_info "All required tools are available"
}

install_dependencies() {
    log_step "Installing system dependencies..."
    echo ""

    log_info "Updating package lists..."
    sudo apt update

    log_info "Installing Java 17..."
    sudo apt install -y openjdk-17-jdk
    java -version

    log_info "Installing Maven..."
    sudo apt install -y maven
    mvn --version

    log_info "Installing Git..."
    sudo apt install -y git

    log_info "Installing curl..."
    sudo apt install -y curl

    if ! command -v docker >/dev/null 2>&1; then
        log_info "Installing Docker..."
        curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
        sudo sh /tmp/get-docker.sh
        sudo usermod -aG docker "$USER"
        log_warn "Docker installed. You may need to log out and back in for group changes to take effect."
    else
        log_info "Docker already installed: $(docker --version)"
    fi

    log_info "Dependencies installation complete!"
    echo ""
    log_warn "IMPORTANT: Log out and log back in for Docker group changes to take effect."
}

check_swap() {
    log_step "Checking swap configuration..."
    local swap_total=$(free -m | awk '/^Swap:/ {print $2}')

    if [ "$swap_total" -lt 1024 ]; then
        log_warn "Swap is less than 1GB ($swap_total MB)."
        log_warn "Building Java projects requires significant memory."
        log_warn "Consider increasing swap to 2GB:"
        echo ""
        echo "  sudo dphys-swapfile swapoff"
        echo "  sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile"
        echo "  sudo dphys-swapfile setup"
        echo "  sudo dphys-swapfile swapon"
        echo ""
    else
        log_info "Swap: ${swap_total}MB (sufficient for builds)"
    fi
}

check_disk() {
    log_step "Checking disk space..."
    local available=$(df -h / | awk 'NR==2 {print $4}')
    local available_kb=$(df / | awk 'NR==2 {print $4}')
    log_info "Available disk space: $available"

    if [ "$available_kb" -lt 5242880 ]; then
        log_warn "Less than 5GB available. Docker images and builds need space."
    fi
}

build_local() {
    log_step "Building all microservices locally (this will take 15-30 minutes)..."
    echo ""

    local services=(
        "eureka-server"
        "api-gateway"
        "device-service"
        "scene-service"
        "edge-service"
        "analytics-service"
        "multimodal-service"
    )

    local total=${#services[@]}
    local count=0

    for svc in "${services[@]}"; do
        count=$((count + 1))
        log_info "[$count/$total] Building $svc..."

        local svc_dir="smart-home-microservices/$svc"
        if [ ! -f "$svc_dir/pom.xml" ]; then
            log_warn "  $svc_dir not found, skipping..."
            continue
        fi

        (
            cd "$svc_dir"
            mvn package -DskipTests -q 2>&1 | tail -3
        ) || {
            log_error "Failed to build $svc"
            exit 1
        }

        log_info "  $svc: OK"
    done

    echo ""
    log_info "All services built successfully!"
}

start_infra() {
    log_step "Starting infrastructure (MariaDB, Redis, RabbitMQ)..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d mariadb redis rabbitmq

    log_info "Waiting for infrastructure to become healthy..."
    local wait_time=0
    local max_wait=180
    while [ $wait_time -lt $max_wait ]; do
        local mdb_health=$(docker inspect --format='{{.State.Health.Status}}' smart-home-mariadb 2>/dev/null || echo "starting")
        local rds_health=$(docker inspect --format='{{.State.Health.Status}}' smart-home-redis 2>/dev/null || echo "starting")
        local rmq_health=$(docker inspect --format='{{.State.Health.Status}}' smart-home-rabbitmq 2>/dev/null || echo "starting")

        echo -ne "\r  MariaDB: $mdb_health  Redis: $rds_health  RabbitMQ: $rmq_health  (${wait_time}s)  "

        if [ "$mdb_health" = "healthy" ] && [ "$rds_health" = "healthy" ] && [ "$rmq_health" = "healthy" ]; then
            echo ""
            log_info "All infrastructure healthy!"
            return 0
        fi

        sleep 10
        wait_time=$((wait_time + 10))
    done

    echo ""
    log_error "Infrastructure failed to become healthy within ${max_wait}s"
    exit 1
}

start_core_services() {
    log_step "Starting core microservices..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d \
        eureka-server api-gateway device-service scene-service analytics-service

    log_info "Core services started. Waiting for stabilization..."
    sleep 20

    log_step "Checking service status..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
}

start_full() {
    log_step "Starting full stack (including edge and multimodal)..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile full up -d \
        eureka-server api-gateway device-service scene-service \
        edge-service analytics-service multimodal-service

    log_info "Full stack started."
}

start_monitoring() {
    log_step "Starting monitoring stack (Prometheus, Grafana)..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile monitor up -d prometheus grafana
    log_info "Monitoring started."
}

start_frontend() {
    log_step "Starting frontend..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d frontend
    log_info "Frontend started."
}

stop_all() {
    log_step "Stopping all services..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile full --profile monitor down
    log_info "All services stopped."
}

stop_services() {
    log_step "Stopping microservices (keeping infrastructure)..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile full stop \
        eureka-server api-gateway device-service scene-service \
        edge-service analytics-service multimodal-service frontend
    log_info "Microservices stopped. Infrastructure still running."
}

restart_service() {
    local svc="$1"
    if [ -z "$svc" ]; then
        log_error "Usage: $0 restart SERVICE_NAME"
        exit 1
    fi
    log_step "Restarting $svc..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" restart "$svc"
    log_info "$svc restarted."
}

show_status() {
    log_step "=== Service Status ==="
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

    echo ""
    log_step "=== Resource Usage ==="
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" \
        $(docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps -q) 2>/dev/null

    echo ""
    log_step "=== System Memory ==="
    free -h
}

show_logs() {
    local svc="${1:-}"
    if [ -n "$svc" ]; then
        docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs -f --tail=100 "$svc"
    else
        docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs -f --tail=50
    fi
}

show_urls() {
    local ip=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "raspberrypi.local")
    echo ""
    echo "=============================================="
    echo "  Smart Home - Raspberry Pi Access URLs"
    echo "=============================================="
    echo ""
    echo "  Frontend:       http://${ip}:${FRONTEND_PORT:-80}"
    echo "  API Gateway:    http://${ip}:${API_GATEWAY_PORT:-8080}"
    echo "  Eureka:         http://${ip}:${EUREKA_PORT:-8761}"
    echo "  RabbitMQ Mgmt:  http://${ip}:${RABBITMQ_MANAGEMENT_PORT:-15672}"
    echo ""
    source "$ENV_FILE" 2>/dev/null
    echo "  Default Credentials:"
    echo "    Grafana:   admin / ${GRAFANA_ADMIN_PASSWORD:-admin}"
    echo "    RabbitMQ:  ${RABBITMQ_DEFAULT_USER:-admin} / ${RABBITMQ_DEFAULT_PASS:-password}"
    echo "    MariaDB:   ${MYSQL_USER:-admin} / ${MYSQL_PASSWORD:-password}"
    echo ""
    echo "=============================================="
}

clean_all() {
    log_warn "This will delete ALL containers, volumes, and data!"
    read -rp "Are you sure? Type 'yes' to confirm: " confirm
    if [ "$confirm" = "yes" ]; then
        log_step "Stopping and removing everything..."
        docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile full --profile monitor down -v --remove-orphans
        log_info "Cleanup complete."
    else
        log_info "Cancelled."
    fi
}

prune_system() {
    log_step "Pruning Docker system (unused images, containers, networks)..."
    docker system prune -f --filter "until=72h"
    log_info "Docker system pruned."
}

quick_start() {
    check_raspberry_pi
    check_swap
    check_disk
    build_local
    start_infra
    start_core_services
    start_frontend
    show_status
    show_urls
}

usage() {
    echo ""
    echo "Smart Home - Raspberry Pi Deployment Script"
    echo "==========================================="
    echo ""
    echo "Usage: $0 COMMAND [OPTIONS]"
    echo ""
    echo "Setup Commands:"
    echo "  install        Install all system dependencies (Java, Maven, Docker, etc.)"
    echo "  check          Check Pi environment, swap, and disk space"
    echo ""
    echo "Deploy Commands:"
    echo "  quick          Full deployment (check + build + infra + core + frontend)"
    echo "  build          Build all microservices locally (~15-30 min first time)"
    echo "  infra          Start infrastructure only (MariaDB, Redis, RabbitMQ)"
    echo "  core           Start core microservices (Eureka, Gateway, Device, Scene, Analytics)"
    echo "  full           Start all services including Edge, Multimodal"
    echo "  frontend       Start frontend Nginx"
    echo "  monitor        Start Prometheus and Grafana monitoring"
    echo "  start          Alias for 'core'"
    echo ""
    echo "Management Commands:"
    echo "  stop           Stop microservices (keep infrastructure)"
    echo "  down           Stop everything"
    echo "  restart SVC    Restart a specific service"
    echo "  status         Show service health and resource usage"
    echo "  logs [SVC]     View logs (all or specific service)"
    echo "  urls           Display all access URLs"
    echo "  prune          Clean up unused Docker images and containers"
    echo "  clean          Delete ALL containers, volumes, and data"
    echo ""
    echo "Examples:"
    echo "  $0 install           # First time: install dependencies"
    echo "  $0 check             # Verify Pi is ready"
    echo "  $0 quick             # Full deployment"
    echo "  $0 infra             # Start databases only"
    echo "  $0 build             # Rebuild after code changes"
    echo "  $0 logs edge-service # Watch edge service logs"
    echo ""
}

case "${1:-}" in
    install)
        install_dependencies
        ;;
    check)
        check_raspberry_pi
        check_swap
        check_disk
        log_info "Raspberry Pi environment check passed."
        ;;
    quick)
        quick_start
        ;;
    build)
        check_raspberry_pi
        check_swap
        build_local
        ;;
    infra)
        check_raspberry_pi
        start_infra
        ;;
    core|start)
        check_raspberry_pi
        start_core_services
        ;;
    full)
        check_raspberry_pi
        start_full
        ;;
    frontend)
        check_raspberry_pi
        start_frontend
        ;;
    monitor)
        check_raspberry_pi
        start_monitoring
        ;;
    stop)
        stop_services
        ;;
    down)
        stop_all
        ;;
    restart)
        restart_service "$2"
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "$2"
        ;;
    urls)
        show_urls
        ;;
    prune)
        prune_system
        ;;
    clean)
        clean_all
        ;;
    *)
        usage
        exit 1
        ;;
esac