package com.example.shoppingguide.agent;

import com.example.shoppingguide.config.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ShoppingAgent {
    private static final Logger log = LoggerFactory.getLogger(ShoppingAgent.class);

    private final ChatClient chatClient;

    public ShoppingAgent(ChatClient.Builder builder, VectorStore vectorStore, RedisChatMemory chatMemory,
                         @org.springframework.beans.factory.annotation.Value("classpath:prompts/shopping-agent.st") org.springframework.core.io.Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory, "shopping", 10),
                        new QuestionAnswerAdvisor(vectorStore),
                        new InfoLoggerAdvisor()
                )
                .build();
    }

    public String chat(String sessionId, String userMessage) {
        log.info("🛒 [ShoppingAgent] 正在请求大模型生成导购回复...");
        String response = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
        log.info("🛒 [ShoppingAgent] 导购回复生成完成");
        return response;
    }

    public Flux<String> streamChat(String sessionId, String userMessage) {
        log.info("🛒 [ShoppingAgent] 正在请求大模型生成流式导购回复...");
        return chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .stream()
                .content();
    }
}
