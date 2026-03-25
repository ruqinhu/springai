package com.example.shoppingguide.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class RouterAgent {

    private final ChatClient chatClient;

    public RouterAgent(ChatClient.Builder builder,
                       @org.springframework.beans.factory.annotation.Value("classpath:prompts/router-agent.st") org.springframework.core.io.Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .build();
    }

    public String classify(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content()
                .trim()
                .toUpperCase();
    }
}
