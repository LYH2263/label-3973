# 中医药特色健康管理系统

基于 Spring Boot 3 + Vue 3 的全栈健康管理平台，融合中医体质辨识特色功能，支持健康档案管理、慢病跟踪、健康指标录入与 9 种中医体质测评，100% 容器化交付。

---

## 🛠 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Pinia + Vue Router + Vite |
| 后端 | Spring Boot 3.2 + MyBatis-Plus 3.5.7 + Spring Security Crypto |
| 接口文档 | Knife4j 4.4 (OpenAPI 3) |
| 认证 | JWT (jjwt 0.11.5) |
| 数据库 | MySQL 8.0 |
| 容器化 | Docker + Docker Compose |

---

## 🚀 启动指南

### 前置条件

- 已安装并启动 **Docker Desktop**
- 端口 `3973`、`8973`、`3306` 未被占用

### 一键启动

```bash
# 在项目根目录执行（首次构建约 5-10 分钟，需下载依赖）
docker compose up --build

# 后台运行
docker compose up --build -d
```

### 停止 & 清理

```bash
# 停止服务（保留数据库数据）
docker compose down

# 停止并清除所有数据（含数据库 volume）
docker compose down -v
```

### 重新构建单个服务

```bash
# 后端有代码变更时
docker compose build --no-cache backend
docker compose up backend

# 前端有代码变更时
docker compose build --no-cache frontend
docker compose up frontend
```

---

## 🔗 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:3973 | Vue 3 SPA |
| 后端 Swagger | http://localhost:8973/doc.html | Knife4j 接口文档 |
| 后端 API 根路径 | http://localhost:8973/api | REST API |
| 数据库 | localhost:3306 | MySQL 8.0 |

---

## 🧪 测试账号

| 账号 | 密码 | 说明 |
|------|------|------|
| `demo` | `demo123` | 演示账号，含预置档案数据 |
| `admin` | `admin123` | 管理员账号 |

> 账号由后端 `DataInitializer` 在首次启动时自动创建，密码使用 BCrypt 加密存储。

---

## 📋 功能说明

### 1. 账号认证
- 用户名/密码注册与登录
- JWT Token 鉴权（有效期 24 小时）
- 登录状态持久化（localStorage）
- Token 失效自动跳转登录页

### 2. 健康档案管理
- 卡片式展示患者档案列表
- 支持新增、编辑、删除操作
- 字段：姓名、性别、年龄、血型、过敏史、既往病史
- 档案与登录用户绑定，互相隔离

### 3. 慢病管理
- 按健康档案筛选，支持多档案切换
- 支持标记 9 种慢病类型：高血压、糖尿病、高血脂、冠心病、脑血管病、慢性肾病、慢性肺病、恶性肿瘤、其他
- 记录疾病名称、确诊日期、备注

### 4. 健康指标录入
- 按健康档案筛选，支持多次历史记录
- 录入指标：身高(cm)、体重(kg)、收缩压/舒张压(mmHg)、空腹血糖(mmol/L)、心率(次/分)
- 自动计算 BMI
- 血压/血糖异常自动标红、警告色提示
- 顶部展示最新一次指标概览卡片

### 5. 中医体质辨识（特色功能）
- **23 题问卷**，覆盖 9 种中医体质
- 答题方式：1-5 分制（从不 / 很少 / 有时 / 经常 / 总是）
- 逐题作答，支持前后翻页，进度条显示
- 自动计算各体质得分（标准化为百分制）
- 智能判定主要体质（平和质需同时满足其他类型得分均低于 30 分）
- 结果页展示：体质名称、特征描述、养生建议、各体质横向评分条形图
- 历史记录可随时回顾

---

## 🗂 项目结构

