package com.fooddelivery.repository;

import com.fooddelivery.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByRestaurantIdAndExpiresAtAfter(Long restaurantId, LocalDateTime now);
}
