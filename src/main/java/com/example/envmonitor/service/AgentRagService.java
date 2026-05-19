package com.example.envmonitor.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentRagService {
    private static final Logger log = LoggerFactory.getLogger(AgentRagService.class);

    public List<Map<String, String>> searchKnowledge(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        String q = query.toLowerCase();

        String projectRoot = System.getProperty("user.dir");
        List<String> docFiles = List.of(
            "README.md",
            "docs/experiment-report-notes.md",
            "docs/hardware-guide.md",
            "docs/burn-and-run.md",
            "docs/web-platform-guide.md",
            "docs/mqtt-guide.md",
            "docs/agent-extension.md"
        );

        for (String file : docFiles) {
            try {
                Path path = Paths.get(projectRoot, file);
                if (!Files.exists(path)) continue;
                String content = Files.readString(path);
                if (matchesQuery(q, content, file)) {
                    Map<String, String> doc = new LinkedHashMap<>();
                    doc.put("path", file);
                    doc.put("title", extractTitle(file, content));
                    doc.put("snippet", extractSnippet(content, 300));
                    results.add(doc);
                }
            } catch (IOException e) {
                log.debug("Cannot read doc file: {}", file);
            }
        }
        return results;
    }

    private boolean matchesQuery(String query, String content, String filePath) {
        String lower = content.toLowerCase();
        String fileName = filePath.toLowerCase();
        if (query.isEmpty()) return true;
        if (fileName.contains(query)) return true;
        return lower.contains(query);
    }

    private String extractTitle(String path, String content) {
        for (String line : content.lines().toList()) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ") && !trimmed.startsWith("## ")) {
                return trimmed.substring(2).trim();
            }
        }
        return Paths.get(path).getFileName().toString();
    }

    private String extractSnippet(String content, int maxLen) {
        String cleaned = content.replaceAll("[#*`\\[\\]()]", " ").replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= maxLen) return cleaned;
        return cleaned.substring(0, maxLen) + "...";
    }

    public String loadReadme() {
        try {
            Path path = Paths.get(System.getProperty("user.dir"), "README.md");
            if (Files.exists(path)) return Files.readString(path);
        } catch (IOException e) {
            log.debug("Cannot read README.md");
        }
        return "README.md 暂不可用";
    }
}
