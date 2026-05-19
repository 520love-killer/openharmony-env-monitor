package com.example.envmonitor.service;

import com.example.envmonitor.entity.SensorData;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SerialIngestionService {
    private final SensorDataService sensorDataService;
    private final boolean enabled;
    private final String portName;
    private final int baudRate;
    private final int reconnectDelayMs;
    private final AtomicLong receivedLines = new AtomicLong();
    private final AtomicLong savedRecords = new AtomicLong();

    private volatile boolean running;
    private volatile boolean connected;
    private volatile String lastRawMessage;
    private volatile String lastSavedMessage;
    private volatile String lastError;
    private volatile LocalDateTime lastReceivedAt;
    private volatile LocalDateTime lastSavedAt;
    private Thread workerThread;

    public SerialIngestionService(
        SensorDataService sensorDataService,
        @Value("${app.serial.enabled:false}") boolean enabled,
        @Value("${app.serial.port-name:COM21}") String portName,
        @Value("${app.serial.baud-rate:115200}") int baudRate,
        @Value("${app.serial.reconnect-delay-ms:3000}") int reconnectDelayMs
    ) {
        this.sensorDataService = sensorDataService;
        this.enabled = enabled;
        this.portName = portName;
        this.baudRate = baudRate;
        this.reconnectDelayMs = reconnectDelayMs;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            lastError = "serial ingestion disabled";
            return;
        }
        running = true;
        workerThread = new Thread(this::runLoop, "hi3861-serial-ingestion");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("connected", connected);
        result.put("portName", portName);
        result.put("baudRate", baudRate);
        result.put("reconnectDelayMs", reconnectDelayMs);
        result.put("receivedLines", receivedLines.get());
        result.put("savedRecords", savedRecords.get());
        result.put("lastRawMessage", lastRawMessage);
        result.put("lastSavedMessage", lastSavedMessage);
        result.put("lastReceivedAt", lastReceivedAt);
        result.put("lastSavedAt", lastSavedAt);
        result.put("lastError", lastError);
        result.put("availablePorts", Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).toList());
        return result;
    }

    private void runLoop() {
        while (running) {
            SerialPort serialPort = SerialPort.getCommPort(portName);
            try {
                serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
                if (!serialPort.openPort()) {
                    connected = false;
                    lastError = "failed to open serial port " + portName;
                    sleepQuietly();
                    continue;
                }
                connected = true;
                lastError = null;
                readLines(serialPort);
            } catch (Exception ex) {
                connected = false;
                lastError = ex.getMessage();
                System.out.println("[Serial] read failed: " + ex.getMessage());
            } finally {
                connected = false;
                if (serialPort.isOpen()) {
                    serialPort.closePort();
                }
            }
            sleepQuietly();
        }
    }

    private void readLines(SerialPort serialPort) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), StandardCharsets.UTF_8))) {
            while (running && serialPort.isOpen()) {
                String line;
                try {
                    line = reader.readLine();
                } catch (SerialPortTimeoutException ex) {
                    continue;
                }
                if (line == null) {
                    continue;
                }
                handleLine(line.trim());
            }
        }
    }

    private void handleLine(String line) {
        if (!StringUtils.hasText(line)) {
            return;
        }
        receivedLines.incrementAndGet();
        lastRawMessage = line;
        lastReceivedAt = LocalDateTime.now();
        sensorDataService.createFromSerialMessage(line).ifPresent(this::markSaved);
    }

    private void markSaved(SensorData data) {
        savedRecords.incrementAndGet();
        lastSavedMessage = data.getRawMessage();
        lastSavedAt = data.getCreatedAt();
        System.out.println("[Serial] sensor data saved, id=" + data.getId() + ", source=" + data.getDataSource());
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(reconnectDelayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
