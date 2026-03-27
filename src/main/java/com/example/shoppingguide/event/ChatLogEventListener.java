package com.example.shoppingguide.event;

import com.example.shoppingguide.domain.ChatLog;
import com.example.shoppingguide.repository.ChatLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ChatLogEventListener {
    private static final Logger log = LoggerFactory.getLogger(ChatLogEventListener.class);

    private final ChatLogRepository chatLogRepository;

    public ChatLogEventListener(ChatLogRepository chatLogRepository) {
        this.chatLogRepository = chatLogRepository;
    }

    @Async
    @EventListener
    public void handleChatLogEvent(ChatLogEvent event) {
        log.info("📝 [AsyncEventListener] 收到异步日志事件 - Session: {}", event.getSessionId());
        ChatLog chatLog = new ChatLog(event.getSessionId(), event.getMessage(), event.getResponse());
        chatLogRepository.save(chatLog);
        log.info("📝 [AsyncEventListener] 聊天日志已持久化到 MySQL - Session: {}", event.getSessionId());
    }
}
