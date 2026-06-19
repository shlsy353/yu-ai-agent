# Yu AI Agent Frontend

一个基于 Vue 3 的 AI 智能应用前端项目，提供两个 AI 聊天应用：

- **AI 恋爱大师** - 你的私人恋爱顾问，帮助解答情感问题
- **AI 超级智能体** - 强大的 AI 智能体，帮你完成各种任务

## 技术栈

- **Vue 3** (组合式 API + `<script setup>`)
- **Vite 5** (构建工具)
- **Vue Router 4** (路由管理)
- **Axios** (HTTP 请求库)

## 项目结构

```
src/
├── api/
│   ├── index.js          # Axios 配置（baseURL: http://localhost:8123/api）
│   └── ai.js             # SSE 接口封装
├── router/
│   └── index.js          # 路由配置
├── views/
│   ├── Home.vue          # 主页 - 应用切换
│   ├── LoveMaster.vue    # AI 恋爱大师 - 聊天室
│   └── Manus.vue         # AI 超级智能体 - 聊天室
├── App.vue               # 根组件
└── main.js               # 入口文件
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器将在 `http://localhost:3000` 启动。

### 构建生产版本

```bash
npm run build
```

## 后端接口

本项目需要配合后端服务运行，接口地址前缀：`http://localhost:8123/api`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/ai/love_app/chat/sse?message=&chatId=` | GET | AI 恋爱大师 SSE 聊天 |
| `/ai/manus/chat?message=` | GET | AI 超级智能体 SSE 聊天 |

两个接口均使用 SSE (Server-Sent Events) 实现流式响应，前端通过 `fetch` + `ReadableStream` 实时读取并展示对话内容。

## 功能说明

### 主页
项目入口，以卡片形式展示两个 AI 应用，点击即可进入对应的聊天页面。

### AI 恋爱大师
- 聊天室风格界面，用户消息在右侧，AI 消息在左侧
- 进入页面后自动生成唯一 `chatId`（格式：`love_{timestamp}_{random}`），用于区分不同会话
- 通过 SSE 流式调用后端接口，实时显示对话内容

### AI 超级智能体
- 聊天室风格界面，交互方式同恋爱大师
- 通过 SSE 流式调用后端接口，实时显示对话内容