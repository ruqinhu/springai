package com.example.shoppingguide.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class RouterAgent {

    private final ChatClient chatClient;

    public RouterAgent(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are an intent classifier. Your only job is to output exactly one word: " +
                        "'SHOPPING' if the user is asking about or looking for a product to buy, " +
                        "'ORDER' if the user is asking about an existing order's status or details, " +
                        "or 'GENERAL' for greetings or other queries. Do not output anything else.")
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
