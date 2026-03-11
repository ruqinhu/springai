package com.example.shoppingguide.agent;

import com.example.shoppingguide.config.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

@Service
public class OrderAgent {

    private final ChatClient chatClient;

    public OrderAgent(ChatClient.Builder builder, RedisChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem("You are a helpful customer service agent. " +
                        "You help users look up their order status using the provided tools. " +
                        "Be polite and reassure the customer about their order.")
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
