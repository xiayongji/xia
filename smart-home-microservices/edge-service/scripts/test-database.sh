# 边缘服务数据库连接测试脚本

# 数据库连接配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="smarthome_edge"
DB_USER="root"
DB_PASSWORD="infini_rag_flow"

echo "==================================="
echo "智能家居边缘网关数据库测试脚本"
echo "==================================="
echo ""

# 1. 测试数据库连接
echo "[1/7] 测试数据库连接..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -e "SELECT '数据库连接成功！' AS message;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ 数据库连接成功"
else
    echo "✗ 数据库连接失败"
    exit 1
fi
echo ""

# 2. 检查数据库是否存在
echo "[2/7] 检查数据库..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ 数据库 $DB_NAME 存在"
else
    echo "✗ 数据库 $DB_NAME 不存在，正在创建..."
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" < sql/init-edge-database.sql
    echo "✓ 数据库创建完成"
fi
echo ""

# 3. 检查设备表
echo "[3/7] 检查设备表..."
DEVICE_COUNT=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM edge_device;" 2>/dev/null)

if [ $? -eq 0 ]; then
    echo "✓ 设备表查询成功，当前设备数量: $DEVICE_COUNT"
else
    echo "✗ 设备表查询失败"
fi
echo ""

# 4. 检查设备状态表
echo "[4/7] 检查设备状态表..."
STATUS_COUNT=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM edge_device_status;" 2>/dev/null)

if [ $? -eq 0 ]; then
    echo "✓ 设备状态表查询成功，当前状态记录数量: $STATUS_COUNT"
else
    echo "✗ 设备状态表查询失败"
fi
echo ""

# 5. 查看所有设备状态
echo "[5/7] 查看所有设备状态..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e "SELECT device_id, status, device_type, power, temperature, humidity, last_update_time FROM edge_device_status;" 2>/dev/null
echo ""

# 6. 查看在线设备统计
echo "[6/7] 在线设备统计..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e "SELECT status, COUNT(*) as count FROM edge_device_status GROUP BY status;" 2>/dev/null
echo ""

# 7. 测试边缘服务API
echo "[7/7] 测试边缘服务API..."

echo "测试健康检查..."
curl -s http://localhost:8084/api/edge/health | python3 -m json.tool 2>/dev/null || echo "API服务未启动或响应格式错误"

echo ""
echo "测试获取所有设备..."
curl -s http://localhost:8084/api/edge/devices | python3 -m json.tool 2>/dev/null || echo "API服务未启动或响应格式错误"

echo ""
echo "测试获取所有设备状态..."
curl -s http://localhost:8084/api/edge/devices/status/all | python3 -m json.tool 2>/dev/null || echo "API服务未启动或响应格式错误"

echo ""
echo "测试获取设备统计..."
curl -s http://localhost:8084/api/edge/devices/statistics | python3 -m json.tool 2>/dev/null || echo "API服务未启动或响应格式错误"

echo ""
echo "==================================="
echo "测试完成！"
echo "==================================="
