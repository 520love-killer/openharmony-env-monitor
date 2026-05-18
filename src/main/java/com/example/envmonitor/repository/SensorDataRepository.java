package com.example.envmonitor.repository;

import com.example.envmonitor.entity.SensorData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    Optional<SensorData> findTopByOrderByCreatedAtDesc();

    Optional<SensorData> findTopByDataSourceOrderByCreatedAtDesc(String dataSource);

    List<SensorData> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<SensorData> findByDataSourceOrderByCreatedAtDesc(String dataSource, Pageable pageable);

    List<SensorData> findByDataSourceOrderByCreatedAtDesc(String dataSource);

    List<SensorData> findByStatusOrderByCreatedAtDesc(String status);

    List<SensorData> findByStatusAndDataSourceOrderByCreatedAtDesc(String status, String dataSource);
}
