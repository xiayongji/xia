# 设备状态显示offline问题解决方案

## 问题现象

前端Dashboard显示所有设备状态都是"offline"：

```
loadDevices - device-1779073453825-9716: stored=offline, backend=offline, final=offline
loadDevices - device-1779079690283-0983: stored=offline, backend=offline, final=offline
```

## 问题原因

### 1. 数据库中没有这些设备记录

边缘服务的MySQL数据库（`smarthome_edge`）中没有这些设备的信息：

- `edge_device` 表中没有设备记录
- `edge_device_status` 表中没有设备状态记录

### 2. 前端localStorage存储的是离线状态

前端将设备状态存储在浏览器的localStorage中：
- 如果设备从未成功注册到数据库
- 或者数据库中的状态本身就是"offline"
- localStorage会一直显示"offline"

### 3. 后端API返回的状态就是offline

边缘服务的API从数据库查询设备状态：
- 数据库中没有记录 → 返回null或默认值"offline"
- 数据库中有记录但状态为offline → 返回"offline"

## 解决方案

### 方案一：手动插入设备数据（推荐）

#### 步骤1：确保MySQL服务运行

```bash
# 检查MySQL服务状态
sudo systemctl status mysql

# 如果没有运行，启动它
sudo systemctl start mysql
```

#### 步骤2：执行SQL脚本插入设备数据

**Windows系统：**

```bash
cd c:\Users\user\Desktop\毕业设计\smart-home-microservices\edge-service
scripts\insert-test-devices.bat
```

**Linux/树莓派系统：**

```bash
cd ~/smarthome/edge-service
chmod +x scripts/insert-test-devices.sh
./scripts/insert-test-devices.sh
```

**或者直接在MySQL中执行SQL：**

```bash
mysql -u root -p infini_rag_flow -D smarthome_edge < sql/insert-all-test-devices.sql
```

#### 步骤3：验证数据插入成功

```bash
# 查询数据库中的设备
mysql -u root -p infini_rag_flow -D smarthome_edge -e "SELECT * FROM edge_device;"

# 查询设备状态
mysql -u root -p infini_rag_flow -D smarthome_edge -e "SELECT * FROM edge_device_status;"
```

#### 步骤4：重启边缘服务

```bash
sudo systemctl restart edge-service
```

#### 步骤5：刷新前端页面

在浏览器中刷新Dashboard页面，查看设备状态是否变为"online"。

---

### 方案二：通过API注册设备

如果方案一不起作用，可以尝试通过API注册设备：

```bash
# 注册设备1
curl -X POST http://localhost:8084/api/edge/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "device-1779073453825-9716",
    "deviceName": "温湿度传感器",
    "deviceType": "sensor",
    "protocol": "WiFi",
    "status": "online",
    "location": "living_room"
  }'

# 注册设备2
curl -X POST http://localhost:8084/api/edge/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "device-1779079690283-0983",
    "deviceName": "智能调光灯",
    "deviceType": "light",
    "protocol": "WiFi",
    "status": "online",
    "location": "bedroom"
  }'
```

然后更新设备状态：

```bash
# 更新设备1状态
curl -X PUT http://localhost:8084/api/edge/devices/device-1779073453825-9716/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "online",
    "power": 5.2,
    "temperature": 24.5,
    "humidity": 62.3
  }'

# 更新设备2状态
curl -X PUT http://localhost:8084/api/edge/devices/device-1779079690283-0983/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "online",
    "power": 15.0
  }'
```

---

### 方案三：清除前端localStorage（辅助步骤）

有时候前端缓存了旧的状态数据，可以尝试清除：

1. **方法1：浏览器开发者工具**

   - 打开浏览器开发者工具（F12）
   - 切换到"Application"选项卡
   - 左侧找到"Local Storage" → 选择您的网站
   - 右键删除所有项，或手动删除`deviceStates`项

2. **方法2：JavaScript控制台**

   ```javascript
   // 清除设备状态缓存
   localStorage.removeItem('deviceStates');
   
   // 清除所有localStorage
   localStorage.clear();
   
   // 刷新页面
   location.reload();
   ```

3. **方法3：禁用localStorage临时测试**

   在浏览器控制台中执行：

   ```javascript
   // 强制从后端获取，不使用localStorage
   localStorage.setItem('deviceStates', JSON.stringify({}));
   location.reload();
   ```

