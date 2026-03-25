package com.example.shoppingguide.controller;

import com.example.shoppingguide.agent.DesignAgent;
import com.example.shoppingguide.agent.OrderAgent;
import com.example.shoppingguide.agent.ProductAnalyzerAgent;
import com.example.shoppingguide.agent.RouterAgent;
import com.example.shoppingguide.agent.ShoppingAgent;
import com.example.shoppingguide.domain.ChatLog;
import com.example.shoppingguide.domain.ProductPreference;
import com.example.shoppingguide.repository.ChatLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RouterAgent routerAgent;
    private final ShoppingAgent shoppingAgent;
    private final OrderAgent orderAgent;
    private final ProductAnalyzerAgent analyzerAgent;
    private final DesignAgent designAgent;
    private final ChatLogRepository chatLogRepository;

    public ChatController(RouterAgent routerAgent, ShoppingAgent shoppingAgent,
                          OrderAgent orderAgent, ProductAnalyzerAgent analyzerAgent,
                          DesignAgent designAgent, ChatLogRepository chatLogRepository) {
        this.routerAgent = routerAgent;
        this.shoppingAgent = shoppingAgent;
        this.orderAgent = orderAgent;
        this.analyzerAgent = analyzerAgent;
        this.designAgent = designAgent;
        this.chatLogRepository = chatLogRepository;
    }

    /**
     * ### 1. 基础对话接口 (Chat)
     * 这个接口负责接收用户的聊天信息，进行意图识别（Router Agent），并路由到负责特定功能的 Agent 身上，同时会记录历史会话。
     *
     * - **URL:** `POST http://localhost:8080/api/chat`
     * - **Content-Type:** `application/json`
     *
     * **请求体 (JSON):**
     * ```json
     * {
     *   "sessionId": "user-123",
     *   "message": "我想买一双适合户外跑步的阿迪达斯运动鞋"
     * }
     * ```
     * *(注：`sessionId` 是可选的，如果不传默认会使用 "default-session"。为了让机器记住你的上下文对话，一般需要每次传入相同的 sessionId)*
     *
     * **`curl` 示例:**
     * ```bash
     * curl -X POST http://localhost:8080/api/chat \
     *      -H "Content-Type: application/json" \
     *      -d '{"sessionId":"user-123", "message":"我想买一双适合户外跑步的运动鞋"}'
     * ```
     * @param request
     * @return
     */
    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default-session");
        String message = request.get("message");

        if (message == null || message.trim().isEmpty()) {
            return Map.of("error", "Message cannot be empty");
        }

        // Phase 1: Intent Classification
        String intent = routerAgent.classify(message);
        String response;

        // Phase 2: Route to specific Agent
        if (intent.contains("ORDER")) {
            response = orderAgent.chat(sessionId, message);
        } else {
            // Default to Shopping Agent for RAG / general queries
            response = shoppingAgent.chat(sessionId, message);
        }

        // Phase 3: Persist Conversation history to MySQL
        ChatLog chatLog = new ChatLog(sessionId, message, response);
        chatLogRepository.save(chatLog);

        return Map.of(
                "intent", intent,
                "response", response
        );
    }

    /**
     * ### 2. 偏好分析接口 (Analyze Preference)
     * 利用 AI 结构化输出分析用户输入的一段文字，并提取出对应的产品偏好信息（转化为对应的 `ProductPreference` 对象）。
     *
     * - **URL:** `POST http://localhost:8080/api/chat/analyze-preference`
     * - **Content-Type:** `application/json`
     *
     * **请求体 (JSON):**
     * ```json
     * {
     *   "description": "我平时喜欢穿黑色的衣服，不喜欢带太多图案的，预算大概在500元以内，主要是日常通勤穿。"
     * }
     * ```
     *
     * **`curl` 示例:**
     * ```bash
     * curl -X POST http://localhost:8080/api/chat/analyze-preference \
     *      -H "Content-Type: application/json" \
     *      -d '{"description":"我平时喜欢穿黑色的衣服，不喜欢带太多图案的，预算大概在500元以内，主要是日常通勤穿。"}'
     * ```
     * @param request
     * @return
     */
    @PostMapping("/analyze-preference")
    public ProductPreference analyzePreference(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        return analyzerAgent.analyzePreference(description);
    }

    /**
     * ### 3. 生成商品设计图接口 (Generate Design)
     * 利用 Image Model（例如通义万相、OpenAI Dall-E 等）根据文本描述生成一张设计或展示用的图片。
     *
     * - **URL:** `POST http://localhost:8080/api/chat/design`
     * - **Content-Type:** `application/json`
     *
     * **请求体 (JSON):**
     * ```json
     * {
     *   "description": "一款赛博朋克风格的未来蓝牙耳机，带有霓虹发光线条"
     * }
     * ```
     *
     * **`curl` 示例:**
     * ```bash
     * curl -X POST http://localhost:8080/api/chat/design \
     *      -H "Content-Type: application/json" \
     *      -d '{"description":"一款赛博朋克风格的未来蓝牙耳机，带有霓虹发光线条"}'
     * ```
     * @param request
     * @return
     */
    @PostMapping("/design")
    public Map<String, String> generateDesign(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        String imageUrl = designAgent.generateDesign(description);
        return Map.of("imageUrl", imageUrl);
    }
}
