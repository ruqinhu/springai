package com.example.shoppingguide.agent;

import com.example.shoppingguide.domain.IntentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RouterAgent {
    private static final Logger log = LoggerFactory.getLogger(RouterAgent.class);

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public RouterAgent(ChatClient.Builder builder,
                       @Value("classpath:prompts/router-agent.st") Resource systemPrompt) {
        this.chatClient = builder
                .defaultAdvisors(new InfoLoggerAdvisor())
                .build();
        this.systemPrompt = systemPrompt;
    }

    public IntentType classify(String userMessage) {
        log.info("🤖 [RouterAgent] 正在分析用户意图大模型调用中...");

        String rawOutput = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        log.info("🤖 [RouterAgent] 大模型原始返回: [{}]", rawOutput);

        // 容错解析：去除引号、花括号、空白等干扰字符
        String cleaned = rawOutput.trim()
                .replaceAll("[\"'{}\\[\\]`]", "")
                .trim()
                .toUpperCase();

        try {
            IntentType intent = IntentType.valueOf(cleaned);
            log.info("🤖 [RouterAgent] 意图分析完成 -> {}", intent);
            return intent;
        } catch (IllegalArgumentException e) {
            log.warn("🤖 [RouterAgent] 无法识别的意图 '{}', 降级为 UNKNOWN", cleaned);
            return IntentType.UNKNOWN;
        }
    }
}