```
label-3973/
├── docker-compose.yml              # 服务编排（db / backend / frontend）
├── init.sql                        # 数据库初始化 DDL（纯英文，无中文）
├── README.md
│
├── backend/                        # Spring Boot 后端
│   ├── Dockerfile                  # Maven 多阶段构建 → eclipse-temurin:17-jre 运行
│   ├── settings.xml                # Maven 阿里云镜像加速
│   ├── pom.xml
│   └── src/main/java/com/tcm/health/
│       ├── TcmHealthApplication.java      # 启动类（排除 Security 自动配置）
│       ├── common/
│       │   ├── R.java                     # 统一响应体
│       │   ├── JwtUtil.java               # JWT 生成与解析
│       │   ├── JwtInterceptor.java        # 请求拦截器（验证 Token）
│       │   └── UserContext.java           # ThreadLocal 用户上下文
│       ├── config/
│       │   ├── WebMvcConfig.java          # CORS + 拦截器注册
│       │   ├── Knife4jConfig.java         # Swagger OpenAPI 配置
│       │   ├── MybatisPlusConfig.java     # 自动填充 created_at / updated_at
│       │   ├── PasswordConfig.java        # BCryptPasswordEncoder Bean
│       │   └── DataInitializer.java       # 启动时写入体质问题 + 演示数据（Java 写入避免乱码）
│       ├── entity/                        # 6 个实体：User / HealthRecord / ChronicDisease /
│       │                                  #   HealthIndicator / ConstitutionQuestion / ConstitutionResult
│       ├── mapper/                        # MyBatis-Plus BaseMapper（6 个）
│       ├── dto/                           # 请求 DTO（6 个，含 JSR-303 校验）
│       ├── service/ + service/impl/       # 业务逻辑层
│       └── controller/                    # REST 控制器（5 个）
│
└── frontend/                       # Vue 3 前端
    ├── Dockerfile                   # node:20-alpine 构建 → nginx:alpine 服务
    ├── nginx.conf                   # SPA 路由 + /api 代理到后端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js                  # 入口，注册 Element Plus + Pinia
        ├── App.vue
        ├── router/index.js          # 路由守卫（未登录跳转 /login）
        ├── store/user.js            # Pinia 用户状态（token + userInfo）
        ├── api/                     # axios 封装（request.js + 5 个模块）
        ├── components/
        │   └── MainLayout.vue       # 侧边栏导航 + 顶部用户栏布局
        └── views/
            ├── LoginView.vue        # 登录页（含演示账号快速填入）
            ├── RegisterView.vue     # 注册页
            ├── DashboardView.vue    # 首页概览（统计卡片 + 最新档案 + 最新体质结果）
            ├── HealthRecordView.vue # 健康档案（卡片增删改查）
            ├── ChronicDiseaseView.vue  # 慢病管理（表格 CRUD）
            ├── HealthIndicatorView.vue # 健康指标（录入 + 异常标色）
            └── ConstitutionView.vue    # 体质辨识（问卷 + 结果 + 历史）
```

---

## 🌐 API 接口一览

所有受保护接口需在 Header 中携带：`Authorization: Bearer <token>`

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 登录，返回 token |
| 认证 | POST | `/api/auth/register` | 注册 |
| 健康档案 | GET | `/api/records` | 查询当前用户所有档案 |
| 健康档案 | POST | `/api/records` | 新建档案 |
| 健康档案 | PUT | `/api/records/{id}` | 更新档案 |
| 健康档案 | DELETE | `/api/records/{id}` | 删除档案 |
| 慢病管理 | GET | `/api/chronic-diseases?recordId=` | 查询档案慢病列表 |
| 慢病管理 | POST | `/api/chronic-diseases` | 新增慢病 |
| 慢病管理 | PUT | `/api/chronic-diseases/{id}` | 更新慢病 |
| 慢病管理 | DELETE | `/api/chronic-diseases/{id}` | 删除慢病 |
| 健康指标 | GET | `/api/indicators?recordId=` | 查询档案指标列表 |
| 健康指标 | POST | `/api/indicators` | 录入指标 |
| 健康指标 | DELETE | `/api/indicators/{id}` | 删除指标 |
| 体质辨识 | GET | `/api/constitution/questions` | 获取全部问卷题目 |
| 体质辨识 | POST | `/api/constitution/submit` | 提交答案，返回结果 |
| 体质辨识 | GET | `/api/constitution/results` | 查看历史测评记录 |

完整接口说明请访问 Swagger：http://localhost:8973/doc.html

---

## 🔢 体质辨识算法说明

1. 题目共 23 道，每种体质 2-3 题，答案分值 1-5（从不=1 … 总是=5）
2. 每种体质得分 = `(平均分 - 1) ÷ 4 × 100`，归一化为百分制
3. 判定规则：
   - 平和质得分 ≥ 60 **且** 其他所有体质得分均 < 30 → 判定为**平和质**
   - 否则取非平和质中**得分最高**的体质为主要体质

---

## 🗄 数据库说明

| 表名 | 说明 |
|------|------|
| `user` | 用户账号（BCrypt 密码） |
| `health_record` | 健康档案（关联用户） |
| `chronic_disease` | 慢病记录（关联档案） |
| `health_indicator` | 健康指标记录（关联档案） |
| `constitution_question` | 体质问卷题目（启动时由 Java 初始化） |
| `constitution_result` | 体质测评结果（关联用户） |

> `init.sql` 仅含纯英文 DDL，所有中文内容（体质题目、演示数据）均由后端 `DataInitializer` 通过 JDBC 写入，彻底避免 SQL 文件编码乱码问题。

---

## ⚠️ 常见问题

**Q: 首次启动后登录提示"用户不存在"**
> 后端启动需要等数据库就绪后才能初始化数据，`docker compose up` 后等待后端日志出现 `Started TcmHealthApplication` 再访问。

**Q: 前端页面空白或 404**
> 确认 Docker 容器已全部启动：`docker compose ps`，检查 frontend 容器状态是否为 Up。

**Q: Swagger 无法访问**
> 访问 http://localhost:8973/doc.html，若后端未启动会超时，等待后端容器健康后再试。

**Q: 数据库中文乱码**
> 本项目已将所有中文数据改为 Java 代码初始化，JDBC URL 含 `characterEncoding=utf8`，正常情况下不会出现乱码。若已有旧数据，执行 `docker compose down -v` 清除 volume 后重新启动。
