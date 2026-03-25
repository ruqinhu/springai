
### 1. 基础对话接口 (Chat)
这个接口负责接收用户的聊天信息，进行意图识别（Router Agent），并路由到负责特定功能的 Agent 身上，同时会记录历史会话。

- **URL:** `POST http://localhost:8080/api/chat`
- **Content-Type:** `application/json`

**请求体 (JSON):**
```json
{
  "sessionId": "user-123",
  "message": "我想买一双适合户外跑步的阿迪达斯运动鞋"
}
```
*(注：`sessionId` 是可选的，如果不传默认会使用 "default-session"。为了让机器记住你的上下文对话，一般需要每次传入相同的 sessionId)*

**`curl` 示例:**
```bash
curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"sessionId":"user-123", "message":"我想买一双适合户外跑步的运动鞋"}'
```

---

### 2. 偏好分析接口 (Analyze Preference)
利用 AI 结构化输出分析用户输入的一段文字，并提取出对应的产品偏好信息（转化为对应的 `ProductPreference` 对象）。

- **URL:** `POST http://localhost:8080/api/chat/analyze-preference`
- **Content-Type:** `application/json`

**请求体 (JSON):**
```json
{
  "description": "我平时喜欢穿黑色的衣服，不喜欢带太多图案的，预算大概在500元以内，主要是日常通勤穿。"
}
```

**`curl` 示例:**
```bash
curl -X POST http://localhost:8080/api/chat/analyze-preference \
     -H "Content-Type: application/json" \
     -d '{"description":"我平时喜欢穿黑色的衣服，不喜欢带太多图案的，预算大概在500元以内，主要是日常通勤穿。"}'
```

---

### 3. 生成商品设计图接口 (Generate Design)
利用 Image Model（例如通义万相、OpenAI Dall-E 等）根据文本描述生成一张设计或展示用的图片。

- **URL:** `POST http://localhost:8080/api/chat/design`
- **Content-Type:** `application/json`

**请求体 (JSON):**
```json
{
  "description": "一款赛博朋克风格的未来蓝牙耳机，带有霓虹发光线条"
}
```

**`curl` 示例:**
```bash
curl -X POST http://localhost:8080/api/chat/design \
     -H "Content-Type: application/json" \
     -d '{"description":"一款赛博朋克风格的未来蓝牙耳机，带有霓虹发光线条"}'
```

# 最长调用链 (Longest Call Chain)

当用户查询订单状态时，会触发整个系统中最长的完整调用链（包含两次大模型调用、一次数据库查询、一次日志保存）。

```mermaid
sequenceDiagram
    autonumber
    participant User as 用户 (User)
    participant ChatController as 聊天控制器 (ChatController)
    participant RouterAgent as 意图识别 Agent (RouterAgent)
    participant OrderAgent as 订单支持 Agent (OrderAgent)
    participant LLM as 大语言模型 (Spring AI / DashScope)
    participant OrderServiceTool as 订单服务工具 (OrderServiceTool)
    participant DB as MySQL 数据库 (ProductOrderRepository)

    User->>ChatController: POST /api/chat {message: "帮我查一下订单，单号是ORD123，我是Alice"}
    
    %% Phase 1: Intent Classification
    Note over ChatController,RouterAgent: 阶段 1：意图识别
    ChatController->>RouterAgent: classify(message)
    RouterAgent->>LLM: prompt(SystemPrompt + message)
    LLM-->>RouterAgent: 返回 "ORDER" 字符串
    RouterAgent-->>ChatController: 返回意图 "ORDER"

    %% Phase 2: Order Processing with Tool Call
    Note over ChatController,OrderAgent: 阶段 2：路由至 OrderAgent
    ChatController->>OrderAgent: chat(sessionId, message)
    OrderAgent->>LLM: prompt(附加 RedisChatMemory + getOrderStatus 函数)
    
    %% Tool call negotiation
    Note over LLM,DB: 大模型决定调用工具
    LLM-->>OrderAgent: Function Call 请求 (getOrderStatus, {orderId="ORD123", customerName="Alice"})
    OrderAgent->>OrderServiceTool: getOrderStatus.apply(request)
    
    OrderServiceTool->>DB: findByOrderIdAndCustomerName("ORD123", "Alice")
    DB-->>OrderServiceTool: 返回 ProductOrder 实体 / Optional
    OrderServiceTool-->>OrderAgent: 返回 OrderResponse
    
    OrderAgent->>LLM: 提供 Function Call 结果
    LLM-->>OrderAgent: 生成最终润色后的回复 (如 "您的订单状态是...")
    OrderAgent-->>ChatController: 返回处理结果
    
    %% Phase 3: Persist Log
    Note over ChatController,DB: 阶段 3：聊天日志持久化
    ChatController->>DB: chatLogRepository.save(ChatLog)
    DB-->>ChatController: 实体保存成功
    
    ChatController-->>User: 返回 {intent: "ORDER", response: "您的订单..."}
```


