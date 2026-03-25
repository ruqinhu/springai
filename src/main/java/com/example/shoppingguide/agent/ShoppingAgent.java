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

    public ShoppingAgent(ChatClient.Builder builder, VectorStore vectorStore, RedisChatMemory chatMemory,
                         @org.springframework.beans.factory.annotation.Value("classpath:prompts/shopping-agent.st") org.springframework.core.io.Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
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
