package com.smarthome.analytics.service;

import com.smarthome.analytics.model.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LSTMAnomalyDetectionService {

    private static final double ANOMALY_THRESHOLD = 0.7;
    private static final double HIGH_ANOMALY_THRESHOLD = 0.9;
    private static final int SEQUENCE_LENGTH = 24;

    private final Map<String, List<PowerReading>> deviceHistory = new ConcurrentHashMap<>();
    private final Map<String, PowerConsumptionPattern> devicePatterns = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        initializeSamplePatterns();
    }

    private void initializeSamplePatterns() {
        PowerConsumptionPattern acPattern = PowerConsumptionPattern.builder()
                .deviceId("ac_001")
                .minPower(800.0)
                .maxPower(1500.0)
                .avgPower(1100.0)
                .stdDeviation(200.0)
                .peakThreshold(1400.0)
                .normalRangeMin(700.0)
                .normalRangeMax(1600.0)
                .lastUpdated(LocalDateTime.now())
                .build();
        devicePatterns.put("ac_001", acPattern);

        PowerConsumptionPattern fridgePattern = PowerConsumptionPattern.builder()
                .deviceId("fridge_001")
                .minPower(100.0)
                .maxPower(200.0)
                .avgPower(150.0)
                .stdDeviation(30.0)
                .peakThreshold(250.0)
                .normalRangeMin(80.0)
                .normalRangeMax(220.0)
                .lastUpdated(LocalDateTime.now())
                .build();
        devicePatterns.put("fridge_001", fridgePattern);

        PowerConsumptionPattern heaterPattern = PowerConsumptionPattern.builder()
                .deviceId("heater_001")
                .minPower(1000.0)
                .maxPower(2000.0)
                .avgPower(1500.0)
                .stdDeviation(300.0)
                .peakThreshold(1900.0)
                .normalRangeMin(900.0)
                .normalRangeMax(2100.0)
                .lastUpdated(LocalDateTime.now())
                .build();
        devicePatterns.put("heater_001", heaterPattern);
    }

    public AnomalyResult detectAnomaly(String deviceId, PowerReading reading) {
        PowerConsumptionPattern pattern = devicePatterns.get(deviceId);

        if (pattern == null) {
            pattern = createPatternFromHistory(deviceId);
        }

        double score = calculateAnomalyScore(pattern, reading);

        boolean isAnomaly = score > ANOMALY_THRESHOLD;
        String level = score > HIGH_ANOMALY_THRESHOLD ? "HIGH" :
                       score > ANOMALY_THRESHOLD ? "MEDIUM" : "LOW";

        String anomalyType = classifyAnomaly(pattern, reading, score);
        String description = generateAnomalyDescription(deviceId, anomalyType, score);

        updateDeviceHistory(deviceId, reading);

        return AnomalyResult.builder()
                .deviceId(deviceId)
                .score(Math.min(score, 1.0))
                .isAnomaly(isAnomaly)
                .level(level)
                .anomalyType(anomalyType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .threshold(ANOMALY_THRESHOLD)
                .build();
    }

    private PowerConsumptionPattern createPatternFromHistory(String deviceId) {
        List<PowerReading> history = deviceHistory.get(deviceId);

        if (history == null || history.isEmpty()) {
            return PowerConsumptionPattern.builder()
                    .deviceId(deviceId)
                    .minPower(0.0)
                    .maxPower(1000.0)
                    .avgPower(500.0)
                    .stdDeviation(100.0)
                    .peakThreshold(900.0)
                    .normalRangeMin(0.0)
                    .normalRangeMax(1100.0)
                    .lastUpdated(LocalDateTime.now())
                    .build();
        }

        DoubleSummaryStatistics stats = history.stream()
                .mapToDouble(PowerReading::getPower)
                .summaryStatistics();

        double mean = stats.getAverage();
        double stdDev = calculateStdDeviation(history, mean);

        return PowerConsumptionPattern.builder()
                .deviceId(deviceId)
                .minPower(stats.getMin())
                .maxPower(stats.getMax())
                .avgPower(mean)
                .stdDeviation(stdDev)
                .peakThreshold(mean + 2 * stdDev)
                .normalRangeMin(mean - 2 * stdDev)
                .normalRangeMax(mean + 2 * stdDev)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private double calculateAnomalyScore(PowerConsumptionPattern pattern, PowerReading reading) {
        double score = 0.0;
        double power = reading.getPower();

        if (power < pattern.getNormalRangeMin() || power > pattern.getNormalRangeMax()) {
            score += 0.3;
        }

        if (power > pattern.getPeakThreshold()) {
            score += 0.4;
        }

        score += calculatePatternDeviation(pattern, reading);
        score += calculateRateOfChangeScore(pattern.getDeviceId(), reading);
        score += calculateTimeBasedScore(pattern, reading);

        return Math.min(score, 1.0);
    }

    private double calculatePatternDeviation(PowerConsumptionPattern pattern, PowerReading reading) {
        double deviation = Math.abs(reading.getPower() - pattern.getAvgPower());
        double normalizedDeviation = deviation / pattern.getStdDeviation();

        if (normalizedDeviation > 3) {
            return 0.3;
        } else if (normalizedDeviation > 2) {
            return 0.2;
        } else if (normalizedDeviation > 1) {
            return 0.1;
        }
        return 0.0;
    }

    private double calculateRateOfChangeScore(String deviceId, PowerReading reading) {
        List<PowerReading> history = deviceHistory.get(deviceId);

        if (history == null || history.isEmpty()) {
            return 0.0;
        }

        PowerReading lastReading = history.get(history.size() - 1);
        double rateOfChange = Math.abs(reading.getPower() - lastReading.getPower()) / lastReading.getPower();

        if (rateOfChange > 0.5) {
            return 0.25;
        } else if (rateOfChange > 0.3) {
            return 0.15;
        } else if (rateOfChange > 0.2) {
            return 0.1;
        }
        return 0.0;
    }

    private double calculateTimeBasedScore(PowerConsumptionPattern pattern, PowerReading reading) {
        int hour = reading.getTimestamp().getHour();

        boolean isPeakHour = hour >= 7 && hour <= 9 || hour >= 18 && hour <= 21;
        boolean isNightHour = hour >= 0 && hour <= 5;

        double power = reading.getPower();

        if (isNightHour && power > pattern.getAvgPower() * 0.5) {
            return 0.15;
        }

        if (isPeakHour && power > pattern.getMaxPower() * 0.9) {
            return 0.1;
        }

        return 0.0;
    }

    private String classifyAnomaly(PowerConsumptionPattern pattern, PowerReading reading, double score) {
        if (score < ANOMALY_THRESHOLD) {
            return "NORMAL";
        }

        double power = reading.getPower();

        if (power > pattern.getMaxPower() * 1.5) {
            return "OVERLOAD";
        }

        if (power < pattern.getMinPower() * 0.3) {
            return "UNDERLOAD";
        }

        if (power > pattern.getPeakThreshold()) {
            return "POWER_SPIKE";
        }

        List<PowerReading> history = deviceHistory.get(reading.getDeviceId());
        if (history != null && history.size() >= 2) {
            double trend = calculateTrend(history, reading);
            if (trend > 0.8) {
                return "ABNORMAL_INCREASE";
            } else if (trend < -0.8) {
                return "ABNORMAL_DECREASE";
            }
        }

        return "UNUSUAL_PATTERN";
    }

    private double calculateTrend(List<PowerReading> history, PowerReading current) {
        if (history.size() < 3) {
            return 0.0;
        }

        int startIdx = Math.max(0, history.size() - 6);
        List<PowerReading> recentHistory = history.subList(startIdx, history.size());

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = recentHistory.size();

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = recentHistory.get(i).getPower();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

        double currentPower = current.getPower();
        double expectedPower = recentHistory.get(n - 1).getPower() + slope;

        if (expectedPower == 0) {
            return 0.0;
        }

        return (currentPower - expectedPower) / expectedPower;
    }

    private String generateAnomalyDescription(String deviceId, String anomalyType, double score) {
        String baseDescription = switch (anomalyType) {
            case "OVERLOAD" -> "设备功率严重超出正常范围，可能存在故障或异常负载";
            case "UNDERLOAD" -> "设备功率异常低，可能存在故障或能效问题";
            case "POWER_SPIKE" -> "检测到功率突增，可能存在瞬时负载或设备异常";
            case "ABNORMAL_INCREASE" -> "功率呈现持续上升趋势，存在潜在故障风险";
            case "ABNORMAL_DECREASE" -> "功率呈现持续下降趋势，设备可能存在性能问题";
            case "UNUSUAL_PATTERN" -> "检测到异常用电模式，建议检查设备状态";
            default -> "未检测到明显异常";
        };

        return String.format("[%s] %s (异常分数: %.2f)", deviceId, baseDescription, score);
    }

    private void updateDeviceHistory(String deviceId, PowerReading reading) {
        deviceHistory.computeIfAbsent(deviceId, k -> Collections.synchronizedList(new ArrayList<>()));

        List<PowerReading> history = deviceHistory.get(deviceId);
        synchronized (history) {
            history.add(reading);

            if (history.size() > SEQUENCE_LENGTH * 7) {
                history.remove(0);
            }
        }
    }

    private double calculateStdDeviation(List<PowerReading> readings, double mean) {
        if (readings.isEmpty()) {
            return 0.0;
        }

        double sumSquaredDiff = readings.stream()
                .mapToDouble(r -> Math.pow(r.getPower() - mean, 2))
                .sum();

        return Math.sqrt(sumSquaredDiff / readings.size());
    }

    public List<AnomalyResult> batchDetectAnomalies(List<PowerReading> readings) {
        return readings.stream()
                .map(reading -> detectAnomaly(reading.getDeviceId(), reading))
                .collect(Collectors.toList());
    }

    public Map<String, List<AnomalyResult>> getRecentAnomalies(String deviceId, int hours) {
        List<PowerReading> history = deviceHistory.get(deviceId);

        if (history == null) {
            return Collections.emptyMap();
        }

        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);

        List<PowerReading> recentReadings = history.stream()
                .filter(r -> r.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());

        List<AnomalyResult> anomalies = batchDetectAnomalies(recentReadings)
                .stream()
                .filter(AnomalyResult::getIsAnomaly)
                .collect(Collectors.toList());

        return Map.of(deviceId, anomalies);
    }

    public void updatePattern(String deviceId, PowerConsumptionPattern pattern) {
        devicePatterns.put(deviceId, pattern);
    }

    public PowerConsumptionPattern getPattern(String deviceId) {
        return devicePatterns.get(deviceId);
    }
}