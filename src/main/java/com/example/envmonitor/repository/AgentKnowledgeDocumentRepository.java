package com.example.envmonitor.repository;

import com.example.envmonitor.entity.AgentKnowledgeDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentKnowledgeDocumentRepository extends JpaRepository<AgentKnowledgeDocument, Long> {
    Optional<AgentKnowledgeDocument> findByPath(String path);
    List<AgentKnowledgeDocument> findByType(String type);
}
