package com.example.shoppingguide.agent;

import com.example.shoppingguide.config.RedisChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class KnowledgeAgent {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeAgent.class);

    private final ChatClient chatClient;

    public KnowledgeAgent(ChatClient.Builder builder, 
                          @Qualifier("knowledgeVectorStore") VectorStore knowledgeVectorStore,
                          RedisChatMemory chatMemory,
                          @Value("classpath:prompts/knowledge-agent.st") Resource systemPrompt) {
        
        log.info("🤖 [KnowledgeAgent] 正在初始化私有知识库引擎...");
        
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory, "knowledge", 10),
                        new QuestionAnswerAdvisor(knowledgeVectorStore),
                        new InfoLoggerAdvisor()
                )
                .build();
    }

    /**
     * 流式询问企业知识库，结合 RAG 和大模型润色输出
     */
    public Flux<String> streamChat(String sessionId, String userMessage) {
        log.info("🗄️ [KnowledgeAgent] 开始向大模型发起私有知识库 (RAG) 增强检索请求... 用户标识: {}, 问题: {}", sessionId, userMessage);
        return chatClient.prompt()
                .user(userMessage)
                .advisors(new InfoLoggerAdvisor())
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .stream()
                .content()
                .doOnComplete(() -> log.info("🗄️ [KnowledgeAgent] ✅ 大模型流式总结完毕，RAG 检索请求处理成功"))
                .doOnError(e -> log.error("🗄️ [KnowledgeAgent] ❌ 知识库大模型调用发生异常", e));
    }
}
