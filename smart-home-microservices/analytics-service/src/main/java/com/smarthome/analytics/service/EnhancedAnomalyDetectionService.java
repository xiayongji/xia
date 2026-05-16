package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.*;
import com.smarthome.analytics.repository.LoadProfileRepository;
import com.smarthome.analytics.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedAnomalyDetectionService {

    private final SensorDataRepository sensorDataRepository;
    private final LoadProfileRepository loadProfileRepository;
    
    private final Map<String, PowerConsumptionPattern> patternCache = new ConcurrentHashMap<>();

    public AnomalyResult detectAnomaly(String deviceId) {
        List<SensorData> recentData = sensorDataRepository.findRecentByDeviceId(deviceId, 60);
        
        if (recentData.isEmpty()) {
            return AnomalyResult.builder()
                    .deviceId(deviceId)
                    .isAnomaly(false)
                    .anomalyProbability(0.0)
                    .message("无足够数据进行检测")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        PowerConsumptionPattern pattern = getOrBuildPattern(deviceId, recentData);
        
        SensorData latestReading = recentData.get(0);
        PowerReading reading = PowerReading.builder()
                .deviceId(deviceId)
                .power(latestReading.getValue())
                .timestamp(latestReading.getTimestamp())
                .rateOfChange(calculateRateOfChange(recentData))
                .build();

        double anomalyScore = calculateAnomalyScore(pattern, reading);
        boolean isAnomaly = anomalyScore > 0.7;
        
        AnomalyResult result = AnomalyResult.builder()
                .deviceId(deviceId)
                .isAnomaly(isAnomaly)
                .anomalyProbability(anomalyScore)
                .message(isAnomaly ? generateAnomalyMessage(pattern, reading) : "设备运行正常")
                .timestamp(LocalDateTime.now())
                .build();

        if (isAnomaly) {
            log.warn("检测到设备异常 - 设备ID: {}, 异常概率: {}, 级别: {}", 
                    deviceId, anomalyScore, AnomalyLevel.fromScore(anomalyScore));
        }

        return result;
    }

    private PowerConsumptionPattern getOrBuildPattern(String deviceId, List<SensorData> data) {
        return patternCache.computeIfAbsent(deviceId, k -> {
            double[] values = data.stream()
                    .mapToDouble(SensorData::getValue)
                    .toArray();
            
            double avg = java.util.Arrays.stream(values).average().orElse(0);
            double variance = java.util.Arrays.stream(values)
                    .map(v -> Math.pow(v - avg, 2))
                    .average().orElse(0);
            double stdDev = Math.sqrt(variance);
            
            return PowerConsumptionPattern.builder()
                    .deviceId(deviceId)
                    .normalRange(Range.builder()
                            .min(avg - 2 * stdDev)
                            .max(avg + 2 * stdDev)
                            .build())
                    .peakRange(Range.builder()
                            .min(avg + 2 * stdDev)
                            .max(avg + 4 * stdDev)
                            .build())
                    .avgPower(avg)
                    .stdDev(stdDev)
                    .build();
        });
    }

    private double calculateAnomalyScore(PowerConsumptionPattern pattern, PowerReading reading) {
        double score = 0;
        double power = reading.getPower();

        if (!pattern.isInNormalRange(power)) {
            score += 0.3;
        }

        if (pattern.isPeak(power)) {
            score += 0.4;
        }

        score += calculatePatternDeviation(pattern, reading);
        score += calculateRateOfChangeScore(reading);

        return Math.min(score, 1.0);
    }

    private double calculatePatternDeviation(PowerConsumptionPattern pattern, PowerReading reading) {
        double deviation = Math.abs(reading.getPower() - pattern.getAvgPower()) / pattern.getStdDev();
        return Math.min(deviation * 0.15, 0.25);
    }

    private double calculateRateOfChangeScore(PowerReading reading) {
        double rate = Math.abs(reading.getRateOfChange());
        return Math.min(rate * 0.1, 0.3);
    }

    private double calculateRateOfChange(List<SensorData> data) {
        if (data.size() < 2) return 0;
        
        SensorData latest = data.get(0);
        SensorData previous = data.get(1);
        
        double timeDiff = java.time.Duration.between(previous.getTimestamp(), latest.getTimestamp()).toSeconds();
        if (timeDiff == 0) return 0;
        
        return (latest.getValue() - previous.getValue()) / timeDiff;
    }

    private String generateAnomalyMessage(PowerConsumptionPattern pattern, PowerReading reading) {
        StringBuilder sb = new StringBuilder();
        sb.append("检测到异常: ");
        
        if (!pattern.isInNormalRange(reading.getPower())) {
            sb.append(String.format("功率值(%.2fW)超出正常范围[%.2f, %.2f]; ", 
                    reading.getPower(), pattern.getNormalRange().getMin(), pattern.getNormalRange().getMax()));
        }
        if (pattern.isPeak(reading.getPower())) {
            sb.append(String.format("达到峰值功率(%.2fW > %.2fW); ", 
                    reading.getPower(), pattern.getPeakRange().getMax()));
        }
        if (Math.abs(reading.getRateOfChange()) > 10) {
            sb.append(String.format("功率变化率异常(%.2f/s); ", reading.getRateOfChange()));
        }
        
        return sb.toString();
    }

    public void updatePattern(String deviceId, List<SensorData> newData) {
        patternCache.remove(deviceId);
        getOrBuildPattern(deviceId, newData);
    }

    public AnomalyLevel getAnomalyLevel(String deviceId) {
        AnomalyResult result = detectAnomaly(deviceId);
        return AnomalyLevel.fromScore(result.getAnomalyProbability());
    }
}