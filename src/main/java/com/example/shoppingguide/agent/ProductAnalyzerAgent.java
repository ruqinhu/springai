package com.example.shoppingguide.agent;

import com.example.shoppingguide.domain.ProductPreference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductAnalyzerAgent {

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public ProductAnalyzerAgent(ChatClient.Builder builder,
                                @Value("classpath:prompts/product-analyzer.st") Resource systemPrompt) {
        this.chatClient = builder.build();
        this.systemPrompt = systemPrompt;
    }

    public ProductPreference analyzePreference(String userMessage) {
        var outputConverter = new BeanOutputConverter<>(ProductPreference.class);
        String format = outputConverter.getFormat();

        return chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                              .param("format", format))
                .user(userMessage)
                .call()
                .entity(outputConverter);
    }
}
