package com.example.shoppingguide.event;

import org.springframework.context.ApplicationEvent;

public class ChatLogEvent extends ApplicationEvent {

    private final String sessionId;
    private final String message;
    private final String response;

    public ChatLogEvent(Object source, String sessionId, String message, String response) {
        super(source);
        this.sessionId = sessionId;
        this.message = message;
        this.response = response;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }
}
