# 核心功能与中间件集成架构总结

本项目是一个集成了向量检索、长短期记忆、意图路由及多模态生成的企业级 AI 应用。以下是核心功能与背后支撑的中间件及其使用方式。

## 1. 核心功能概览

| 功能模块 | 业务价值 | 实现细节 |
| :--- | :--- | :--- |
| **智能导购 (Shopping)** | 提升转化率 | 基于 `RouterAgent` 进行毫秒级意图识别，支持商品推荐与流式应答。 |
| **隔离知识库 (RAG)** | 企业级文档管理 | 自动解析 PDF/Word，将非结构化数据转化为向量，仅基于企业内部文档回复。 |
| **自动化物流助手** | 减少人工客服 | 通过 `Function Calling` 实时捕获后端物流系统状态，实现查单自动化。 |
| **设计辅助 (DALL-E)** | 视觉化呈现 | 输入产品描述，实时生成多张 1024x1024 高清设计草图。 |

## 2. 核心中间件及使用方式

### 🍃 Elasticsearch (向量索引库)
*   **使用方式**：作为核心向量搜索引擎，支撑 RAG。
*   **架构设计**：采用**双索引隔离架构**。
    *   `products` 索引：存放公开商品数据。
    *   `knowledge` 索引：存放企业内部加密/敏感文档。
*   **技术栈**：通过 `VectorStore` 接口实现，利用 cosine 相似度进行 Top-K 检索。

### 🔴 Redis (会话状态中心)
*   **使用方式**：存储用户的对话状态（ChatMessage）。
*   **架构设计**：基于 `MessageChatMemoryAdvisor`。
    *   每个 `sessionId` 对应 Redis 中的一个 List。
    *   AI 在每次对话前会自动回放最近 10 轮的历史记录，确保语境连贯。

### 📄 Apache Tika (文档解析引擎)
*   **使用方式**：集成在 `KnowledgeService` 中。
*   **技术细节**：支持对上传的 MultipartFile 进行二进制流解析。
    *   **自动识别**：能识别 .pdf, .docx, .txt 等多种 MIME 类型。
    *   **提取注入**：将文档文本提取后，配合 `TokenTextSplitter` 动态分片，并自动挂载 `source` 文件名元数据。

### 🐬 MySQL (结构化关系数据库)
*   **使用方式**：存储基础业务模型。
*   **架构设计**：作为商品原始数据、订单状态数据的持久化层，AI 通过 JPA 实体和 Function Calling 与其进行数据交互。

## 3. 关键交互模式

### 🔄 Advisor 拦截器模式
*   **InfoLoggerAdvisor**：自定义实现 `CallAroundAdvisor` 和 `StreamAroundAdvisor`。在 LLM 调用前后（Around）进行拦截，实现 100% 的输入输出透明化。
*   **QuestionAnswerAdvisor**：Spring AI 标准 RAG 组件，在用户提问时自动触发向量库搜索并注入提示词。

### 📡 Server-Sent Events (SSE)
*   **使用方式**：全链路采用 `MediaType.TEXT_EVENT_STREAM_VALUE`。
*   **业务价值**：消除长时间等待大模型生成的焦虑感，实现像 ChatGPT 一样的流式逐字输出。

---
> [!IMPORTANT]
> **架构安全性**：知识库与导购库的物理隔离（双 VectorStore Bean 声明）是本项目企业化落地的核心壁垒，有效防止了敏感文档在导购过程中泄露。
