# Fit AI Agent（fitagent）

面向运动健康场景的 AI 应用：支持每日打卡、AI 规划 30 天计划，以及两种 AI 对话能力（健康咨询 / 超级智能体）。

## 功能概览

| 模块 | 说明 |
|------|------|
| **用户与权限** | 注册、登录（JWT）、个人角色；管理员可管理用户 |
| **30 天健康计划** | AI 问答收集需求 → 生成计划预览 → 确认写入 → 按日打卡 |
| **健康咨询** | 多轮运动健康对话，记忆按用户隔离，支持新建/切换历史会话 |
| **超级智能体** | 可结合位置与需求分步完成任务，支持联网检索、生成可下载的 PDF 计划 |
| **文件下载** | 智能体生成的 PDF 上传至腾讯云 COS，登录后短时链接下载 |

## 技术栈

**后端**

- Java 21、Spring Boot 3
- Spring AI（阿里云百炼 / DashScope）
- MySQL + MyBatis-Flex
- Redis（部分会话缓存）
- 腾讯云 COS（PDF 对象存储）

**前端**

- Vue 3、TypeScript、Vite
- Pinia、Vue Router、Tailwind CSS

## 目录结构

```text
fit-ai-agent/                 # 后端（Spring Boot）
├── sql/                      # 建表脚本
├── src/main/java/...         # 业务代码
└── src/main/resources/       # 配置（application.yml 等）

fit-ai-agent-frontend/        # 前端（Vue）
├── src/views/                # 页面
├── src/components/           # 组件
└── docs/                     # 前端相关文档
```

## 快速开始

### 1. 准备环境

- JDK 21、Maven
- Node.js 18+
- MySQL、Redis
- 配置百炼 API Key；PDF 下载需配置 COS（可放在 `application-local.yml`）

### 2. 初始化数据库

执行 `sql/` 目录下脚本，例如：

```bash
mysql -u root -p < sql/user.sql
mysql -u root -p < sql/plan.sql
mysql -u root -p < sql/chat_message.sql
mysql -u root -p < sql/chat_session.sql
```

按本地实际情况修改 `src/main/resources/application.yml` 中的数据库账号密码。敏感配置建议写在已 gitignore 的 `application-local.yml`。

### 3. 启动后端

```bash
# 在项目根目录 fit-ai-agent
mvn spring-boot:run
```

默认接口前缀：`http://localhost:8122/api`  
接口文档：`http://localhost:8122/api/doc.html`

### 4. 启动前端

```bash
cd fit-ai-agent-frontend
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`  
前端通过 Vite 将 `/api` 代理到后端。

## 使用提示

1. 注册并登录后进入打卡页。
2. 无计划时可点「AI 帮我规划」，完成问答并确认 30 天计划。
3. 在「AI 对话」中选择：
   - **健康咨询**：日常运动问题多轮问答
   - **超级智能体**：复杂任务与 PDF 生成（需登录后下载）

默认管理员账号见 `sql/user.sql`（若已初始化）。

## 说明

- 健康咨询对话按登录用户隔离，不会串号。
- 超级智能体为单轮任务流；PDF 在 COS 保留约 7 天。
- 更细的前端启动与验收流程可见：`fit-ai-agent-frontend/快速启动说明.md`。