---

## 验证解决方案

### 1. 检查边缘服务状态

```bash
# 检查服务是否运行
sudo systemctl status edge-service

# 查看服务日志
sudo journalctl -u edge-service -n 50
```

### 2. 测试API

```bash
# 健康检查
curl http://localhost:8084/api/edge/health

# 获取所有设备状态
curl http://localhost:8084/api/edge/devices/status/all | jq

# 获取设备统计
curl http://localhost:8084/api/edge/devices/statistics | jq
```

### 3. 验证数据库

```sql
-- 查看所有设备及其状态
SELECT 
    d.device_id,
    d.device_name,
    d.status as device_status,
    ds.status as status_status,
    ds.power,
    ds.temperature,
    ds.humidity
FROM edge_device d
LEFT JOIN edge_device_status ds ON d.device_id = ds.device_id;
```

### 4. 检查前端网络请求

在浏览器开发者工具中：

1. 打开"Network"选项卡
2. 刷新页面
3. 查找对`/api/edge/devices/status/all`的请求
4. 检查响应中的设备状态是否为"online"

---

## 常见问题排查

### 问题1：MySQL连接失败

**症状：** 脚本执行时报错"Can't connect to MySQL server"

**解决方案：**
```bash
# 检查MySQL是否运行
sudo systemctl status mysql

# 启动MySQL
sudo systemctl start mysql

# 检查端口
sudo netstat -tuln | grep 3306
```

### 问题2：数据库不存在

**症状：** 报错"Unknown database 'smarthome_edge'"

**解决方案：**
```bash
# 先创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS smarthome_edge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 然后重新执行脚本
mysql -u root -p infini_rag_flow -D smarthome_edge < sql/init-edge-database.sql
```

### 问题3：设备状态还是offline

**症状：** 数据库中有设备，但状态还是offline

**解决方案：**
```sql
-- 直接更新状态
UPDATE edge_device_status 
SET status = 'online', last_update_time = NOW()
WHERE device_id IN ('device-1779073453825-9716', 'device-1779079690283-0983');

UPDATE edge_device
SET status = 'online', updated_at = NOW()
WHERE device_id IN ('device-1779073453825-9716', 'device-1779079690283-0983');
```

### 问题4：前端不刷新状态

**症状：** 数据库已更新，但前端还显示old

**解决方案：**
1. 清除浏览器缓存（Ctrl+Shift+Delete）
2. 强制刷新（Ctrl+F5）
3. 或清除localStorage后刷新

---

## 预防措施

### 1. 自动注册设备

修改前端代码，在设备首次连接时自动注册到数据库：

```javascript
// 在设备连接时自动注册
async function registerDevice(deviceId) {
  const response = await fetch('http://localhost:8084/api/edge/devices/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      deviceId: deviceId,
      deviceName: `设备-${deviceId}`,
      deviceType: 'sensor',
      protocol: 'WiFi',
      status: 'online',
      location: 'unknown'
    })
  });
  return response.json();
}
```

### 2. 定期同步状态

设置定时任务，每隔一段时间从设备同步状态到数据库：

```bash
# 添加到crontab
crontab -e

# 每5分钟执行一次状态同步
*/5 * * * * curl -X POST http://localhost:8084/api/edge/sync/cloud >> /var/log/edge-sync.log 2>&1
```

### 3. 监控告警

设置监控，当设备离线超过一定时间时发送告警：

```bash
# 检查设备离线超过10分钟的脚本
mysql -u root -p'infini_rag_flow' -D smarthome_edge -e "
SELECT device_id, status, last_update_time 
FROM edge_device_status 
WHERE status = 'offline' 
  AND last_update_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE);
"
```

---

## 总结

通过以上步骤，应该能够解决设备状态显示offline的问题：

1. ✅ 执行SQL脚本插入设备数据
2. ✅ 重启边缘服务
3. ✅ 清除浏览器缓存
4. ✅ 刷新前端页面
5. ✅ 验证API返回正确的状态

如果问题仍然存在，请检查：
- MySQL服务是否正常运行
- 边缘服务是否正确连接数据库
- 数据库中是否真的有设备记录
- 前端是否正确调用了API

祝您调试顺利！🎉
