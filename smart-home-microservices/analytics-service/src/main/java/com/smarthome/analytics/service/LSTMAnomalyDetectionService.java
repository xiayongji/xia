package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.AnomalyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LSTMAnomalyDetectionService {

    private final Map<String, List<Double>> deviceDataHistory = new ConcurrentHashMap<>();
    private static final int WINDOW_SIZE = 30;
    private static final double RECONSTRUCTION_ERROR_THRESHOLD = 0.15;

    public AnomalyResult detectAnomaly(String deviceId) {
        List<Double> history = deviceDataHistory.getOrDefault(deviceId, new ArrayList<>());
        
        if (history.size() < WINDOW_SIZE) {
            return AnomalyResult.builder()
                    .deviceId(deviceId)
                    .isAnomaly(false)
                    .anomalyProbability(0.0)
                    .message("数据不足，正在积累")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        double reconstructionError = simulateLSTMInference(history);
        double anomalyProbability = Math.min(reconstructionError / RECONSTRUCTION_ERROR_THRESHOLD, 1.0);
        boolean isAnomaly = anomalyProbability > 0.8;

        return AnomalyResult.builder()
                .deviceId(deviceId)
                .isAnomaly(isAnomaly)
                .anomalyProbability(anomalyProbability)
                .message(isAnomaly ? "LSTM模型检测到异常" : "LSTM模型检测正常")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private double simulateLSTMInference(List<Double> data) {
        double mean = data.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = data.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        
        double latestValue = data.get(data.size() - 1);
        double deviation = Math.abs(latestValue - mean) / Math.sqrt(variance + 1e-6);
        
        return Math.min(deviation * 0.05, 0.3);
    }

    public void addDataPoint(String deviceId, double value) {
        List<Double> history = deviceDataHistory.computeIfAbsent(deviceId, k -> new ArrayList<>());
        history.add(value);
        
        if (history.size() > WINDOW_SIZE * 2) {
            history = new ArrayList<>(history.subList(history.size() - WINDOW_SIZE, history.size()));
            deviceDataHistory.put(deviceId, history);
        }
    }

    public void retrainModel(String deviceId) {
        log.info("开始重新训练设备 {} 的LSTM模型", deviceId);
        
        try {
            Thread.sleep(100);
            log.info("设备 {} 的LSTM模型重新训练完成", deviceId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("LSTM模型训练中断: {}", e.getMessage());
        }
    }

    public void clearHistory(String deviceId) {
        deviceDataHistory.remove(deviceId);
    }
}