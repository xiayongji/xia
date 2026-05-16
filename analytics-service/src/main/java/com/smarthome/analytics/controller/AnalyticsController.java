package com.smarthome.analytics.controller;

import com.smarthome.analytics.model.*;
import com.smarthome.analytics.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final LSTMAnomalyDetectionService anomalyDetectionService;
    private final UserBehaviorAnalyzerService behaviorAnalyzerService;
    private final EnergyStatisticsService energyStatisticsService;

    public AnalyticsController(
            LSTMAnomalyDetectionService anomalyDetectionService,
            UserBehaviorAnalyzerService behaviorAnalyzerService,
            EnergyStatisticsService energyStatisticsService) {
        this.anomalyDetectionService = anomalyDetectionService;
        this.behaviorAnalyzerService = behaviorAnalyzerService;
        this.energyStatisticsService = energyStatisticsService;
    }

    @PostMapping("/anomaly/detect")
    public ResponseEntity<AnomalyResult> detectAnomaly(@RequestBody PowerReading reading) {
        AnomalyResult result = anomalyDetectionService.detectAnomaly(reading.getDeviceId(), reading);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/anomaly/batch-detect")
    public ResponseEntity<List<AnomalyResult>> batchDetectAnomalies(@RequestBody List<PowerReading> readings) {
        List<AnomalyResult> results = anomalyDetectionService.batchDetectAnomalies(readings);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/anomaly/{deviceId}/recent")
    public ResponseEntity<Map<String, List<AnomalyResult>>> getRecentAnomalies(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        Map<String, List<AnomalyResult>> anomalies = anomalyDetectionService.getRecentAnomalies(deviceId, hours);
        return ResponseEntity.ok(anomalies);
    }

    @PostMapping("/behavior/analyze")
    public ResponseEntity<UserBehavior> analyzeUserBehavior(
            @RequestParam Long userId,
            @RequestBody List<BehaviorEvent> events) {
        UserBehavior behavior = behaviorAnalyzerService.analyzeUserBehavior(userId, events);
        return ResponseEntity.ok(behavior);
    }

    @GetMapping("/behavior/{userId}")
    public ResponseEntity<UserBehavior> getUserBehavior(@PathVariable Long userId) {
        UserBehavior behavior = behaviorAnalyzerService.getUserBehavior(userId);
        if (behavior == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(behavior);
    }

    @GetMapping("/behavior/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getBehaviorSummary(@PathVariable Long userId) {
        Map<String, Object> summary = behaviorAnalyzerService.getBehaviorAnalysisSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/behavior/{userId}/recommendations")
    public ResponseEntity<List<SceneRecommendation>> getRecommendations(@PathVariable Long userId) {
        List<SceneRecommendation> recommendations = behaviorAnalyzerService.recommendScenes(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/behavior/segments")
    public ResponseEntity<List<UserSegment>> getAllSegments() {
        List<UserSegment> segments = behaviorAnalyzerService.getAllSegments();
        return ResponseEntity.ok(segments);
    }

    @GetMapping("/energy/today")
    public ResponseEntity<Map<String, Object>> getTodayEnergySummary() {
        Map<String, Object> summary = energyStatisticsService.getOverallEnergySummary(LocalDate.now());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/energy/{deviceId}/daily")
    public ResponseEntity<EnergyStat> getDailyEnergy(
            @PathVariable String deviceId,
            @RequestParam(required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        EnergyStat stat = energyStatisticsService.getDailyEnergy(deviceId, date);
        return ResponseEntity.ok(stat);
    }

    @GetMapping("/energy/{deviceId}/weekly")
    public ResponseEntity<EnergyStat> getWeeklyEnergy(
            @PathVariable String deviceId,
            @RequestParam(required = false) LocalDate startDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        }
        EnergyStat stat = energyStatisticsService.getWeeklyEnergy(deviceId, startDate);
        return ResponseEntity.ok(stat);
    }

    @GetMapping("/energy/{deviceId}/monthly")
    public ResponseEntity<EnergyStat> getMonthlyEnergy(
            @PathVariable String deviceId,
            @RequestParam(required = false) LocalDate startDate) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        EnergyStat stat = energyStatisticsService.getMonthlyEnergy(deviceId, startDate);
        return ResponseEntity.ok(stat);
    }

    @GetMapping("/energy/{deviceId}/compare")
    public ResponseEntity<CompareResult> compareEnergy(
            @PathVariable String deviceId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        CompareResult result = energyStatisticsService.compareEnergy(deviceId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/energy/{deviceId}/report")
    public ResponseEntity<EnergyAnalysisReport> getEnergyReport(
            @PathVariable String deviceId,
            @RequestParam(required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        EnergyAnalysisReport report = energyStatisticsService.generateAnalysisReport(deviceId, date);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/energy/record")
    public ResponseEntity<Void> addEnergyRecord(@RequestBody EnergyRecord record) {
        energyStatisticsService.addEnergyRecord(record);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "analytics-service");
        return ResponseEntity.ok(health);
    }
}