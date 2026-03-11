package com.example.shoppingguide.controller;

import com.example.shoppingguide.agent.OrderAgent;
import com.example.shoppingguide.agent.RouterAgent;
import com.example.shoppingguide.agent.ShoppingAgent;
import com.example.shoppingguide.domain.ChatLog;
import com.example.shoppingguide.repository.ChatLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RouterAgent routerAgent;
    private final ShoppingAgent shoppingAgent;
    private final OrderAgent orderAgent;
    private final ChatLogRepository chatLogRepository;

    public ChatController(RouterAgent routerAgent, ShoppingAgent shoppingAgent,
                          OrderAgent orderAgent, ChatLogRepository chatLogRepository) {
        this.routerAgent = routerAgent;
        this.shoppingAgent = shoppingAgent;
        this.orderAgent = orderAgent;
        this.chatLogRepository = chatLogRepository;
    }

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
}
