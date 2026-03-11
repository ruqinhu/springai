package com.example.shoppingguide.repository;

import com.example.shoppingguide.domain.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findBySessionIdOrderByTimestampAsc(String sessionId);
}
