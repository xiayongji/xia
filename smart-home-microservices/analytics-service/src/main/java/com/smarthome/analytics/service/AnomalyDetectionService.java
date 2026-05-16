package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.AnomalyDetection;
import com.smarthome.analytics.repository.AnomalyDetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final AnomalyDetectionRepository anomalyRepository;
    
    @Value("${analytics.anomaly.threshold:0.95}")
    private Double anomalyThreshold;

    /**
     * 检测设备异常
     */
    @Transactional
    public AnomalyDetection detectAnomaly(String deviceId, String deviceName, 
                                         String metricType, Double value, 
                                         Double normalMin, Double normalMax) {
        String anomalyType = null;
        String details = null;
        Double confidence = 0.0;
        
        if (value < normalMin || value > normalMax) {
            anomalyType = "out_of_range";
            confidence = calculateConfidence(value, normalMin, normalMax);
            details = String.format("检测到%s异常: 当前值=%.2f, 正常范围=[%.2f, %.2f]", 
                                  metricType, value, normalMin, normalMax);
        }
        
        if (anomalyType != null && confidence >= anomalyThreshold) {
            AnomalyDetection anomaly = new AnomalyDetection();
            anomaly.setDeviceId(deviceId);
            anomaly.setDeviceName(deviceName);
            anomaly.setAnomalyType(anomalyType);
            anomaly.setConfidence(confidence);
            anomaly.setDetails(details);
            anomaly.setStatus("pending");
            anomaly.setDetectedAt(LocalDateTime.now());
            
            AnomalyDetection saved = anomalyRepository.save(anomaly);
            log.warn("检测到设备异常: {} - {} - 置信度: {}", deviceId, anomalyType, confidence);
            return saved;
        }
        
        return null;
    }

    /**
     * 使用统计方法检测异常
     */
    @Transactional
    public AnomalyDetection detectAnomalyWithStatistics(String deviceId, String deviceName,
                                                      List<Double> recentValues, String metricType) {
        if (recentValues == null || recentValues.size() < 10) {
            return null;
        }
        
        double mean = recentValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = recentValues.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        double latestValue = recentValues.get(recentValues.size() - 1);
        double zScore = Math.abs((latestValue - mean) / stdDev);
        
        if (zScore > 3.0) {
            String anomalyType = "statistical_anomaly";
            double confidence = Math.min(0.99, 0.8 + zScore * 0.03);
            String details = String.format("Z-score异常检测: Z=%.2f, 当前值=%.2f, 均值=%.2f, 标准差=%.2f",
                                        zScore, latestValue, mean, stdDev);
            
            AnomalyDetection anomaly = new AnomalyDetection();
            anomaly.setDeviceId(deviceId);
            anomaly.setDeviceName(deviceName);
            anomaly.setAnomalyType(anomalyType);
            anomaly.setConfidence(confidence);
            anomaly.setDetails(details);
            anomaly.setStatus("pending");
            
            AnomalyDetection saved = anomalyRepository.save(anomaly);
            log.warn("统计异常检测: {} - Z-score={}", deviceId, zScore);
            return saved;
        }
        
        return null;
    }

    /**
     * 获取设备异常记录
     */
    public List<AnomalyDetection> getDeviceAnomalies(String deviceId) {
        return anomalyRepository.findByDeviceIdOrderByDetectedAtDesc(deviceId);
    }

    /**
     * 获取待处理异常
     */
    public List<AnomalyDetection> getPendingAnomalies() {
        return anomalyRepository.findPendingAnomalies();
    }

    /**
     * 获取高置信度异常
     */
    public List<AnomalyDetection> getHighConfidenceAnomalies() {
        return anomalyRepository.findHighConfidenceAnomalies(anomalyThreshold);
    }

    /**
     * 标记异常已处理
     */
    @Transactional
    public void resolveAnomaly(Long anomalyId) {
        anomalyRepository.findById(anomalyId).ifPresent(anomaly -> {
            anomaly.setStatus("resolved");
            anomaly.setResolvedAt(LocalDateTime.now());
            anomalyRepository.save(anomaly);
            log.info("异常已处理: {}", anomalyId);
        });
    }

    /**
     * 获取异常统计
     */
    public Map<String, Object> getAnomalyStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<Object[]> typeStats = anomalyRepository.countByAnomalyType(lastWeek);
        statistics.put("byType", typeStats);
        
        List<Object[]> deviceStats = anomalyRepository.countPendingAnomaliesByDevice();
        statistics.put("pendingByDevice", deviceStats);
        
        long totalPending = anomalyRepository.findByStatusOrderByDetectedAtDesc("pending").size();
        statistics.put("totalPending", totalPending);
        
        return statistics;
    }

    /**
     * 定时检测任务
     */
    @Scheduled(fixedRateString = "${analytics.anomaly.detection-interval:600000}")
    public void periodicAnomalyDetection() {
        log.info("执行定时异常检测");
        // 可以扩展实现定期检测逻辑
    }

    private double calculateConfidence(Double value, Double min, Double max) {
        double range = max - min;
        double distance;
        
        if (value < min) {
            distance = min - value;
        } else {
            distance = value - max;
        }
        
        double normalizedDistance = distance / range;
        return Math.min(1.0, 0.7 + normalizedDistance * 0.3);
    }
}