package com.example.envmonitor.repository;

import com.example.envmonitor.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    Optional<SensorData> findTopByOrderByCreatedAtDesc();

    Optional<SensorData> findTopByDataSourceOrderByCreatedAtDesc(String dataSource);

    Optional<SensorData> findTopByDataSourceInOrderByCreatedAtDesc(List<String> dataSources);

    List<SensorData> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<SensorData> findByDataSourceOrderByCreatedAtDesc(String dataSource, Pageable pageable);

    List<SensorData> findByDataSourceInOrderByCreatedAtDesc(List<String> dataSources, Pageable pageable);

    List<SensorData> findByDataSourceOrderByCreatedAtDesc(String dataSource);

    List<SensorData> findByStatusOrderByCreatedAtDesc(String status);

    List<SensorData> findByStatusAndDataSourceOrderByCreatedAtDesc(String status, String dataSource);

    long countByDataSource(String dataSource);

    @Modifying
    @Query("delete from SensorData s where s.dataSource in :sources and s.createdAt < :cutoff")
    int deleteByDataSourceInAndCreatedAtBefore(@Param("sources") List<String> sources, @Param("cutoff") LocalDateTime cutoff);

    @Modifying
    int deleteByDataSourceAndCreatedAtBefore(String dataSource, LocalDateTime cutoff);
}
