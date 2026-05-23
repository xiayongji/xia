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

command_exists() { command -v "$1" &>/dev/null; }

check_prerequisites() {
  log_step "Checking prerequisites..."

  if ! command_exists docker; then
    log_error "Docker is not installed. Please install Docker first."
    exit 1
  fi
  log_info "Docker $(docker --version | awk '{print $3}' | tr -d ',')"

  if ! docker compose version &>/dev/null; then
    log_error "Docker Compose V2 is required."
    exit 1
  fi
  log_info "Docker Compose $(docker compose version --short)"

  if [ ! -f .env ]; then
    log_error ".env file not found. Run 'cp .env.example .env' and configure it first."
    exit 1
  fi
  log_info ".env file found"
}

start_infra() {
  log_step "Starting infrastructure services (MySQL, Redis, RabbitMQ)..."
  docker compose up -d mysql redis rabbitmq
  log_info "Infrastructure services started. Waiting for health checks..."
  sleep 5

  local wait_time=0
  local max_wait=120
  while [ $wait_time -lt $max_wait ]; do
    local mysql_healthy=$(docker inspect --format='{{.State.Health.Status}}' smart-home-mysql 2>/dev/null || echo "unknown")
    local redis_healthy=$(docker inspect --format='{{.State.Health.Status}}' smart-home-redis 2>/dev/null || echo "unknown")
    local rabbitmq_healthy=$(docker inspect --format='{{.State.Health.Status}}' smart-home-rabbitmq 2>/dev/null || echo "unknown")

    if [ "$mysql_healthy" = "healthy" ] && [ "$redis_healthy" = "healthy" ] && [ "$rabbitmq_healthy" = "healthy" ]; then
      log_info "All infrastructure services are healthy!"
      return 0
    fi

    sleep 5
    wait_time=$((wait_time + 5))
    echo -n "."
  done

  log_error "Infrastructure services failed to become healthy within ${max_wait}s"
  exit 1
}

build_services() {
  log_step "Building microservice images..."
  local services=(
    "eureka-server"
    "api-gateway"
    "device-service"
    "scene-service"
    "edge-service"
    "analytics-service"
  )

  for svc in "${services[@]}"; do
    log_info "Building $svc..."
    docker compose build "$svc" 2>&1 | tail -1
  done
  log_info "All service images built successfully"
}

start_services() {
  log_step "Starting microservices..."
  docker compose up -d eureka-server api-gateway device-service scene-service edge-service analytics-service
  log_info "Microservices started"
}

start_monitoring() {
  log_step "Starting monitoring stack (Prometheus, Grafana)..."
  docker compose up -d prometheus grafana
  log_info "Monitoring stack started"
}

start_frontend() {
  log_step "Starting frontend..."
  docker compose up -d frontend
  log_info "Frontend started"
}

start_all() {
  log_step "Starting all services..."
  docker compose up -d

  log_info "Waiting for all services to be healthy..."
  sleep 10

  local wait_time=0
  local max_wait=300
  while [ $wait_time -lt $max_wait ]; do
    local unhealthy=$(docker compose ps --format json | grep -v '"Health":"healthy"' | grep -v '"Health":""' | wc -l || echo "0")
    if [ "$unhealthy" -eq 0 ]; then
      break
    fi
    sleep 10
    wait_time=$((wait_time + 10))
    echo -n "."
  done
  echo ""
}

stop_all() {
  log_step "Stopping all services..."
  docker compose down
  log_info "All services stopped"
}

stop_services() {
  log_step "Stopping microservices only (keeping infrastructure)..."
  docker compose stop eureka-server api-gateway device-service scene-service edge-service analytics-service frontend
  log_info "Microservices stopped"
}

restart_service() {
  local svc="$1"
  if [ -z "$svc" ]; then
    log_error "Usage: $0 restart SERVICE_NAME"
    exit 1
  fi
  log_step "Restarting $svc..."
  docker compose restart "$svc"
  log_info "$svc restarted"
}

show_status() {
  log_step "Service Status"
  echo ""
  docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
  echo ""
  log_step "Resource Usage"
  docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" \
    $(docker compose ps -q)
}

show_logs() {
  local svc="${1:-}"
  if [ -n "$svc" ]; then
    docker compose logs -f --tail=100 "$svc"
  else
    docker compose logs -f --tail=100
  fi
}

clean_all() {
  log_warn "This will remove all containers, volumes, and networks!"
  read -rp "Are you sure? [y/N] " confirm
  if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    log_step "Cleaning everything..."
    docker compose down -v --remove-orphans
    log_info "Cleanup completed"
  else
    log_info "Cancelled"
  fi
}

show_urls() {
  echo ""
  echo "=============================================="
  echo "  Smart Home Microservices - Access URLs"
  echo "=============================================="

  source .env 2>/dev/null
  local HOST="${HOST:-localhost}"

  echo ""
  echo "  Frontend:       http://${HOST}:${FRONTEND_PORT:-3001}"
  echo "  API Gateway:    http://${HOST}:${API_GATEWAY_PORT:-8080}"
  echo "  Eureka:         http://${HOST}:${EUREKA_PORT:-8761}"
  echo "  RabbitMQ Mgmt:  http://${HOST}:${RABBITMQ_MANAGEMENT_PORT:-15672}"
  echo "  Prometheus:     http://${HOST}:${PROMETHEUS_PORT:-9090}"
  echo "  Grafana:        http://${HOST}:${GRAFANA_PORT:-3000}"
  echo ""
  echo "=============================================="
}

usage() {
  echo "Usage: $0 COMMAND [OPTIONS]"
  echo ""
  echo "Commands:"
  echo "  up          Start all services (infrastructure + microservices + monitoring + frontend)"
  echo "  down        Stop all services"
  echo "  build       Build all microservice Docker images"
  echo "  start       Start microservices only (infrastructure must be running)"
  echo "  stop        Stop microservices (keep infrastructure running)"
  echo "  infra       Start infrastructure services only (MySQL, Redis, RabbitMQ)"
  echo "  monitor     Start monitoring stack (Prometheus, Grafana)"
  echo "  restart SVC Restart a specific service"
  echo "  status      Show service status and resource usage"
  echo "  logs [SVC]  Show logs (all or specific service)"
  echo "  urls        Show access URLs"
  echo "  clean       Remove all containers, volumes, and networks"
  echo ""
  echo "Examples:"
  echo "  $0 infra              # Start infrastructure"
  echo "  $0 build              # Build all service images"
  echo "  $0 up                 # Start everything"
  echo "  $0 logs device-service # View device-service logs"
  echo "  $0 restart api-gateway # Restart API gateway"
}

case "${1:-}" in
  up)
    check_prerequisites
    start_all
    show_status
    show_urls
    ;;
  down)
    stop_all
    ;;
  build)
    check_prerequisites
    build_services
    ;;
  start)
    check_prerequisites
    start_services
    show_status
    ;;
  stop)
    stop_services
    ;;
  infra)
    check_prerequisites
    start_infra
    show_status
    ;;
  monitor)
    check_prerequisites
    start_monitoring
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
  clean)
    clean_all
    ;;
  *)
    usage
    exit 1
    ;;
esac