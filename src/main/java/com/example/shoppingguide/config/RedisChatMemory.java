package com.example.shoppingguide.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisChatMemory implements ChatMemory {
    private static final Logger log = LoggerFactory.getLogger(RedisChatMemory.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String PREFIX = "chat_memory:";
    private static final long EXPIRE_HOURS = 24;

    public RedisChatMemory(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    record SerializableMessage(String type, String content) {}

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = PREFIX + conversationId;
        log.info("🧠 [RedisChatMemory] 写入记忆 - Key: {}, 新增消息数: {}", key, messages.size());
        List<Message> existing = get(conversationId, 100);
        existing.addAll(messages);

        List<SerializableMessage> serializableMessages = existing.stream().map(m -> {
            if (m instanceof UserMessage) return new SerializableMessage("user", m.getText());
            if (m instanceof AssistantMessage) return new SerializableMessage("assistant", m.getText());
            if (m instanceof SystemMessage) return new SerializableMessage("system", m.getText());
            return new SerializableMessage("unknown", m.getText());
        }).toList();

        try {
            String json = objectMapper.writeValueAsString(serializableMessages);
            redisTemplate.opsForValue().set(key, json, EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("🧠 [RedisChatMemory] 记忆已写入 Redis - Key: {}, 总消息数: {}", key, serializableMessages.size());
        } catch (JsonProcessingException e) {
            log.error("🧠 [RedisChatMemory] 序列化记忆失败 - Key: {}", key, e);
            throw new RuntimeException("Error serializing chat memory", e);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = PREFIX + conversationId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            log.info("🧠 [RedisChatMemory] 读取记忆 - Key: {} 不存在，返回空列表", key);
            return new ArrayList<>();
        }

        try {
            List<SerializableMessage> dtoList = objectMapper.readValue(json, new TypeReference<>() {});
            List<Message> allMessages = dtoList.stream().map(dto -> {
                String type = dto.type();
                String content = dto.content();
                if ("user".equals(type)) {
                    return new UserMessage(content);
                } else if ("assistant".equals(type)) {
                    return new AssistantMessage(content);
                } else if ("system".equals(type)) {
                    return new SystemMessage(content);
                } else {
                    return new UserMessage(content);
                }
            }).collect(Collectors.toList());

            if (allMessages.size() > lastN) {
                log.info("🧠 [RedisChatMemory] 读取记忆 - Key: {}, 总量: {}, 截取最近 {} 条", key, allMessages.size(), lastN);
                return new ArrayList<>(allMessages.subList(allMessages.size() - lastN, allMessages.size()));
            }
            log.info("🧠 [RedisChatMemory] 读取记忆 - Key: {}, 返回 {} 条消息", key, allMessages.size());
            return allMessages;
        } catch (JsonProcessingException e) {
            log.error("🧠 [RedisChatMemory] 反序列化记忆失败 - Key: {}", key, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        String key = PREFIX + conversationId;
        log.info("🧠 [RedisChatMemory] 清除记忆 - Key: {}", key);
        redisTemplate.delete(key);
    }
}
