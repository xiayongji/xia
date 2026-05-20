# 边缘服务API测试脚本

BASE_URL="http://localhost:8084/api/edge"

echo "==================================="
echo "智能家居边缘服务API测试"
echo "==================================="
echo ""

# 1. 健康检查
echo "[1/12] 健康检查..."
curl -s -X GET "$BASE_URL/health" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/health"
echo ""

# 2. 获取所有设备
echo "[2/12] 获取所有设备..."
curl -s -X GET "$BASE_URL/devices" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices"
echo ""

# 3. 获取所有设备状态
echo "[3/12] 获取所有设备状态..."
curl -s -X GET "$BASE_URL/devices/status/all" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/status/all"
echo ""

# 4. 获取设备统计
echo "[4/12] 获取设备统计..."
curl -s -X GET "$BASE_URL/devices/statistics" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/statistics"
echo ""

# 5. 获取性能指标
echo "[5/12] 获取性能指标..."
curl -s -X GET "$BASE_URL/metrics/latency" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/metrics/latency"
echo ""

# 6. 获取特定设备状态
echo "[6/12] 获取特定设备状态 (device-001)..."
curl -s -X GET "$BASE_URL/devices/device-001/status" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/device-001/status"
echo ""

# 7. 获取最新设备状态
echo "[7/12] 获取最新设备状态 (device-001)..."
curl -s -X GET "$BASE_URL/devices/device-001/status/latest" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/device-001/status/latest"
echo ""

# 8. 获取设备ID列表
echo "[8/12] 获取设备ID列表..."
curl -s -X GET "$BASE_URL/devices/ids" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/ids"
echo ""

# 9. 获取特定类型的设备
echo "[9/12] 获取特定类型的设备 (sensor)..."
curl -s -X GET "$BASE_URL/devices/type/sensor" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/type/sensor"
echo ""

# 10. 获取特定协议的设备
echo "[10/12] 获取特定协议的设备 (WiFi)..."
curl -s -X GET "$BASE_URL/devices/protocol/WiFi" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/devices/protocol/WiFi"
echo ""

# 11. 设备控制测试
echo "[11/12] 设备控制测试 (device-001, turn_on)..."
curl -s -X POST "$BASE_URL/devices/device-001/command" \
  -H "Content-Type: application/json" \
  -d '{"command":"turn_on"}' | python3 -m json.tool 2>/dev/null || curl -s -X POST "$BASE_URL/devices/device-001/command" -H "Content-Type: application/json" -d '{"command":"turn_on"}'
echo ""

# 12. 云端同步测试
echo "[12/12] 云端同步测试..."
curl -s -X POST "$BASE_URL/sync/cloud" | python3 -m json.tool 2>/dev/null || curl -s -X POST "$BASE_URL/sync/cloud"
echo ""

echo "==================================="
echo "API测试完成！"
echo "==================================="
