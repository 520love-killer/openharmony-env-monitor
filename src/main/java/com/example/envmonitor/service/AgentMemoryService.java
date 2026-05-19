package com.example.envmonitor.service;

import com.example.envmonitor.entity.AgentConversation;
import com.example.envmonitor.entity.AgentMessage;
import com.example.envmonitor.repository.AgentConversationRepository;
import com.example.envmonitor.repository.AgentMessageRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentMemoryService {
    private final AgentConversationRepository conversationRepository;
    private final AgentMessageRepository messageRepository;

    public AgentMemoryService(
        AgentConversationRepository conversationRepository,
        AgentMessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public String ensureSession(String sessionId, String firstMessage) {
        if (sessionId != null && !sessionId.isBlank()) {
            var existing = conversationRepository.findBySessionId(sessionId);
            if (existing.isPresent()) return sessionId;
        }
        String newId = UUID.randomUUID().toString().substring(0, 8);
        AgentConversation conv = new AgentConversation();
        conv.setSessionId(newId);
        String title = firstMessage;
        if (title.length() > 50) title = title.substring(0, 50) + "...";
        conv.setTitle(title);
        conversationRepository.save(conv);
        return newId;
    }

    @Transactional
    public void saveMessage(String sessionId, String role, String content, List<String> usedTools, String dataSource, String confidence) {
        AgentMessage msg = new AgentMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setUsedTools(usedTools != null ? String.join(",", usedTools) : null);
        msg.setDataSource(dataSource);
        msg.setConfidence(confidence);
        messageRepository.save(msg);

        conversationRepository.findBySessionId(sessionId).ifPresent(conv -> {
            conv.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conv);
        });
    }

    public List<Map<String, Object>> getSessions() {
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (AgentConversation conv : conversationRepository.findAllByOrderByUpdatedAtDesc()) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("sessionId", conv.getSessionId());
            s.put("title", conv.getTitle());
            s.put("createdAt", conv.getCreatedAt() != null ? conv.getCreatedAt().toString() : null);
            s.put("updatedAt", conv.getUpdatedAt() != null ? conv.getUpdatedAt().toString() : null);
            sessions.add(s);
        }
        return sessions;
    }

    public List<Map<String, Object>> getSessionMessages(String sessionId) {
        List<Map<String, Object>> msgs = new ArrayList<>();
        for (AgentMessage msg : messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            m.put("usedTools", msg.getUsedTools());
            m.put("dataSource", msg.getDataSource());
            m.put("confidence", msg.getConfidence());
            m.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
            msgs.add(m);
        }
        return msgs;
    }

    @Transactional
    public void deleteSession(String sessionId) {
        messageRepository.deleteBySessionId(sessionId);
        conversationRepository.deleteBySessionId(sessionId);
    }

    public List<Map<String, String>> getRecentConversation(String sessionId, int limit) {
        List<AgentMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<Map<String, String>> history = new ArrayList<>();
        int start = Math.max(0, messages.size() - limit);
        for (int i = start; i < messages.size(); i++) {
            AgentMessage msg = messages.get(i);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            history.add(m);
        }
        return history;
    }
}
