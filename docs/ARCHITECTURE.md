# 项目架构总览与防腐规范

本项目是一个企业级的 Spring AI 导购与知识应用引擎。
它的架构设计包含两个核心方面：**领域分层防腐（Layer Constraints）** 与 **物理隔离设计（Physical Isolation）**。

## 1. 领域分层防腐约束 (Domain Layer Constraints)
如同《Harness Engineering》的倡导，项目内的各模块采用自然严格的依赖层级，强制单向依赖，禁止任何跨层逆向调用。这些规则由 `ArchUnit` 机械化保障。

| 逻辑层级 | 包映射 (Packages) | 依赖约束规则 (Dependency Rules) | 职责说明 |
| :--- | :--- | :--- | :--- |
| **Layer 0** | `domain`, `dto`, `exception`, `event` | 绝对不能引用 `Layer 1/2/3` 的内容。 | 领域实体定义，纯 Java 对象或接口。 |
| **Layer 1** | `config`, `repository` | 只能引用 `Layer 0`。 | DB 与各种中间件、拦截器的配置、持久化抽象。 |
| **Layer 2** | `service`, `agent`, `workflow`, `tools` | 只能引用 `Layer 0` 和 `Layer 1`。 | 核心业务流转、大模型 Prompt 调用与 Function Tools 实现。 |
| **Layer 3** | `controller` | 可以引用 `Layer 0-2`，但**严禁**直接操作数据库 (`repository`) 或跳层。 | 处理 HTTP API 接口请求与响应。 |

> [!WARNING]
> 一旦发生跨层调用（例如 Controller 直接 import 了 Repository 接口），`validate.sh` 必然会将流水线截停！

## 2. 核心隔离设计
- **双 VectorStore 隔离**：由于项目中既有针对公共检索的“商品导购”（Products），也有严格保密的“企业知识库”（Knowledge）。故底层必须维持两个异构的 Elasticsearch 索引并由双 `@Bean` `VectorStore` 实现。业务流不可交叉。
- **意图边界明确**：所有请求经由 `RouterAgent` (层级 2) 前置识别出意图（SHOPPING, ORDER, GENERAL）。识别完毕后必须将工作流交由专职 Agent 负责。

## 3. 对话与短历史设计
- 项目采用 Redis `MessageChatMemoryAdvisor` 或关联拦截器确保 Agent 服务无状态化，保证后续可横向扩展。
