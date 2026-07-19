# Yu AI Agent — AI 智能对话助手

> 本项目为学习项目，在老师原有的项目基础上进行修改，并添加了 RAG 模型、工具调用（Function Calling）、AI Agent 体系、前端页面等功能。
>
> **技术栈**：Spring Boot 3.5.14 + Spring AI 1.1.x + Vue 3 + Vite + 阿里云百炼 DashScope
> **底层模型**：DeepSeek-v4-flash（阿里云百炼部署）
> **构建工具**：Maven + JDK 21 / npm + Vite
> **数据库**：PostgreSQL + PGVector（向量检索）
> **文档**：Swagger (Knife4j) — http://localhost:8123/api/swagger-ui.html
快速启动项目预览：
> 
> 
>   $env:JAVA_HOME="E:\xuexi_app\jdk_21"
    $env:PATH="$env:JAVA_HOME\bin;$env:PATH"
    $env:DASHSCOPE_API_KEY="你的新DashScope Key"
    $env:SEARCH_API_KEY="你的Search API Key"

mvn "-Dmaven.repo.local=E:\ieda_project\yu-ai-agent\.m2\repository" spring-boot:run
---

## 目录

1. [项目全景图](#一项目全景图)
2. [快速启动](#二快速启动)
3. [项目结构](#三项目结构)
4. [前端页面介绍](#四前端页面介绍)
5. [后端核心模块](#五后端核心模块)
6. [AI Agent 体系（ReAct 模式）](#六ai-agent-体系react-模式)
7. [工具调用系统（Function Calling）](#七工具调用系统function-calling)
8. [RAG 管线全解析](#八rag-管线全解析)
9. [三套向量数据库对比](#九三套向量数据库对比)
10. [Advisor 拦截器链](#十advisor-拦截器链)
11. [配置体系](#十一配置体系)
12. [API 接口一览](#十二api-接口一览)
13. [面试常见问题](#十三面试常见问题)
14. [常见问题 FAQ](#十四常见问题-faq)

---

## 一、项目全景图

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              前端 (Vue 3 + Vite)                              │
│  ┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐    │
│  │   Home.vue       │    │   LoveMaster.vue     │    │   Manus.vue          │    │
│  │   首页，选择应用   │    │   AI 恋爱大师         │    │   AI 超级智能体       │    │
│  └────────┬────────┘    └──────────┬──────────┘    └──────────┬──────────┘    │
│           │                        │                          │               │
│           └────────────────────────┼──────────────────────────┘               │
│                                    │                                          │
│                          SSE 流式通信 (fetch + ReadableStream)                 │
└────────────────────────────────────┼──────────────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────┼──────────────────────────────────────────┐
│                              后端 (Spring Boot)                                │
│                                    │                                          │
│  ┌─────────────────────────────────▼──────────────────────────────────────┐  │
│  │                      AiContronller (REST API)                           │  │
│  │  /api/love_app/chat/sse        → 恋爱大师 SSE 流式                       │  │
│  │  /api/love_app/chat/sync       → 恋爱大师 同步                           │  │
│  │  /api/manus/chat               → 超级智能体 SSE 流式                     │  │
│  └─────────────────────────────────┬──────────────────────────────────────┘  │
│                                    │                                          │
│               ┌────────────────────┼────────────────────┐                     │
│               ▼                    ▼                    ▼                     │
│  ┌────────────────────┐  ┌──────────────────┐  ┌──────────────────────┐      │
│  │     LoveApp        │  │   YuManus        │  │   6 个 Tool 工具      │      │
│  │  doChat()          │  │  (Agent)         │  │  联网搜索 / 网页抓取   │      │
│  │  doChatWithReport()│  │  ReAct 循环       │  │  文件读写 / 下载      │      │
│  │  doChatWithRag()   │  │  think → act     │  │  PDF生成 / 终端命令   │      │
│  │  recommendPartner()│  │  → 循环          │  │                      │      │
│  │  doChatWithTools() │  │                  │  │                      │      │
│  │  doChatByStream()  │  │                  │  │                      │      │
│  └─────────┬──────────┘  └────────┬─────────┘  └──────────┬───────────┘      │
│            │                      │                        │                  │
│            ▼                      ▼                        │                  │
│  ┌─────────────────────────────────────────────────────────┴──────────┐       │
│  │                        ChatClient (核心聊天客户端)                     │       │
│  │  builder(dashscopeChatModel)                                         │       │
│  │  .defaultSystem("恋爱顾问提示词")                                      │       │
│  │  .defaultAdvisors(MyLoggerAdvisor, MessageChatMemoryAdvisor)         │       │
│  └────────────────────────────────┬────────────────────────────────────┘       │
│                                   │                                            │
│                                   ▼                                            │
│                         ┌──────────────────┐                                   │
│                         │  DashScope API    │                                   │
│                         │  (DeepSeek 模型)   │                                   │
│                         └──────────────────┘                                   │
│                                                                                │
│    ┌─────────────────── 三条外部数据通道 ────────────────────┐                  │
│    │                                                        │                  │
│    ▼                                                        ▼                  │
│  ┌─────────────────┐  ┌──────────────────────┐  ┌──────────────────┐          │
│  │SimpleVectorStore│  │DashScopeDocument      │  │ PgVectorStore     │          │
│  │(内存向量库)      │  │Retriever             │  │(PostgreSQL持久化)  │          │
│  │recommendPartner │  │(阿里云知识库)          │  │测试中，未接入业务   │          │
│  │专用             │  │doChatWithRag 专用     │  │                   │          │
│  └───────┬─────────┘  └──────────────────────┘  └──────────────────┘          │
│          │                                                                     │
│          │  启动时加载                                                          │
│          ▼                                                                     │
│  ┌──────────────────────────────────────────┐                                  │
│  │  document/*.md (4 个 Markdown 知识文件)    │                                  │
│  │  → LoveAppDocumentLoader 读取             │                                  │
│  │  → MyKeywordEnricher 自动加关键词          │                                  │
│  │  → 存入 SimpleVectorStore                 │                                  │
│  └──────────────────────────────────────────┘                                  │
└────────────────────────────────────────────────────────────────────────────────┘
```

---

## 二、快速启动

### 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 21+ |
| Maven | 3.6+ |
| Node.js | 18+ |
| PostgreSQL | 15+（可选，PGVector 需要） |

### 1. 配置 API Key

编辑 `src/main/resources/application-local.yml`：

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-xxx          # 阿里云百炼 API Key（必填）
  datasource:
    url: jdbc:postgresql://localhost:5432/yu_ai_agent
    username: postgres
    password: root

search-api:
  api-key: xxx                  # searchapi.io API Key（超级智能体联网搜索需要）
```

### 2. 启动后端

```bash
# PowerShell
$env:JAVA_HOME="E:\xuexi_app\jdk_21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn spring-boot:run
```

后端启动后访问 http://localhost:8123/api/healthy 验证。

### 3. 启动前端

```bash
cd yu-ai-agent-frontend
npm install
npm run dev
```

前端启动后访问 http://localhost:3000。

### 4. 访问页面

| 页面 | 地址 | 说明 |
|------|------|------|
| 首页 | http://localhost:3000 | 选择 AI 应用 |
| AI 恋爱大师 | http://localhost:3000/#/love-master | 恋爱顾问对话 |
| AI 超级智能体 | http://localhost:3000/#/manus | Agent 工具调用对话 |

---

## 三、项目结构

```
yu-ai-agent/
├── pom.xml                                          # Maven 依赖总控
│
├── src/main/java/com/tripo/yuaiagent/
│   ├── YuAiAgentApplication.java                    # ★ 启动类
│   │
│   ├── app/
│   │   └── LoveApp.java                             # ★★★ 核心业务（6 个功能）
│   │
│   ├── agent/model/                                 # ★★ AI Agent 体系（新增）
│   │   ├── AgentState.java                          #   Agent 状态枚举（IDLE→RUNNING→FINISHED/ERROR）
│   │   ├── BaseAgent.java                           #   Agent 基类，状态管理 + 流式执行
│   │   ├── ReActAgent.java                          #   ReAct 模式：think → act 循环
│   │   ├── ToolCallAgent.java                       #   工具调用 Agent，think 生成 + act 执行
│   │   └── YuManus.java                             #   超级智能体，配置提示词和工具
│   │
│   ├── tools/                                       # ★★ 工具调用
│   │   ├── ToolRegistration.java                    #   工具注册中心（@Configuration）
│   │   ├── WebSearchTool.java                       #   百度联网搜索
│   │   ├── WebScrapingTool.java                     #   网页内容抓取
│   │   ├── FileOperationTool.java                   #   文件读写操作
│   │   ├── ResourceDownloadTool.java                #   网络资源下载
│   │   ├── PDFGenerationTool.java                   #   PDF 文档生成
│   │   └── TerminalOperationTool.java               #   终端命令执行
│   │
│   ├── rag/                                         # ★★ RAG 相关组件
│   │   ├── LoveAppDocumentLoader.java               #   加载 markdown 文档
│   │   ├── MyTokenTextSplitter.java                 #   文本分割器（分词）
│   │   ├── MyKeywordEnricher.java                   #   AI 自动提取关键词
│   │   ├── QueryRewriter.java                       #   查询重写器
│   │   ├── LoveAppVectorStoreConfig.java            #   SimpleVectorStore 配置
│   │   ├── LoveAppRagCloudAdvisorConfig.java        #   阿里云知识库 Advisor
│   │   └── PgVectorVectorStoreConfig.java           #   PGVector 向量库配置
│   │
│   ├── advisor/                                     # 拦截器
│   │   ├── MyLoggerAdvisor.java                     #   请求/响应日志
│   │   └── ReReadingAdvisor.java                    #   Re2 重复阅读优化
│   │
│   ├── chatMemory/
│   │   └── FileBasedChatMemory.java                 #   Kryo 持久化聊天记录
│   │
│   ├── constant/
│   │   └── FileConstant.java                        # ★ 文件保存路径常量
│   │
│   ├── controller/
│   │   ├── HealthyController.java                   #   GET /api/healthy
│   │   └── AiContronller.java                       #   AI 对话接口
│   │
│   └── demo/invoke/                                 # 四个逐步进阶的调用示例
│       ├── HttpAiInvoke.java                        #   HTTP 裸调（Hutool）
│       ├── SdkAiInvoke.java                         #   DashScope SDK 调用
│       ├── SpringAiInvoke.java                      #   Spring AI ChatModel
│       └── TestApiKey.java                          #   API Key 定义
│
├── src/main/resources/
│   ├── application.yml                              # 主配置（pgvector、端口、Swagger）
│   ├── application-local.yml                        # 本地配置（DB、API Key）
│   └── document/                                    # ★ 知识库原始数据
│       ├── 恋爱常见问题和回答 - 恋爱篇.md
│       ├── 恋爱常见问题和回答 - 单身篇.md
│       ├── 恋爱常见问题和回答 - 已婚篇.md
│       └── 恋爱对象库.md
│
├── src/test/java/com/tripo/yuaiagent/
│   ├── app/LoveAppTest.java                         # ★ 集成测试
│   ├── tools/                                       # ★★ 工具单元测试
│   │   ├── WebSearchToolTest.java
│   │   ├── WebScrapingToolTest.java
│   │   ├── FileOperationToolTest.java
│   │   ├── ResourceDownloadToolTest.java
│   │   ├── PDFGenerationToolTest.java
│   │   └── TerminalOperationToolTest.java
│   ├── rag/LoveAppDocumentLoaderTest.java
│   └── rag/PgVectorVectorStoreConfigTest.java
│
└── yu-ai-agent-frontend/                            # ★★★ 前端项目
    ├── package.json                                 #   依赖：Vue 3、Vite、Vue Router、Axios
    ├── vite.config.js                               #   Vite 配置（端口 3000）
    ├── index.html                                   #   入口 HTML
    ├── dist/                                        #   构建产物
    └── src/
        ├── main.js                                  #   Vue 入口
        ├── App.vue                                  #   根组件
        ├── api/
        │   ├── index.js                             #   Axios 实例（baseURL: /api）
        │   └── ai.js                                #   AI 接口封装（SSE 流式）
        ├── router/
        │   └── index.js                             #   路由配置（3 个页面）
        └── views/
            ├── Home.vue                             #   首页：选择 AI 应用
            ├── LoveMaster.vue                       #   AI 恋爱大师对话页
            └── Manus.vue                            #   AI 超级智能体对话页
```

---

## 四、前端页面介绍

### 前端技术栈

| 技术 | 用途 |
|------|------|
| Vue 3 | 前端框架（Composition API） |
| Vite | 构建工具，开发服务器 |
| Vue Router | 路由管理 |
| Axios | HTTP 请求库（主要用于 baseURL 配置） |
| Fetch API + ReadableStream | SSE 流式接收 |

### 页面说明

#### 首页（Home.vue）

- 路由：`/`
- 展示两个应用卡片，点击进入对应页面
- 卡片 1：AI 智能恋爱大师 → `/love-master`
- 卡片 2：AI 超级智能体 → `/manus`

#### AI 恋爱大师（LoveMaster.vue）

- 路由：`/love-master`
- 调用接口：`GET /api/ai/love_app/chat/sse`（SSE 流式）
- 功能：恋爱顾问对话，基于知识库 + 提示词回答情感问题
- 特点：每次对话自动生成 chatId，流式输出，打字机效果

#### AI 超级智能体（Manus.vue）

- 路由：`/manus`
- 调用接口：`GET /api/ai/manus/chat`（SSE 流式）
- 功能：基于 ReAct 模式的 Agent，可自动调用工具（搜索、抓取网页等）
- 特点：显示处理进度（"正在处理... 步骤 2/20"），最终只展示 AI 整理好的回答

### SSE 通信流程

```
前端（LoveMaster.vue / Manus.vue）
  │
  ├─ sendMessage() → 调用 ai.js 中的方法
  │
  ├─ ai.js: fetch(url, headers: { Accept: 'text/event-stream' })
  │   用原生 fetch + ReadableStream 读取 SSE 数据
  │
  ├─ 后端返回 SseEmitter，逐条 send(data)
  │
  └─ 前端逐条解析 data: 开头的行，调用 onData 回调
       → 恋爱大师：直接拼接显示
       → 超级智能体：区分 [STATUS] 进度 和 [RESULT] 最终结果
```

---

## 五、后端核心模块

### 1. LoveApp — 恋爱大师业务类

位于 `app/LoveApp.java`，是整个项目的核心业务类，提供 6 个功能方法：

| 方法 | 功能 | 使用的 Advisor |
|------|------|---------------|
| `doChat(message, chatId)` | 普通聊天，带记忆 | MyLoggerAdvisor + MessageChatMemoryAdvisor |
| `doChatWithReport(message, chatId)` | 生成结构化恋爱报告（JSON） | MyLoggerAdvisor + MessageChatMemoryAdvisor |
| `doChatWithRag(message, chatId)` | 基于阿里云知识库问答 | 加上 loveAppRagCloudAdvisor |
| `recommendPartner(message)` | 基于向量库推荐恋爱对象 | 加上 QuestionAnswerAdvisor |
| `doChatWithTools(message, chatId)` | 普通聊天 + 工具调用 | 加上工具列表 |
| `doChatByStream(message, chatId)` | 流式聊天（SSE） | 返回 Flux\<String\> |

### 2. 控制器层

位于 `controller/AiContronller.java`：

```java
@RestController
@RequestMapping("/ai")
public class AiContronller {

    @GetMapping("/love_app/chat/sync")   // 同步调用
    @GetMapping("/love_app/chat/sse")    // SSE 流式（恋爱大师）
    @GetMapping("/manus/chat")           // SSE 流式（超级智能体）
}
```

### 3. 学习路线：从裸调到业务

```
第 1 层：HttpAiInvoke                    HTTP + Hutool 手写请求
  │      "原来调用 AI 就是在发 HTTP 请求"
  ▼
第 2 层：SdkAiInvoke                     DashScope SDK 封装
  │      "SDK 帮我处理了 HTTP 细节"
  ▼
第 3 层：SpringAiInvoke                  Spring AI 的 ChatModel
  │      "Spring AI 统一了不同厂商的接口"
  ▼
第 4 层：LoveApp                          ChatClient 编排业务流程
  │      "加记忆、加 RAG、加工具调用"
  ▼
第 5 层：REST 接口 + Vue 前端             @RestController 暴露 HTTP 接口
         "完整的 Web 应用"
```

---

## 六、AI Agent 体系（ReAct 模式）

### 什么是 Agent？

Agent = 能自主决策、调用工具、完成复杂任务的 AI 系统。和普通对话的区别：

| 普通对话 | Agent |
|---------|-------|
| 一问一答，单次调用 LLM | 多步循环，多次调用 LLM |
| 只能"说" | 能"做"（调用工具） |
| 固定流程 | 自主决策调用什么工具 |

### Agent 类继承关系

```
BaseAgent（基类）
  ├─ 状态管理：IDLE → RUNNING → FINISHED/ERROR
  ├─ runStream()：流式执行入口
  │
  └─ ReActAgent（ReAct 模式）
       ├─ step()：think() → act() 循环
       │
       └─ ToolCallAgent（工具调用）
            ├─ think()：调用 LLM，判断是否需要工具
            ├─ act()：执行工具调用
            │
            └─ YuManus（超级智能体）
                 配置提示词、工具、最大步数
```

### ReAct 循环详解

```
用户: "北京明天天气怎么样？"
  │
  ▼
Step 1: think() → 调用 LLM → "我需要搜索北京天气" → 返回 true（需要工具）
Step 2: act()  → 调用 searchWeb("北京天气")
Step 3: think() → 调用 LLM → "我需要抓取天气网页内容" → 返回 true
Step 4: act()  → 调用 scrapeWebPage(url)
Step 5: think() → 调用 LLM → "数据拿到了，我整理一下回答... 调用 terminate" → 返回 true
Step 6: act()  → 执行 doTerminate
  │
  ▼
最终输出: "北京明天多云，气温 15℃~26℃..."
```

### Agent 状态机

```
IDLE ──→ RUNNING ──→ FINISHED（正常结束）
                    ├─→ ERROR（异常结束）
```

### 前端进度显示

后端在每一步循环前发送 `[STATUS]` 进度提示，循环结束后发送 `[RESULT]` 最终结果：

```javascript
// Manus.vue 中处理
onData(data) {
  if (data.startsWith('[STATUS]')) {
    statusText.value = data.slice(8)   // 显示 "正在处理... (步骤 2/20)"
  }
  if (data.startsWith('[RESULT]')) {
    aiContent = data.slice(8)           // 最终 AI 回答
  }
}
```

---

## 七、工具调用系统（Function Calling）

### 什么是 Function Calling？

让 AI 模型能主动调用外部工具的能力：

```
传统 Chat：用户 → AI（只能动嘴说）
工具调用： 用户 → AI（决定需要什么工具）→ 执行 Java 方法 → 结果返回 AI → AI 回复用户
```

### 6 个注册工具一览

| 工具 | 类名 | @Tool 方法 | 能力 | 依赖 |
|------|------|-----------|------|------|
| **联网搜索** | `WebSearchTool` | `searchWeb(query)` | 通过 searchapi.io 调用百度搜索 | searchapi.io API Key |
| **网页抓取** | `WebScrapingTool` | `scrapeWebPage(url)` | 爬取指定 URL 的 HTML 内容 | Jsoup |
| **文件读写** | `FileOperationTool` | `readFile()` / `writeFile()` | 读取/写入文件到 `./tmp/file/` | Hutool |
| **资源下载** | `ResourceDownloadTool` | `downloadResource()` | 下载网络资源到 `./tmp/download/` | Hutool |
| **PDF 生成** | `PDFGenerationTool` | `generatePDF()` | 生成 PDF 文件到 `./tmp/pdf/` | iText |
| **终端命令** | `TerminalOperationTool` | `executeTerminalCommand()` | 执行 cmd.exe 命令 | 无 |

### 工具注册机制

```java
@Configuration
public class ToolRegistration {
    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(
            new FileOperationTool(),
            new WebSearchTool(searchApiKey),
            new WebScrapingTool(),
            new ResourceDownloadTool(),
            new TerminalOperationTool(),
            new PDFGenerationTool()
        );
    }
}
```

### 文件保存目录

```
项目根目录/
  └── tmp/
      ├── file/        ← FileOperationTool 读写
      ├── download/    ← ResourceDownloadTool 下载
      └── pdf/         ← PDFGenerationTool 生成
```

---

## 八、RAG 管线全解析

### 什么是 RAG？

RAG = Retrieval-Augmented Generation = **检索增强生成**

```
不 RAG：用户问 → 模型凭记忆回答 → 可能胡说八道（幻觉）
RAG：   用户问 → 先查知识库 → 把资料+问题给模型 → 基于资料回答（有据可查）
```

### RAG 管线组件

| 组件 | 类名 | 作用 |
|------|------|------|
| 文档加载 | `LoveAppDocumentLoader` | 从 `document/*.md` 读取原始文本 |
| 文本分割 | `MyTokenTextSplitter` | 把长文档切成小块（token 级别） |
| 关键词增强 | `MyKeywordEnricher` | AI 自动给每段加关键词标签 |
| 查询重写 | `QueryRewriter` | 把用户口语化问题改写得更利于检索 |
| 向量检索 | `VectorStore` | 把问题和文档都转成向量，找最相似的 |
| 结果注入 | `Advisor` | 把查到的资料塞进 prompt 给模型 |

### 完整 RAG 链路

```
用户提问 → 查询重写 → 向量检索 → 组装 Prompt（资料+问题）→ AI 模型回答
```

---

## 九、三套向量数据库对比

| 对比维度 | SimpleVectorStore | 阿里云百炼知识库 | PGVector |
|----------|------------------|----------------|----------|
| 配置类 | `LoveAppVectorStoreConfig` | `LoveAppRagCloudAdvisorConfig` | `PgVectorVectorStoreConfig` |
| 存储位置 | Java 内存，重启消失 | 阿里云控制台上传 | PostgreSQL 数据库表 |
| 数据来源 | 自动加载 `document/*.md` | 手动去阿里云后台传文件 | 通过 Java 代码 INSERT |
| 目前谁在用 | `recommendPartner()` | `doChatWithRag()` | 只有测试类在用 |
| 适用场景 | 快速原型、数据量小 | 生产级、数据在云上 | 数据量大、需持久化 |
| 费用 | 免费 | 按量付费 | 免费（自建 PG） |

---

## 十、Advisor 拦截器链

### Advisor 执行顺序

```
请求 → MyLoggerAdvisor.before()         ① 日志
     → MessageChatMemoryAdvisor.before() ② 加载历史
     → 功能 Advisor（RAG/向量检索）       ③ 注入知识
     → [AI 模型调用]
     → MessageChatMemoryAdvisor.after()  ④ 保存历史
     → MyLoggerAdvisor.after()           ⑤ 日志
← 响应
```

### 各 Advisor 详解

| Advisor | 类型 | 作用 |
|---------|------|------|
| `MyLoggerAdvisor` | 自定义 | 打印请求/响应日志 |
| `MessageChatMemoryAdvisor` | Spring AI 内置 | 加载/保存聊天历史（内存，最多 10 条） |
| `QuestionAnswerAdvisor` | Spring AI 内置 | 去 VectorStore 检索相似文档，注入 prompt |
| `loveAppRagCloudAdvisor` | 自定义配置 | 用 `DashScopeDocumentRetriever` 去阿里云知识库检索 |
| `ReReadingAdvisor` | 自定义 | Re2 重复阅读优化（当前未被使用） |

---

## 十一、配置体系

### 双配置文件

```
application.yml          → 公共配置（git 跟踪）
application-local.yml    → 本地敏感配置（.gitignore 忽略）

spring.profiles.active: local  → 激活 local 配置
```

### application.yml（公共）

```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 1024
        initialize-schema: true
server:
  port: 8123
  servlet:
    context-path: /api
search-api:
  api-key: xxx
```

### application-local.yml（本地，不提交 Git）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yu_ai_agent
    username: postgres
    password: root
  ai:
    dashscope:
      api-key: sk-xxx
      chat:
        options:
          model: deepseek-v4-flash
```

---

## 十二、API 接口一览

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/healthy` | GET | 健康检查 |
| `/api/ai/love_app/chat/sync` | GET | 恋爱大师同步调用 |
| `/api/ai/love_app/chat/sse` | GET | 恋爱大师 SSE 流式 |
| `/api/ai/manus/chat` | GET | 超级智能体 SSE 流式 |
| `/api/swagger-ui.html` | GET | Swagger 文档 |

### 请求示例

**恋爱大师 SSE 流式：**

```
GET http://localhost:8123/api/ai/love_app/chat/sse?message=你好&chatId=love_123456
```

**超级智能体 SSE 流式：**

```
GET http://localhost:8123/api/ai/manus/chat?message=北京明天天气怎么样
```

---

