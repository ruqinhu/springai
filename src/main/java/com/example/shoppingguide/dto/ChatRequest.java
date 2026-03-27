package com.example.shoppingguide.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String sessionId,
        @NotBlank(message = "Message cannot be empty") String message
) {
    public ChatRequest {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "default-session";
        }
    }
}
