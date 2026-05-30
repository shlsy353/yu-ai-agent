# 项目结构完整解析
# 本项目为学习项目，在老师原有的项目上进行修改，并添加了 RAG 模型、工具调用（Function Calling）等功能，并完善了代码注释。

> **项目名称**：yu-ai-agent（AI 恋爱顾问）
> **技术栈**：Spring Boot 3.5.14 + Spring AI 1.1.x + 阿里云百炼 DashScope
> **底层模型**：DeepSeek-v4-flash（阿里云百炼部署）
> **构建工具**：Maven + JDK 21
> **数据库**：PostgreSQL + PGVector（向量检索）
> **文档**：Swagger (Knife4j) — http://localhost:8123/api/swagger-ui.html

---

## 目录

1. [项目全景图](#一项目全景图)
2. [文件结构速览](#二文件结构速览)
3. [核心业务流程详解](#三核心业务流程详解)
4. [工具调用系统（Function Calling）](#四工具调用系统function-calling)
5. [RAG 管线全解析](#五rag-管线全解析)
6. [三套向量数据库对比](#六三套向量数据库对比)
7. [Advisor 拦截器链](#七advisor-拦截器链)
8. [聊天记忆机制](#八聊天记忆机制)
9. [配置体系](#九配置体系)
10. [学习路线：从裸调到业务](#十学习路线从裸调到业务)
11. [关键概念速查](#十一关键概念速查)
12. [常见问题 FAQ](#十二常见问题-faq)

---

## 一、项目全景图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         LoveApp (业务入口)                                │
│  @Component                                                              │
│  ┌──────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │ doChat() │  │doChatWith    │  │doChatWith│  │recommend │  │doChat  │ │
│  │ 普通聊天 │  │Report()      │  │Rag()     │  │Partner() │  │With    │ │
│  │          │  │ 恋爱报告     │  │ 知识库   │  │ 推荐对象 │  │Tools() │ │
│  └────┬─────┘  └──────┬───────┘  └────┬────┘  └────┬─────┘  │ 工具调用 │
│       │               │               │           │         └────┬────┘ │
│       └────────┬──────┴───────┬───────┴───────────┘              │      │
│                │              │                                  │      │
│       ┌────────▼──────────────▼──────────────────────────┐       │      │
│       │              ChatClient (核心聊天客户端)            │       │      │
│       │  builder(dashscopeChatModel)                      │       │      │
│       │  .defaultSystem("恋爱顾问提示词")                   │       │      │
│       │  .defaultAdvisors(MyLoggerAdvisor,                │       │      │
│       │                MessageChatMemoryAdvisor)          │       │      │
│       └────────────────────┬─────────────────────────────┘       │      │
│                            │                                      │      │
└────────────────────────────┼──────────────────────────────────────┼──────┘
                             │                                      │
                             ▼                                      ▼
                    ┌──────────────────┐              ┌──────────────────────┐
                    │  DashScope API    │              │  6 个 Tool 工具      │
                    │  (DeepSeek 模型)   │              │  联网搜索 / 网页抓取  │
                    └──────────────────┘              │  文件读写 / 下载      │
                                                      │  PDF生成 / 终端命令   │
                                                      └──────────────────────┘

    ┌────────────────── 三条外部数据通道 ──────────────────┐
    │                                                      │
    ▼                                                      ▼
┌─────────────────┐  ┌──────────────────────┐  ┌──────────────────┐
│ SimpleVectorStore│  │DashScopeDocument      │  │ PgVectorStore     │
│ (内存向量库)     │  │Retriever             │  │ (PostgreSQL持久化) │
│                 │  │ (阿里云知识库)        │  │                   │
│ recommendPartner│  │ doChatWithRag         │  │ 测试中, 未接入    │
│ 专用            │  │ 专用                 │  │ 业务              │
└───────┬─────────┘  └──────────────────────┘  └──────────────────┘
        │
        │  启动时加载
        ▼
┌──────────────────────────────────────────┐
│  document/*.md (4 个 Markdown 知识文件)   │
│   → LoveAppDocumentLoader 读取           │
│   → MyKeywordEnricher 自动加关键词       │
│   → 存入 SimpleVectorStore              │
└──────────────────────────────────────────┘
```

---

## 二、文件结构速览

```
yu-ai-agent/
├── pom.xml                                          # 依赖总控
│
├── src/main/java/com/tripo/yuaiagent/
│   ├── YuAiAgentApplication.java                    # ★ 启动类
│   │
│   ├── app/
│   │   └── LoveApp.java                             # ★★★ 核心业务 (5个功能)
│   │
│   ├── tools/                                       # ★★ 工具调用（新增）
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
│   │   ├── MyTokenTextSplitter.java                 #   文本分割器 (分词)
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
│   │   └── HealthyController.java                   #   GET /api/healthy
│   │
│   └── demo/invoke/                                 # 四个逐步进阶的调用示例
│       ├── HttpAiInvoke.java                        #   HTTP 裸调 (Hutool)
│       ├── SdkAiInvoke.java                         #   DashScope SDK 调用
│       ├── SpringAiInvoke.java                      #   Spring AI ChatModel
│       └── TestApiKey.java                          #   API Key 定义
│
├── src/main/resources/
│   ├── application.yml                              # 主配置 (pgvector, 端口, Swagger)
│   ├── application-local.yml                        # 本地配置 (DB, API Key)
│   └── document/                                    # ★ 知识库原始数据
│       ├── 恋爱常见问题和回答 - 恋爱篇.md
│       ├── 恋爱常见问题和回答 - 单身篇.md
│       ├── 恋爱常见问题和回答 - 已婚篇.md
│       └── 恋爱对象库.md
│
└── src/test/java/com/tripo/yuaiagent/
    ├── app/LoveAppTest.java                         # ★ 集成测试 (5个测试方法)
    ├── tools/                                       # ★★ 工具单元测试（新增）
    │   ├── WebSearchToolTest.java
    │   ├── WebScrapingToolTest.java
    │   ├── FileOperationToolTest.java
    │   ├── ResourceDownloadToolTest.java
    │   ├── PDFGenerationToolTest.java
    │   └── TerminalOperationToolTest.java
    ├── rag/LoveAppDocumentLoaderTest.java           # 文档加载测试
    └── rag/PgVectorVectorStoreConfigTest.java       # PGVector 测试
```

---

## 三、核心业务流程详解

### 流程 1：doChat() —— 带记忆的普通聊天

```
你: "我叫小王"
  │
  ▼
LoveApp.doChat("我叫小王", chatId)
  │
  │  chatClient.prompt().user("我叫小王")
  │     .advisors(spec -> spec
  │         .param("conversation_id", chatId)
  │         .param("chat_memory_retrieve_size", 10))
  │     .call()
  │
  ├─ [Advisor 1] MyLoggerAdvisor.before()
  │   打印: request = ...
  │
  ├─ [Advisor 2] MessageChatMemoryAdvisor
  │   从内存中读取 chatId 对应的最近 10 条历史消息
  │   把历史消息注入到 prompt 的 SystemMessage 里
  │
  ├─ [API] ChatModel.call(prompt)
  │   请求阿里云百炼 → DeepSeek 模型
  │   模型看到: "历史消息: ... \n 用户: 我叫小王"
  │
  ├─ [Advisor 2] MessageChatMemoryAdvisor
  │   把这次对话保存到内存
  │
  ├─ [Advisor 1] MyLoggerAdvisor.after()
  │   打印: response = ...
  │
  └─ 返回: "你好小王！我是你的恋爱顾问..."
```

**关键点**：
- `MessageChatMemoryAdvisor` 是 Spring AI 内置的，不是自己写的
- 聊天记录存在**内存**里，重启就没了
- `chatId` 用来区分不同用户，实现多用户隔离

---

### 流程 2：doChatWithReport() —— 生成结构化报告

```java
public LoveReport doChatWithReport(String message, String chatId) {
    LoveReport loveReport = chatClient.prompt()
        .system(SYSTEM_PROMPT + "只返回 JSON 格式数据...")
        .user(message)
        .call()
        .entity(LoveReport.class);    // ← 自动解析 JSON 成 Java 对象
    return loveReport;
}
```

**关键点**：
- `.entity(LoveReport.class)` 让 Spring AI 自动把模型的 JSON 输出转成 Java 对象
- `jsonschema-generator` 依赖就是干这个的——它把 `LoveReport` 的结构告诉模型，让模型按格式输出

---

### 流程 3：doChatWithRag() —— 基于知识库问答 ★★★

```
你: "婚后关系不太亲密怎么办？"
  │
  ▼
LoveApp.doChatWithRag("婚后关系不太亲密怎么办？", chatId)
  │
  ├─ 第 1 步：查询重写
  │   queryRewriter.doQueryRewrite("婚后关系不太亲密怎么办？")
  │   → 用 AI 把用户问题改写得更容易检索
  │   → 返回: "婚后如何改善夫妻关系？如何增进亲密感？"
  │
  ├─ 第 2 步：用重写后的查询调用 ChatClient
  │   chatClient.prompt().user("婚后如何改善夫妻关系？...")
  │
  ├─ [Advisor 1] MyLoggerAdvisor            ← 日志
  │
  ├─ [Advisor 2] MessageChatMemoryAdvisor   ← 历史记忆
  │
  ├─ [Advisor 3] loveAppRagCloudAdvisor     ← ★ RAG 核心！
  │   │
  │   ├─ DashScopeDocumentRetriever.retrieve()
  │   │   → 去阿里云百炼上的"恋爱大师"知识库搜索相关文档
  │   │   → 返回相关片段
  │   │
  │   └─ RetrievalAugmentationAdvisor
  │       → 把搜索到的片段拼接到 prompt 里
  │       → "基于以下资料回答问题：\n[资料1]...\n[资料2]..."
  │
  ├─ [API] ChatModel.call(prompt)
  │   模型看到: "资料:... 问题:婚后如何改善..."
  │
  └─ 返回: "婚后关系冷淡是常见问题，建议你们..."
```

**为什么叫 RAG（检索增强生成）？**
> 不 RAG：模型只凭自己的训练数据回答 → 容易胡编乱造（幻觉）
> RAG：先查资料，再让模型基于查到的资料回答 → 答案更准确、可追溯

---

### 流程 4：recommendPartner() —— 基于向量库推荐

```
你: "请你给我推荐一个25岁的程序员，我不考虑星座"
  │
  ▼
LoveApp.recommendPartner("请你给我推荐一个25岁的程序员...")
  │
  ├─ [Advisor 1] MyLoggerAdvisor
  │
  ├─ [Advisor 2] QuestionAnswerAdvisor
  │   │
  │   ├─ loveAppVectorStore.similaritySearch("25岁程序员")
  │   │   → 把用户问题转成向量（1024 个小数）
  │   │   → 在向量库里找最相似的 4 个文档
  │   │   → 返回 "张三：25岁程序员，单身，喜欢..."
  │   │
  │   └─ 把查到的资料注入 prompt
  │
  ├─ [API] ChatModel.call(prompt)
  │   模型看到: "资料:张三:25岁程序员... 问题:推荐25岁程序员"
  │
  └─ 返回: "我为你推荐张三，他25岁..."
```

---

### 流程 5：doChatWithTools() —— 工具调用（Function Calling）★★★

```
你: "周末想去上海约会，推荐小众打卡地"
  │
  ▼
LoveApp.doChatWithTools("周末想带女朋友去上海约会...", chatId)
  │
  ├─ 第 1 步：AI 决定要调用什么工具
  │   DeepSeek 模型分析用户意图
  │   → "用户想去上海约会，需要查找小众打卡地"
  │   → 决定调用 WebSearchTool.searchWeb("上海小众约会打卡地推荐")
  │
  ├─ 第 2 步：执行工具
  │   框架自动调用对应的 Java 方法
  │   → WebSearchTool.searchWeb("上海小众约会打卡地推荐")
  │   → 发起 HTTP 请求到 searchapi.io，搜索百度
  │   → 返回搜索结果（JSON 格式）
  │
  ├─ 第 3 步：工具结果返回给 AI
  │   模型看到: "搜索结果：田子坊、1933老场坊..."
  │   → 模型根据搜索结果生成自然语言回复
  │
  └─ 返回: "推荐上海几个小众约会圣地：1. 田子坊..."

---

更复杂的工具调用链路（多个工具协同）：

```
你: "下载一张星空情侣壁纸，保存到本地"
  │
  ├─ AI 分析: 需要"下载图片"
  │   → 先调用 WebSearchTool.searchWeb("星空情侣壁纸")
  │   → 搜索结果里有图片 URL
  │
  ├─ AI 再决定: 需要"保存图片到本地"
  │   → 调用 ResourceDownloadTool.downloadResource(url, "星空壁纸.jpg")
  │   → 文件保存到 ./tmp/download/星空壁纸.jpg
  │
  └─ 返回: "已经下载好了，保存在 ./tmp/download/ 目录下"

---

你: "生成一份七夕约会计划 PDF"
  │
  ├─ AI 分析: 需要"生成 PDF 文件"
  │   → 先自己构思内容（餐厅预订、活动流程、礼物清单...）
  │   → 调用 PDFGenerationTool.generatePDF("七夕约会计划.pdf", content)
  │   → PDF 保存到 ./tmp/pdf/七夕约会计划.pdf
  │
  └─ 返回: "已经生成好了，内容包含..." + PDF 下载路径
```

**工具调用的本质**：AI 模型自己决定"我现在需要执行哪个函数来获取信息或完成操作"，而不是人写死的固定流程。

---

## 四、工具调用系统（Function Calling）

### 什么是 Function Calling？

Function Calling（函数调用）让 AI 模型能主动调用外部工具：

```
传统 Chat：用户 → AI（只能动嘴说）
工具调用： 用户 → AI（决定需要什么工具）→ 执行 Java 方法 → 结果返回 AI → AI 回复用户
```

### 6 个注册工具一览

所有工具在 `ToolRegistration.java` 中统一注册为 `ToolCallback[]` Bean，然后注入到 `LoveApp`。

| 工具 | 类名 | @Tool 方法 | 能力 | 依赖 |
|------|------|-----------|------|------|
| **联网搜索** | `WebSearchTool` | `searchWeb(query)` | 通过 searchapi.io 调用百度搜索 | searchapi.io API Key |
| **网页抓取** | `WebScrapingTool` | `scrapeWebPage(url)` | 爬取指定 URL 的 HTML 内容 | Jsoup |
| **文件读写** | `FileOperationTool` | `readFile(fileName)` / `writeFile(fileName, content)` | 读取/写入文件到 `./tmp/file/` | Hutool |
| **资源下载** | `ResourceDownloadTool` | `downloadResource(url, fileName)` | 下载网络资源到 `./tmp/download/` | Hutool |
| **PDF 生成** | `PDFGenerationTool` | `generatePDF(fileName, content)` | 生成 PDF 文件到 `./tmp/pdf/` | iText |
| **终端命令** | `TerminalOperationTool` | `executeTerminalCommand(command)` | 执行 cmd.exe 命令 | 无 |

### 工具注册机制

```java
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

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

**关键点**：Spring AI 中工具注册有两种方式：
- `tools(Object...)` — 传入普通对象，框架自动扫描 @Tool 注解（用于构建时）
- `toolCallbacks(ToolCallback...)` — 传入已经构建好的 ToolCallback 实例（用于调用时）

`LoveApp.doChatWithTools()` 中调用 `.toolCallbacks(allTools)`，因为 `allTools` 已经是 `ToolCallback[]`。

### 文件保存目录

所有文件操作（读写、下载、PDF）统一保存在：

```java
FileConstant.File_SAVE_DIR = System.getProperty("user.dir") + "/tmp"
```

即项目根目录下的 `tmp/` 文件夹，按工具类型分子目录：

```
项目根目录/
  └── tmp/
      ├── file/        ← FileOperationTool 读写
      ├── download/    ← ResourceDownloadTool 下载
      └── pdf/         ← PDFGenerationTool 生成
```

---

## 五、RAG 管线全解析

### 什么是 RAG？

RAG = Retrieval-Augmented Generation = **检索增强生成**

```
传统 AI 对话：
  用户问 → 模型凭记忆回答 → 可能胡说八道
                            ↑
                      这叫"幻觉"（hallucination）

RAG 对话：
  用户问 → 先去知识库查资料 → 把资料+问题一起给模型 → 模型基于资料回答
                                                      ↑
                                            答案有据可查，不易胡说
```

### 本项目中的 RAG 管线组件

| 组件 | 类名 | 作用 | 类比 |
|------|------|------|------|
| **文档加载** | `LoveAppDocumentLoader` | 从 `document/*.md` 读取原始文本 | 去图书馆借书 |
| **文本分割** | `MyTokenTextSplitter` | 把长文档切成小块（token 级别） | 把书拆成章节 |
| **关键词增强** | `MyKeywordEnricher` | AI 自动给每段加关键词标签 | 给章节贴标签 |
| **查询重写** | `QueryRewriter` | 把用户问题改写得更利于搜索 | "我想要..." → "推荐条件:..." |
| **向量检索** | `VectorStore` | 把问题和文档都转成向量，找最相似的 | 在图书馆按主题找书 |
| **结果注入** | `Advisor` | 把查到的资料塞进 prompt 给模型 | 把书放在桌上让 AI 参考 |

### 完整的 RAG 链路

```
                      ┌─────────────┐
                      │  用户提问    │
                      └──────┬──────┘
                             │
                             ▼
                      ┌─────────────┐
                      │ 查询重写     │ ← QueryRewriter
                      │ "改善关系" → │   用 AI 改写问题
                      │ "婚后亲密技巧"│
                      └──────┬──────┘
                             │
                             ▼
                      ┌─────────────┐
                      │  向量检索    │ ← VectorStore
                      │ 找最相似文档  │   把问题变成向量去匹配
                      └──────┬──────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
             ┌────────────┐   ┌────────────┐
             │ 原始文档     │   │ 重写后的查询 │
             │(检索到的知识) │   │            │
             └──────┬─────┘   └──────┬─────┘
                    │                 │
                    └──────┬──────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  组装 Prompt  │ ← Advisor
                    │ "基于以下资料  │   把资料拼进问题
                    │  回答问题..." │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  AI 模型回答  │ ← DashScope API
                    │  基于资料生成  │
                    └──────────────┘
```

---

## 六、三套向量数据库对比

这个项目里有三套完全不同的知识库方案。它们互不干扰，各自服务于不同的功能：

| 对比维度 | SimpleVectorStore | 阿里云百炼知识库 | PGVector |
|----------|------------------|----------------|----------|
| **配置类** | `LoveAppVectorStoreConfig` | `LoveAppRagCloudAdvisorConfig` | `PgVectorVectorStoreConfig` |
| **Bean 名** | `loveAppVectorStore` | `loveAppRagCloudAdvisor` | `pgVectorVectorStore` |
| **存储位置** | Java 内存，重启消失 | 阿里云控制台上传 | PostgreSQL 数据库表 |
| **数据来源** | 自动加载 `document/*.md` | 手动去阿里云后台传文件 | 通过 Java 代码 INSERT |
| **检索方式** | `QuestionAnswerAdvisor` 自动调 | `DashScopeDocumentRetriever` | `similaritySearch()` |
| **目前谁在用** | `recommendPartner()` | `doChatWithRag()` | 只有测试类在用 |
| **适用场景** | 快速原型、数据量小 | 生产级、数据在云上 | 数据量大、需持久化 |

### 为什么要有三套？

这是从简单到复杂的**学习轨迹**：

1. **SimpleVectorStore（内存）** → 最简单，不用装任何东西，适合入门
2. **阿里云知识库** → 商业方案，不用自己管理数据库，但花钱
3. **PGVector** → 自建持久化向量库，完全掌控数据

老师带你走这条路线，是为了让你理解不同方案的取舍。

---

## 七、Advisor 拦截器链

### 什么是 Advisor？

Advisor = Spring AI 里的**拦截器**。在发消息给 AI 之前、收到 AI 回复之后，插入自定义逻辑。

```
用户请求 → [Advisor 1] → [Advisor 2] → [Advisor 3] → AI 模型
用户响应 ← [Advisor 1] ← [Advisor 2] ← [Advisor 3] ← AI 模型
```

### 本项目的 Advisor 链

```
每次 ChatClient 调用，Advisor 按顺序执行：

 1. MyLoggerAdvisor.before()     ← 打印请求日志
 2. MessageChatMemoryAdvisor     ← 加载聊天历史
 3. (功能 Advisor)               ← QuestionAnswerAdvisor / loveAppRagCloudAdvisor
 4. [调用 AI 模型]
 5. MessageChatMemoryAdvisor     ← 保存聊天历史
 6. MyLoggerAdvisor.after()      ← 打印响应日志
```

### 各 Advisor 详解

| Advisor | 类型 | 作用 |
|---------|------|------|
| `MyLoggerAdvisor` | 自定义 | 在 before 打印请求、after 打印响应 |
| `MessageChatMemoryAdvisor` | Spring AI 内置 | 在 before 把历史消息注入 prompt，在 after 保存本轮对话 |
| `QuestionAnswerAdvisor` | Spring AI 内置 | 在 before 去 VectorStore 检索相似文档，注入 prompt |
| `loveAppRagCloudAdvisor` | 自定义配置 | 用 `DashScopeDocumentRetriever` 去阿里云知识库检索 |
| `ReReadingAdvisor` | 自定义 | 把问题重复一遍（Re2 模式），让模型更关注问题本身（**当前未被使用**） |

---

## 八、聊天记忆机制

### 当前使用的记忆方式

```java
// LoveApp.java 构造函数
ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .maxMessages(10)           // 只记住最近 10 条消息
    .build();
```

- 存在**内存**中
- 按 `chatId` 隔离不同用户
- 最多 10 条，超出自动丢弃最旧的
- 重启后丢失

### FileBasedChatMemory（已定义但未启用）

```java
public class FileBasedChatMemory implements ChatMemory {
    // 用 Kryo 序列化把聊天记录存到文件
    // 每个 chatId 对应一个 .kryo 文件
}
```

这相当于"升级版"——把聊天记录存到硬盘文件，重启不丢失。但目前 `LoveApp` 没有用这个类，它是留给后续扩展的。

---

## 九、配置体系

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
        index-type: HNSW          # 向量索引算法
        distance-type: COSINE_DISTANCE  # 余弦相似度
        dimensions: 1024          # 向量维度（和模型匹配）
        initialize-schema: true   # 启动自动建表
server:
  port: 8123
  servlet:
    context-path: /api            # 接口前缀

search-api:
  api-key: xxx                    # searchapi.io 的 API Key（联网搜索用）
```

### application-local.yml（本地）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yu_ai_agent
    username: postgres
    password: root
  ai:
    dashscope:
      api-key: sk-xxx             # 阿里云百炼 API Key
      chat:
        options:
          model: deepseek-v4-flash  # 阿里云上的 DeepSeek 模型
```

### @SpringBootApplication(exclude = ...)

```java
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
```

这行排除了 pgvector 的自动配置。为什么？因为 `PgVectorVectorStoreConfig` 已经手动创建了 `pgVectorVectorStore` 这个 Bean，如果保留自动配置，Spring 会再创建一个同名的，导致冲突。

---

## 十、学习路线：从裸调到业务

```
第 1 层：HttpAiInvoke                    HTTP + Hutool 手写请求
  │      "原来调用 AI 就是在发 HTTP 请求"
  │
  ▼
第 2 层：SdkAiInvoke                     DashScope SDK 封装
  │      "SDK 帮我处理了 HTTP 细节"
  │
  ▼
第 3 层：SpringAiInvoke                  Spring AI 的 ChatModel
  │      "Spring AI 统一了不同厂商的接口"
  │
  ▼
第 4 层：LoveApp (当前)                   ChatClient 编排业务流程
  │      "加记忆、加 RAG、加工具调用"
  │
  ▼
第 5 层（下一步）：REST 接口              @RestController 暴露给前端
  │      "把 LoveApp 方法变成 HTTP 接口"
  │
  ▼
第 6 层（未来）：前端页面                 Vue / React / 小程序
         "真正的用户界面"
```

每一层都在前一层的**基础上封装复杂性**。你现在在第 4 层，理解了第 4 层就自然明白前面各层为什么存在。

---

## 十一、关键概念速查

| 概念 | 一句话解释 | 类比 |
|------|-----------|------|
| **ChatModel** | 大模型的 Java 接口 | 打电话给 AI 的电话机 |
| **ChatClient** | 比 ChatModel 更好用的高级 API | 带自动拨号、录音的电话 |
| **Prompt** | 发给模型的指令 | 你跟 AI 说的话 |
| **Advisor** | 拦截器，在请求前后插入逻辑 | 电话的自动应答机 |
| **VectorStore** | 存语义向量的数据库 | 按意思找书，不是按标题 |
| **Embedding** | 把文本转成一串数字（向量） | 给每本书算一个"指纹" |
| **相似度搜索** | 找向量最接近的文档 | 指纹最像的那本书 |
| **RAG** | 先查资料再回答 | 开卷考试 vs 闭卷考试 |
| **Function Calling** | AI 决定调用哪个 Java 方法 | AI 会"动手"而不是只动嘴 |
| **Tool** | AI 可以调用的外部功能（如搜索、写文件） | AI 的"工具包" |
| **Token** | AI 理解的最小单位（≈0.75 个汉字） | 积木的最小颗粒 |
| **Query Rewriting** | 把用户问题改得更容易检索 | 把模糊问题改清晰 |

---

## 十二、常见问题 FAQ

### Q：启动报错"不支持发行版本 21"怎么办？
A：你的 JDK 版本低于 21。项目要求 JDK 21。两种方案：
- 安装 JDK 21 并把系统 `JAVA_HOME` 指向它
- 在终端临时切换：PowerShell 执行 `$env:JAVA_HOME = "你的JDK21路径"`

### Q：启动报错 `No @Tool annotated methods found` 是什么原因？
A：`LoveApp.doChatWithTools()` 中用了 `.tools(allTools)`，但 `allTools` 已经是 `ToolCallback[]` 类型，应该用 `.toolCallbacks(allTools)`。Spring AI 1.1.x 中这两个方法用途不同：
- `tools(Object...)` — 传入普通对象，框架自动扫描 `@Tool` 注解
- `toolCallbacks(ToolCallback...)` — 传入已经构建好的回调实例

### Q：我可以换成 DeepSeek 官方 API Key 吗？
A：不能直接换。你当前用的是阿里云百炼的 API Key（`spring.ai.dashscope.api-key`），底层的 DeepSeek 模型也是部署在阿里云上的。想用 DeepSeek 官方 API 需要：
1. 把 Maven 依赖从 `spring-ai-alibaba-starter-dashscope` 换成对应的 OpenAI 兼容 starter
2. 修改配置指向 `api.deepseek.com`
3. RAG 知识库（阿里云百炼独有功能）需要替换方案

简单说：**只换 key 不行，要换整套集成方案。**

### Q：为什么有时启动报错说找不到 Bean？
A：最常见的原因是 `PgVectorVectorStoreConfig` 里的 bean 名和 `LoveApp` 里的 `@Resource` 字段名对不上。`@Resource` 先按**字段名**匹配 Bean，匹配不上就按**类型**匹配，如果有多个同类型就报错。

### Q：聊天记录存在哪里？
A：目前存在应用内存里，重启就没了。`FileBasedChatMemory` 是文件持久化版本，但没有启用。

### Q：为什么有 MyLoggerAdvisor 又在 yml 里配置了 SimpleLoggerAdvisor？
A：yml 里的 `SimpleLoggerAdvisor` 配置被注释掉了（`application.yml` 第 44 行），实际用的是自定义的 `MyLoggerAdvisor`。

### Q：document/ 里的 markdown 文件怎么用？
A：`LoveAppDocumentLoader` 在启动时通过 `@Resource` 被 `LoveAppVectorStoreConfig` 调用，把文件内容加载到 `SimpleVectorStore` 里供 `recommendPartner()` 使用。

### Q：PgVectorVectorStore 为什么没有用在任何业务方法里？
A：目前还处于测试阶段。`PgVectorVectorStoreConfigTest` 验证了它能正常工作，但还没有业务方法接入。这是留给后续扩展的"基础设施"。

### Q：文件保存到哪里了？
A：所有工具生成的文件统一保存在项目根目录的 `tmp/` 下：
- `./tmp/file/` — 文件读写操作
- `./tmp/download/` — 资源下载
- `./tmp/pdf/` — PDF 生成

路径定义在 `FileConstant.File_SAVE_DIR = System.getProperty("user.dir") + "/tmp"`。

### Q：联网搜索需要什么配置？
A：需要在 `application-local.yml` 中配置 `search-api.api-key`，这是 searchapi.io 的 API Key。不配的话联网搜索工具会报错。