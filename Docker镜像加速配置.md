# Docker 镜像加速器配置

## 问题原因
Docker Hub 在中国大陆访问较慢或超时，导致镜像下载失败。

## 解决方案：配置镜像加速器

### 方法一：Docker Desktop 设置（推荐）

1. **打开 Docker Desktop**
2. **点击右上角设置图标（⚙️）**
3. **选择 "Docker Engine"**
4. **在编辑器中修改 JSON 配置**

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me",
    "https://docker.m.daocloud.io"
  ]
}
```

5. **点击 "Apply & Restart"**

---

### 方法二：手动配置文件

1. 找到 Docker 配置文件：
   - Windows: `%USERPROFILE%\.docker\daemon.json`
   - Mac: `~/.docker/daemon.json`

2. 创建或编辑文件，添加镜像源：

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me",
    "https://docker.m.daocloud.io",
    "https://ccr.ccs.tencentyun.com"
  ]
}
```

3. **重启 Docker Desktop**

---

### 方法三：使用阿里云镜像加速器（推荐国内用户）

1. 访问 https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
2. 登录阿里云账号
3. 复制你的专属加速器地址
4. 替换 JSON 中的地址

```json
{
  "registry-mirrors": [
    "https://你的ID.mirror.aliyuncs.com"
  ]
}
```

---

## 验证配置

重启 Docker Desktop 后，运行：

```powershell
docker info | findstr "Registry Mirrors"
```

应该显示：
```
Registry Mirrors:
 https://docker.1ms.run/
```

---

## 重新拉取 Redis

配置完成后，重新运行：

```powershell
docker pull redis:latest
```

---

## 备选方案：如果以上方法都失败

使用 Valkey 替代 Redis（已安装）

```powershell
docker run -d --name redis -p 6379:6379 valkey/valkey:8
```

Valkey 是 Redis 的兼容替代品，完全兼容 Redis 协议，可以直接使用。
