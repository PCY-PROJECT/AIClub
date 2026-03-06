# AI 知识库 — 服务器部署指南

> 适用环境：Ubuntu 22.04 LTS（推荐）/ CentOS 8+
> 架构：前端 Nginx 静态托管 + 后端 Spring Boot JAR + MySQL 8.0

---

## 一、服务器环境准备

### 1.1 安装 JDK 17

```bash
apt update && apt install -y openjdk-17-jdk
java -version   # 确认输出 openjdk 17
```

### 1.2 安装 MySQL 8.0

```bash
apt install -y mysql-server
systemctl start mysql
systemctl enable mysql

# 初始化安全设置
mysql_secure_installation
```

### 1.3 安装 Nginx

```bash
apt install -y nginx
systemctl start nginx
systemctl enable nginx
```

### 1.4 安装 Node.js 18+（仅构建前端用，服务器上可选）

```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install -y nodejs
node -v
```

---

## 二、数据库初始化

### 2.1 上传备份文件

将 `backend/src/main/resources/db/full_backup.sql` 上传到服务器：

```bash
scp backend/src/main/resources/db/full_backup.sql root@YOUR_SERVER_IP:/tmp/
```

### 2.2 创建数据库和用户

```sql
-- 以 root 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE ai_invitecode CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建专用用户（替换 YOUR_DB_PASSWORD）
CREATE USER 'aicode'@'localhost' IDENTIFIED BY 'YOUR_DB_PASSWORD';
GRANT ALL PRIVILEGES ON ai_invitecode.* TO 'aicode'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2.3 导入数据

```bash
mysql -u aicode -p ai_invitecode < /tmp/full_backup.sql
# 输入上一步设置的密码
```

### 2.4 验证

```bash
mysql -u aicode -p ai_invitecode -e "SHOW TABLES;"
# 应显示 t_user, t_invite_code, t_product, t_site 等表
```

---

## 三、后端部署

### 3.1 本地打包（在开发机执行）

```bash
cd backend
./mvnw clean package -DskipTests
# 产物：target/backend-0.0.1-SNAPSHOT.jar
```

### 3.2 上传 JAR

```bash
scp target/backend-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/aicode/
```

### 3.3 创建生产配置文件

在服务器上创建 `/opt/aicode/application-prod.yml`，**务必替换所有占位符**：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_invitecode?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: aicode
    password: YOUR_DB_PASSWORD          # 替换为数据库密码

  mail:
    host: smtp.qq.com                   # 替换为你的邮件服务商
    port: 587
    username: your@qq.com               # 替换为发件邮箱
    password: YOUR_MAIL_AUTH_CODE       # QQ邮箱填授权码，非登录密码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  jwt:
    secret: REPLACE_WITH_64_CHAR_RANDOM_STRING_FOR_PRODUCTION_USE_ONLY
    expiration: 7200
  encrypt:
    key: REPLACE_WITH_32_CHAR_AES_KEY_!!

logging:
  level:
    com.aicode: info
    org.springframework.security: warn
```

> **安全提示**：`jwt.secret` 建议用 `openssl rand -hex 32` 生成，`encrypt.key` 用 `openssl rand -hex 16` 生成。

### 3.4 创建 systemd 服务（开机自启）

创建 `/etc/systemd/system/aicode.service`：

```ini
[Unit]
Description=AI Knowledge Base Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/aicode
ExecStart=/usr/bin/java \
  -Xms512m -Xmx1g \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -Dspring.config.additional-location=/opt/aicode/application-prod.yml \
  -jar /opt/aicode/backend-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/aicode/app.log
StandardError=append:/var/log/aicode/app.log

[Install]
WantedBy=multi-user.target
```

```bash
mkdir -p /var/log/aicode

systemctl daemon-reload
systemctl enable aicode
systemctl start aicode

# 查看启动日志
journalctl -u aicode -f
# 或
tail -f /var/log/aicode/app.log
```

### 3.5 验证后端

```bash
curl http://localhost:8080/api/products
# 返回 {"code":0,"message":"ok","data":[...]} 即正常
```

---

## 四、前端部署

### 4.1 修改生产环境 API 地址

编辑 `frontend/.env.production`：

```env
VITE_API_BASE_URL=https://your-domain.com
```

### 4.2 本地构建（在开发机执行）

```bash
cd frontend
npm install
npm run build
# 产物在 frontend/dist/
```

### 4.3 上传构建产物

```bash
scp -r frontend/dist/* root@YOUR_SERVER_IP:/var/www/aicode/
```

### 4.4 配置 Nginx

创建 `/etc/nginx/sites-available/aicode`：

```nginx
server {
    listen 80;
    server_name your-domain.com;          # 替换为你的域名或服务器 IP

    root /var/www/aicode;
    index index.html;

    # 前端 SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 30s;
        proxy_read_timeout 60s;
    }

    # Swagger 文档（可选，生产环境建议关闭）
    location /doc.html {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }

    # 静态资源长缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}
```

```bash
mkdir -p /var/www/aicode

# 启用配置
ln -s /etc/nginx/sites-available/aicode /etc/nginx/sites-enabled/
nginx -t          # 检查配置语法
systemctl reload nginx
```

---

## 五、HTTPS 配置（推荐）

使用 Certbot 免费申请 Let's Encrypt 证书（需要已绑定域名）：

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d your-domain.com
# 按提示操作，自动续期已内置
```

配置完成后 Nginx 会自动更新为 HTTPS，并将 HTTP 重定向到 HTTPS。

---

## 六、微信群二维码配置

将微信群二维码图片命名为 `wechat-qr.png`，放入构建产物目录：

```bash
scp wechat-qr.png root@YOUR_SERVER_IP:/var/www/aicode/wechat-qr.png
```

---

## 七、后续更新部署

### 更新后端

```bash
# 开发机打包并上传
./mvnw clean package -DskipTests
scp target/backend-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/aicode/

# 服务器重启服务
systemctl restart aicode
```

### 更新前端

```bash
# 开发机构建并上传
npm run build
scp -r frontend/dist/* root@YOUR_SERVER_IP:/var/www/aicode/
# Nginx 无需重启，静态文件自动生效
```

---

## 八、常用运维命令

```bash
# 查看后端状态
systemctl status aicode

# 实时日志
tail -f /var/log/aicode/app.log

# 重启后端
systemctl restart aicode

# 重载 Nginx（前端更新后）
systemctl reload nginx

# 数据库备份
mysqldump -u aicode -p ai_invitecode > /backup/aicode_$(date +%Y%m%d).sql

# 查看端口占用
ss -tlnp | grep -E '8080|80|443'
```

---

## 九、管理员账号

| 字段 | 值 |
|------|-----|
| 邮箱 | `oker@test.com` |
| 密码 | `Admin@123` |
| 后台入口 | `https://your-domain.com/finderFundadmin` |

> **部署后请立即修改管理员密码。** 通过后台「用户」Tab 找到该账号，或直接更新数据库：
> ```sql
> -- 生成新密码 hash：python3 -c "import bcrypt; print(bcrypt.hashpw(b'新密码', bcrypt.gensalt(10)).decode())"
> UPDATE t_user SET password='新hash' WHERE email='oker@test.com';
> ```

---

## 十、防火墙配置

```bash
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw deny 8080/tcp   # 禁止外部直接访问后端（通过 Nginx 代理）
ufw enable
```
