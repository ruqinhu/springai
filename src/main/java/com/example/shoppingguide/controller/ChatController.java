package com.example.shoppingguide.controller;

import com.example.shoppingguide.agent.DesignAgent;
import com.example.shoppingguide.agent.ProductAnalyzerAgent;
import com.example.shoppingguide.agent.ShoppingAgent;
import com.example.shoppingguide.domain.ProductPreference;
import com.example.shoppingguide.dto.*;
import com.example.shoppingguide.workflow.ChatWorkflowService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ProductAnalyzerAgent analyzerAgent;
    private final DesignAgent designAgent;
    private final ChatWorkflowService chatWorkflowService;
    private final ShoppingAgent shoppingAgent;

    public ChatController(ProductAnalyzerAgent analyzerAgent,
                          DesignAgent designAgent,
                          ChatWorkflowService chatWorkflowService,
                          ShoppingAgent shoppingAgent) {
        this.analyzerAgent = analyzerAgent;
        this.designAgent = designAgent;
        this.chatWorkflowService = chatWorkflowService;
        this.shoppingAgent = shoppingAgent;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request) {
        log.info("📥 [Controller] /api/chat 收到请求 - Session: {}, Message: {}", request.sessionId(), request.message());
        return chatWorkflowService.runStreamWorkflow(request.sessionId(), request.message());
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("📥 [Controller] /api/chat/stream 收到流式请求 - Session: {}, Message: {}", request.sessionId(), request.message());
        return shoppingAgent.streamChat(request.sessionId(), request.message());
    }

    @PostMapping("/analyze-preference")
    public ProductPreference analyzePreference(@Valid @RequestBody AnalyzePreferenceRequest request) {
        log.info("📥 [Controller] /api/chat/analyze-preference 收到偏好分析请求 - Description: {}", request.description());
        ProductPreference result = analyzerAgent.analyzePreference(request.description());
        log.info("📤 [Controller] /api/chat/analyze-preference 响应完成");
        return result;
    }

    @PostMapping("/design")
    public DesignResponse generateDesign(@Valid @RequestBody DesignRequest request) {
        log.info("📥 [Controller] /api/chat/design 收到设计图生成请求 - Description: {}", request.description());
        String imageUrl = designAgent.generateDesign(request.description());
        log.info("📤 [Controller] /api/chat/design 响应完成 - URL: {}", imageUrl);
        return new DesignResponse(imageUrl);
    }
}
