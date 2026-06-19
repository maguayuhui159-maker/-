# 匠脉锡雕非遗数字平台

这是一个围绕锡雕非遗传承、课程学习、作品展示和文创交易搭建的全栈项目。平台面向学员、讲师和管理员三类角色，提供在线课程、作品审核、学习数据、线下活动、订单售后和 AI 辅助创作等能力，适合作为简历项目或面试展示项目。

## 项目亮点

- 非遗主题业务完整：覆盖课程学习、作品上传、作品售卖、线下活动预约、订单售后等核心流程。
- 多角色权限设计：管理员负责用户、课程、作品和运营数据；讲师负责课程与作业；学员负责学习、创作和购买。
- AI 能力接入：支持 AI 问答、作业点评和图片生成，可通过 OpenAI、DeepSeek 或豆包接口配置。
- 容器化部署：通过 Docker Compose 一键启动前端、后端、MySQL、Redis、Nginx 和 AI 服务。
- 展示友好：前端页面包含首页、数据看板、课程管理、作品集市、个人中心等模块，适合给 HR 或面试官演示。

## 技术栈

- 前端：Vue 3、Vite、Element Plus、Axios、Vue Router
- 后端：Spring Boot 3、MyBatis-Plus、MySQL、Redis、JWT
- AI 服务：FastAPI、Uvicorn、第三方大模型接口
- 部署：Docker、Docker Compose、Nginx

## 目录结构

```text
admin-web/     前端管理与展示页面
backend/       Spring Boot 后端接口服务
ai-service/    Python AI 服务
nginx/         网关反向代理配置
docs/          项目需求与角色说明文档
init.sql       数据库初始化脚本
deploy.sh      macOS / Linux 一键部署脚本
deploy.ps1     Windows 一键部署脚本
```

## 快速启动

1. 复制环境变量模板：

```bash
cp .env.example .env
```

2. 在 `.env` 中填写真实的 AI 接口密钥。三种供应商任选一种即可：

```bash
# 使用 OpenAI
AI_PROVIDER=openai
OPENAI_API_KEY=你的_OpenAI_API_Key
OPENAI_MODEL=gpt-4o-mini
```

```bash
# 使用 DeepSeek
AI_PROVIDER=deepseek
DEEPSEEK_API_KEY=你的_DeepSeek_API_Key
```

```bash
# 使用豆包 / 火山方舟
AI_PROVIDER=doubao
ARK_API_KEY=你的_火山方舟_API_Key
DOUBAO_MODEL=doubao-seed-1-6-250615
```

3. 启动项目：

```bash
# macOS / Linux
chmod +x deploy.sh
./deploy.sh
```

```powershell
# Windows PowerShell
.\deploy.ps1
```

## 访问地址

- 前端页面：`http://localhost/`
- 后端健康检查：`http://localhost/api/health`
- AI 服务健康检查：`http://localhost/api/ai/health`

## AI 接口测试

```bash
curl -X POST "http://localhost/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"请介绍一下锡雕入门步骤\",\"user_id\":1}"
```

## 默认演示账号

初始化脚本会写入演示用户，便于快速体验不同角色的功能：

- 管理员：`admin / admin123`
- 讲师：`uploader / uploader123`
- 学员：`student / student123`

首次登录后，后端会自动把明文演示密码升级为加密存储。

## 展示建议

给 HR 展示时，可以重点演示这几条主线：

- 首页：说明项目主题、非遗场景和视觉设计。
- 数据看板：展示不同角色的数据统计能力。
- 课程管理：展示课程上传、审核、购买和学习流程。
- 作品集市：展示作品上传、收藏、浏览记录和交易功能。
- AI 服务：展示 AI 问答、点评和图片生成能力。

## 注意事项

- `.env` 中保存真实密钥，不会被提交到 GitHub。
- `mysql_data/`、`redis_data/`、`node_modules/`、`dist/`、`target/` 都是本地运行产物，已通过 `.gitignore` 忽略。
- 本项目用于学习、作品集和面试展示，生产环境部署前还需要补充更严格的权限、安全和日志策略。
