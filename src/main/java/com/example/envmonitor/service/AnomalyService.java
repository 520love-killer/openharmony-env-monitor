package com.example.envmonitor.service;

import com.example.envmonitor.dto.AnomalyItem;
import com.example.envmonitor.dto.AnomalyResponse;
import com.example.envmonitor.entity.AnomalyRecord;
import com.example.envmonitor.entity.SensorData;
import com.example.envmonitor.repository.AnomalyRecordRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnomalyService {
    private final AnalyticsService analyticsService;
    private final SensorDataService sensorDataService;
    private final AnomalyRecordRepository anomalyRecordRepository;

    public AnomalyService(
        AnalyticsService analyticsService,
        SensorDataService sensorDataService,
        AnomalyRecordRepository anomalyRecordRepository
    ) {
        this.analyticsService = analyticsService;
        this.sensorDataService = sensorDataService;
        this.anomalyRecordRepository = anomalyRecordRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "anomalyCache", key = "#source + '_' + #limit")
    public AnomalyResponse detect(String source, int limit) {
        String dataSource = sensorDataService.normalizeSource(source);
        List<SensorData> data = analyticsService.chronological(dataSource, limit);
        if (data.size() < 5) {
            return new AnomalyResponse(false, "真实数据不足，至少需要 5 条数据", dataSource, data.size(),
                false, 0, List.of());
        }
        List<AnomalyItem> items = detectItems(data);
        return new AnomalyResponse(true,
            items.isEmpty() ? "未检测到明显异常" : "检测到 " + items.size() + " 个异常风险",
            dataSource, data.size(), !items.isEmpty(), items.size(), items);
    }

    @Transactional
    public int saveDetectedAnomalies(String source, int limit) {
        String dataSource = sensorDataService.normalizeSource(source);
        List<SensorData> data = analyticsService.chronological(dataSource, limit);
        if (data.size() < 5) {
            return 0;
        }
        int saved = 0;
        for (AnomalyItem item : detectItems(data)) {
            boolean exists = anomalyRecordRepository.existsBySourceAndTypeAndRelatedSensorDataIdAndDataTime(
                dataSource, item.type(), item.relatedSensorDataId(), item.time());
            if (!exists) {
                AnomalyRecord record = new AnomalyRecord();
                record.setSource(dataSource);
                record.setType(item.type());
                record.setLevel(item.level());
                record.setReason(item.reason());
                record.setSuggestion(item.suggestion());
                record.setRelatedSensorDataId(item.relatedSensorDataId());
                record.setDataTime(item.time());
                anomalyRecordRepository.save(record);
                saved++;
            }
        }
        return saved;
    }

    private List<AnomalyItem> detectItems(List<SensorData> data) {
        List<AnomalyItem> items = new ArrayList<>();
        for (SensorData current : data) {
            threshold(current, items);
        }
        for (int i = 1; i < data.size(); i++) {
            SensorData previous = data.get(i - 1);
            SensorData current = data.get(i);
            jump(previous, current, items);
        }
        rising(data, items);
        stuck(data, items);
        return items;
    }

    private void threshold(SensorData data, List<AnomalyItem> items) {
        if (data.getTemperature() >= 40) {
            items.add(item("TEMP_THRESHOLD", data, "HIGH", "温度超过 40℃", "温度过高，建议检查环境温度、通风或制冷设备。"));
        }
        if (data.getHumidity() >= 80) {
            items.add(item("HUMIDITY_THRESHOLD", data, "MEDIUM", "湿度超过 80%", "湿度过高，建议开启除湿或通风。"));
        }
        if (data.getGas() >= 50) {
            items.add(item("GAS_THRESHOLD", data, "HIGH", "燃气浓度超过 50 ppm", "建议立即通风，远离火源，检查燃气源。"));
        }
    }

    private void jump(SensorData previous, SensorData current, List<AnomalyItem> items) {
        if (Math.abs(current.getTemperature() - previous.getTemperature()) >= 3.0) {
            items.add(item("TEMP_JUMP", current, "MEDIUM", "相邻两条温度突变超过 3℃", "建议检查传感器读数是否稳定。"));
        }
        if (Math.abs(current.getHumidity() - previous.getHumidity()) >= 15.0) {
            items.add(item("HUMIDITY_JUMP", current, "MEDIUM", "相邻两条湿度突变超过 15%", "建议检查湿度传感器与环境扰动。"));
        }
        if (Math.abs(current.getGas() - previous.getGas()) >= 20.0) {
            items.add(item("GAS_JUMP", current, "HIGH", "相邻两条燃气浓度突增超过 20 ppm", "建议立即通风并排查燃气源。"));
        }
    }

    private void rising(List<SensorData> data, List<AnomalyItem> items) {
        if (data.size() < 5) {
            return;
        }
        List<SensorData> recent = data.subList(data.size() - 5, data.size());
        if (strictlyIncreasing(recent.stream().map(SensorData::getTemperature).toList())) {
            SensorData last = recent.get(recent.size() - 1);
            items.add(item("TEMPERATURE_RISING", last, "MEDIUM", "最近连续 5 条温度上升", "温度持续上升，建议观察是否存在热源或制冷不足。"));
        }
        if (strictlyIncreasing(recent.stream().map(SensorData::getGas).toList())) {
            SensorData last = recent.get(recent.size() - 1);
            items.add(item("GAS_RISING", last, "MEDIUM", "最近连续 5 条燃气浓度上升", "燃气浓度连续升高，建议持续观察并保持通风。"));
        }
    }

    private void stuck(List<SensorData> data, List<AnomalyItem> items) {
        if (data.size() < 10) {
            return;
        }
        List<SensorData> recent = data.subList(data.size() - 10, data.size());
        SensorData last = recent.get(recent.size() - 1);
        if (range(recent.stream().map(SensorData::getTemperature).toList()) < 0.1) {
            items.add(item("TEMP_STUCK", last, "LOW", "最近 10 条温度几乎无变化", "温度长期无变化，建议检查传感器是否正常刷新。"));
        }
        if (range(recent.stream().map(SensorData::getHumidity).toList()) < 0.2) {
            items.add(item("HUMIDITY_STUCK", last, "LOW", "最近 10 条湿度几乎无变化", "湿度长期无变化，建议检查传感器是否正常刷新。"));
        }
        if (range(recent.stream().map(SensorData::getGas).toList()) < 0.5) {
            items.add(item("GAS_STUCK", last, "LOW", "最近 10 条燃气浓度几乎无变化", "燃气读数长期无变化，建议检查传感器是否正常刷新。"));
        }
    }

    private boolean strictlyIncreasing(List<Double> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) <= values.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    private double range(List<Double> values) {
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        return max - min;
    }

    private AnomalyItem item(String type, SensorData data, String level, String reason, String suggestion) {
        return new AnomalyItem(type, data.getCreatedAt(), level, reason, suggestion, data.getId());
    }
}
