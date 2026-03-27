package com.example.shoppingguide.agent;

import com.example.shoppingguide.config.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Service
public class OrderAgent {
    private static final Logger log = LoggerFactory.getLogger(OrderAgent.class);

    private final ChatClient chatClient;

    public OrderAgent(ChatClient.Builder builder, RedisChatMemory chatMemory,
                      @org.springframework.beans.factory.annotation.Value("classpath:prompts/order-agent.st") org.springframework.core.io.Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory, "order", 10),
                        new InfoLoggerAdvisor()
                )
                .defaultFunctions("getOrderStatus")
                .build();
    }

    public String chat(String sessionId, String userMessage) {
        log.info("📦 [OrderAgent] 正在请求大模型处理订单查询 (可能涉及函数调用)...");
        String response = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
        log.info("📦 [OrderAgent] 订单处理回复生成完成");
        return response;
    }

    public Flux<String> streamChat(String sessionId, String userMessage) {
        log.info("📦 [OrderAgent] 正在请求大模型流式处理订单查询...");
        return chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .stream()
                .content();
    }
}
