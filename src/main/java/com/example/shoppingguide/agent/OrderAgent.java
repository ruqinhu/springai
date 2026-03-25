package com.example.shoppingguide.agent;

import com.example.shoppingguide.config.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

@Service
public class OrderAgent {

    private final ChatClient chatClient;

    public OrderAgent(ChatClient.Builder builder, RedisChatMemory chatMemory,
                      @org.springframework.beans.factory.annotation.Value("classpath:prompts/order-agent.st") org.springframework.core.io.Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory, "order", 10))
                .defaultFunctions("getOrderStatus")
                .build();
    }

    public String chat(String sessionId, String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
    }
}
