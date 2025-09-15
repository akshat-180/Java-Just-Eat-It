package com.fooddelivery.repository;

import com.fooddelivery.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByTimestamp(Long sessionId);
    List<ChatMessage> findBySessionIdAndIdGreaterThanOrderByTimestamp(Long sessionId, Long id);
}
