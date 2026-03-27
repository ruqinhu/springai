package com.example.shoppingguide.controller;

import com.example.shoppingguide.agent.KnowledgeAgent;
import com.example.shoppingguide.service.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

    private final KnowledgeService knowledgeService;
    private final KnowledgeAgent knowledgeAgent;

    public KnowledgeController(KnowledgeService knowledgeService, KnowledgeAgent knowledgeAgent) {
        this.knowledgeService = knowledgeService;
        this.knowledgeAgent = knowledgeAgent;
    }

    /**
     * 流式询问大模型 + 知识库搜索 (Agentic RAG)
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithKnowledge(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default-session");
        String message = request.get("message");
        log.info("📥 [KnowledgeController] /api/knowledge/chat 接收到请求 - message={}", message);

        return knowledgeAgent.streamChat(sessionId, message);
    }

    /**
     * 批量上传文档并向量化存储
     * 支持格式: PDF, DOCX, PPTX, TXT, HTML, CSV, Excel 等 (Tika 支持)
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadDocuments(@RequestParam("files") MultipartFile[] files) {
        log.info("📥 [KnowledgeController] /api/knowledge/upload 收到 {} 个文件", files.length);
        return knowledgeService.uploadAndVectorize(files);
    }

    /**
     * 向量相似度搜索测试接口
     */
    @GetMapping("/search")
    public List<Map<String, Object>> searchKnowledge(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK) {
        log.info("📥 [KnowledgeController] /api/knowledge/search 收到搜索请求 - query: {}, topK: {}", query, topK);
        return knowledgeService.searchSimilar(query, topK);
    }
}
