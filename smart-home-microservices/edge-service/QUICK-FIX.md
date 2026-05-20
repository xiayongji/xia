# 快速修复：设备状态显示offline

## 问题
前端Dashboard显示所有设备状态为"offline"

## 快速解决方案（3步）

### 步骤1：执行SQL脚本（Windows）

打开PowerShell或CMD，执行：

```powershell
cd "c:\Users\user\Desktop\毕业设计\smart-home-microservices\edge-service"
scripts\insert-test-devices.bat
```

### 步骤2：重启边缘服务

```powershell
# 如果边缘服务是通过systemd运行
sudo systemctl restart edge-service

# 或者在Windows上重新启动Java进程
```

### 步骤3：清除浏览器缓存

按 `Ctrl+Shift+Delete`，勾选"Cookie"和"缓存"，点击清除。

然后按 `Ctrl+F5` 强制刷新页面。

## 验证修复

### 方法1：浏览器控制台

打开F12开发者工具，切换到Console，执行：

```javascript
localStorage.clear();
location.reload();
```

### 方法2：检查API响应

在浏览器开发者工具的Network选项卡中，查找对`/api/edge/devices/status/all`的请求，查看响应是否为：

```json
[
  {
    "deviceId": "device-1779073453825-9716",
    "status": "online",
    ...
  },
  {
    "deviceId": "device-1779079690283-0983",
    "status": "online",
    ...
  }
]
```

### 方法3：命令行测试

```powershell
curl http://localhost:8084/api/edge/devices/status/all
```

如果看到多个`"status":"online"`的记录，说明修复成功。

## 如果还是不行？

### 检查清单

1. MySQL服务是否运行？
   ```powershell
   sudo systemctl status mysql
   ```

2. 数据库是否存在？
   ```powershell
   mysql -u root -p -e "SHOW DATABASES;"
   ```

3. 设备数据是否插入成功？
   ```powershell
   mysql -u root -p infini_rag_flow -D smarthome_edge -e "SELECT * FROM edge_device_status;"
   ```

4. 边缘服务是否连接了正确的数据库？
   检查`application.yml`中的数据库配置。

## 更多信息

详细的故障排查指南请查看：[TROUBLESHOOTING.md](TROUBLESHOOTING.md)
