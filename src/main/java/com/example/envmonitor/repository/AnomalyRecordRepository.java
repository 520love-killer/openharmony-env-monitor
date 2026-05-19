package com.example.envmonitor.repository;

import com.example.envmonitor.entity.AnomalyRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface AnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long> {
    List<AnomalyRecord> findBySourceOrderByCreatedAtDesc(String source, Pageable pageable);

    Optional<AnomalyRecord> findTopByOrderByCreatedAtDesc();

    Optional<AnomalyRecord> findTopBySourceOrderByCreatedAtDesc(String source);

    boolean existsBySourceAndTypeAndRelatedSensorDataIdAndDataTime(String source, String type, Long relatedSensorDataId, LocalDateTime dataTime);

    @Modifying
    int deleteByCreatedAtBefore(LocalDateTime cutoff);
}
