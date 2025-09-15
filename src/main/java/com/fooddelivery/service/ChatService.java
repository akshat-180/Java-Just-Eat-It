package com.fooddelivery.service;

import com.fooddelivery.model.ChatMessage;
import com.fooddelivery.model.ChatSession;
import com.fooddelivery.repository.ChatMessageRepository;
import com.fooddelivery.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    public ChatService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
    }

    @Transactional
    public ChatSession createSession(String username, Long restaurantId, int deliveryMinutes) {
        int chatMinutes = Math.max(1, deliveryMinutes - 2); // chat length = delivery - 2 (min 1)
        ChatSession s = ChatSession.builder()
                .username(username)
                .restaurantId(restaurantId)
                .expiresAt(LocalDateTime.now().plusMinutes(chatMinutes))
                .build();
        return sessionRepo.save(s);
    }

    public Optional<ChatSession> findSession(Long id) {
        return sessionRepo.findById(id);
    }

    public List<ChatSession> activeSessionsForRestaurant(Long restaurantId) {
        return sessionRepo.findByRestaurantIdAndExpiresAtAfter(restaurantId, LocalDateTime.now());
    }

    @Transactional
    public ChatMessage sendMessage(Long sessionId, String fromUser, String toUser, String message) {
        ChatSession s = sessionRepo.findById(sessionId).orElseThrow(() -> new IllegalStateException("session not found"));
        if (s.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("chat expired");
        }
        ChatMessage m = ChatMessage.builder()
                .sessionId(sessionId)
                .fromUser(fromUser)
                .toUser(toUser)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return messageRepo.save(m);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> history(Long sessionId) {
        return messageRepo.findBySessionIdOrderByTimestamp(sessionId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> historyAfter(Long sessionId, Long afterId) {
        return messageRepo.findBySessionIdAndIdGreaterThanOrderByTimestamp(sessionId, afterId);
    }
}
