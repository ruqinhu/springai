package com.example.shoppingguide.agent;

import com.example.shoppingguide.domain.ProductPreference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class ProductAnalyzerAgent {
    private static final Logger log = LoggerFactory.getLogger(ProductAnalyzerAgent.class);

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public ProductAnalyzerAgent(ChatClient.Builder builder,
                                @Value("classpath:prompts/product-analyzer.st") Resource systemPrompt) {
        this.chatClient = builder
                .defaultAdvisors(new InfoLoggerAdvisor())
                .build();
        this.systemPrompt = systemPrompt;
    }

    public ProductPreference analyzePreference(String userMessage) {
        var outputConverter = new BeanOutputConverter<>(ProductPreference.class);
        String format = outputConverter.getFormat();

        log.info("🔍 [ProductAnalyzerAgent] 正在请求大模型进行商品偏好分析...");
        ProductPreference preference = chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                              .param("format", format))
                .user(userMessage)
                .call()
                .entity(outputConverter);
        log.info("🔍 [ProductAnalyzerAgent] 偏好分析完成: {}", preference);
        return preference;
    }
}
