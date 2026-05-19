package com.example.envmonitor.repository;

import com.example.envmonitor.entity.AnalyticsSummary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, Long> {
    List<AnalyticsSummary> findBySourceOrderByCreatedAtDesc(String source, Pageable pageable);

    Optional<AnalyticsSummary> findTopByOrderByCreatedAtDesc();

    Optional<AnalyticsSummary> findTopBySourceOrderByCreatedAtDesc(String source);

    @Modifying
    int deleteByCreatedAtBefore(LocalDateTime cutoff);
}
