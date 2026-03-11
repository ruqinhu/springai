package com.example.shoppingguide.agent;

import com.example.shoppingguide.config.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ShoppingAgent {

    private final ChatClient chatClient;

    public ShoppingAgent(ChatClient.Builder builder, VectorStore vectorStore, RedisChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem("You are a helpful and polite shopping assistant. " +
                        "Use the provided product information to answer user questions. " +
                        "If you don't know the answer, say you don't know.")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory, "shopping", 10),
                        new QuestionAnswerAdvisor(vectorStore)
                )
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
