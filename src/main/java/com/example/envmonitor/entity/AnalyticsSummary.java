package com.example.envmonitor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_summary")
public class AnalyticsSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private Integer sampleCount;

    private Double temperatureAvg;
    private Double temperatureMax;
    private Double temperatureMin;
    private Double temperatureStd;
    private Double humidityAvg;
    private Double humidityMax;
    private Double humidityMin;
    private Double humidityStd;
    private Double gasAvg;
    private Double gasMax;
    private Double gasMin;
    private Double gasStd;
    private Double temperatureChangeRate;
    private Double humidityChangeRate;
    private Double gasChangeRate;
    private String volatilityLevel;
    private String trend;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public Double getTemperatureAvg() { return temperatureAvg; }
    public void setTemperatureAvg(Double temperatureAvg) { this.temperatureAvg = temperatureAvg; }
    public Double getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(Double temperatureMax) { this.temperatureMax = temperatureMax; }
    public Double getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(Double temperatureMin) { this.temperatureMin = temperatureMin; }
    public Double getTemperatureStd() { return temperatureStd; }
    public void setTemperatureStd(Double temperatureStd) { this.temperatureStd = temperatureStd; }
    public Double getHumidityAvg() { return humidityAvg; }
    public void setHumidityAvg(Double humidityAvg) { this.humidityAvg = humidityAvg; }
    public Double getHumidityMax() { return humidityMax; }
    public void setHumidityMax(Double humidityMax) { this.humidityMax = humidityMax; }
    public Double getHumidityMin() { return humidityMin; }
    public void setHumidityMin(Double humidityMin) { this.humidityMin = humidityMin; }
    public Double getHumidityStd() { return humidityStd; }
    public void setHumidityStd(Double humidityStd) { this.humidityStd = humidityStd; }
    public Double getGasAvg() { return gasAvg; }
    public void setGasAvg(Double gasAvg) { this.gasAvg = gasAvg; }
    public Double getGasMax() { return gasMax; }
    public void setGasMax(Double gasMax) { this.gasMax = gasMax; }
    public Double getGasMin() { return gasMin; }
    public void setGasMin(Double gasMin) { this.gasMin = gasMin; }
    public Double getGasStd() { return gasStd; }
    public void setGasStd(Double gasStd) { this.gasStd = gasStd; }
    public Double getTemperatureChangeRate() { return temperatureChangeRate; }
    public void setTemperatureChangeRate(Double temperatureChangeRate) { this.temperatureChangeRate = temperatureChangeRate; }
    public Double getHumidityChangeRate() { return humidityChangeRate; }
    public void setHumidityChangeRate(Double humidityChangeRate) { this.humidityChangeRate = humidityChangeRate; }
    public Double getGasChangeRate() { return gasChangeRate; }
    public void setGasChangeRate(Double gasChangeRate) { this.gasChangeRate = gasChangeRate; }
    public String getVolatilityLevel() { return volatilityLevel; }
    public void setVolatilityLevel(String volatilityLevel) { this.volatilityLevel = volatilityLevel; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
