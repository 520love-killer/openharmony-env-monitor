package com.example.envmonitor.repository;

import com.example.envmonitor.entity.AgentConversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentConversationRepository extends JpaRepository<AgentConversation, Long> {
    Optional<AgentConversation> findBySessionId(String sessionId);
    List<AgentConversation> findAllByOrderByUpdatedAtDesc();
    void deleteBySessionId(String sessionId);
}
