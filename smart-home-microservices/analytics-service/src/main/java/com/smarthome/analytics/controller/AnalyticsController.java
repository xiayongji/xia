package com.smarthome.analytics.controller;

import com.smarthome.analytics.entity.AnomalyDetection;
import com.smarthome.analytics.entity.EnergyConsumption;
import com.smarthome.analytics.service.AnomalyDetectionService;
import com.smarthome.analytics.service.EnergyStatisticsService;
import com.smarthome.analytics.service.UserBehaviorAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final EnergyStatisticsService energyService;
    private final AnomalyDetectionService anomalyService;
    private final UserBehaviorAnalysisService behaviorService;

    /**
     * 能耗统计相关接口
     */
    @PostMapping("/energy")
    public ResponseEntity<EnergyConsumption> saveEnergyData(@RequestBody Map<String, Object> data) {
        try {
            String deviceId = (String) data.get("deviceId");
            String deviceName = (String) data.get("deviceName");
            Double power = ((Number) data.get("power")).doubleValue();
            Double energy = ((Number) data.get("energy")).doubleValue();
            String deviceType = (String) data.get("deviceType");
            
            EnergyConsumption saved = energyService.saveEnergyData(deviceId, deviceName, power, energy, deviceType);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("保存能耗数据失败: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/energy/today")
    public ResponseEntity<Map<String, Object>> getTodayEnergyStatistics() {
        Map<String, Object> statistics = energyService.getTodayEnergyStatistics();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/energy/stats")
    public ResponseEntity<Map<String, Object>> getEnergyStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, Object> statistics = energyService.getEnergyStatistics(startTime, endTime);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/energy/device/{deviceId}")
    public ResponseEntity<List<EnergyConsumption>> getDeviceEnergyHistory(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<EnergyConsumption> history = energyService.getDeviceEnergyHistory(deviceId, startTime, endTime);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @GetMapping("/energy/trend")
    public ResponseEntity<List<Map<String, Object>>> getEnergyTrend(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> trend = energyService.getEnergyTrend(days);
        return new ResponseEntity<>(trend, HttpStatus.OK);
    }

    @GetMapping("/energy/high-energy-devices")
    public ResponseEntity<List<Map<String, Object>>> getHighEnergyDevices(@RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> devices = energyService.getHighEnergyDevices(limit);
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    /**
     * 异常检测相关接口
     */
    @PostMapping("/anomaly/detect")
    public ResponseEntity<?> detectAnomaly(@RequestBody Map<String, Object> data) {
        try {
            String deviceId = (String) data.get("deviceId");
            String deviceName = (String) data.get("deviceName");
            String metricType = (String) data.get("metricType");
            Double value = ((Number) data.get("value")).doubleValue();
            Double normalMin = ((Number) data.get("normalMin")).doubleValue();
            Double normalMax = ((Number) data.get("normalMax")).doubleValue();
            
            AnomalyDetection anomaly = anomalyService.detectAnomaly(deviceId, deviceName, metricType, value, normalMin, normalMax);
            if (anomaly != null) {
                return new ResponseEntity<>(anomaly, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("未检测到异常", HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("异常检测失败: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/anomaly/device/{deviceId}")
    public ResponseEntity<List<AnomalyDetection>> getDeviceAnomalies(@PathVariable String deviceId) {
        List<AnomalyDetection> anomalies = anomalyService.getDeviceAnomalies(deviceId);
        return new ResponseEntity<>(anomalies, HttpStatus.OK);
    }

    @GetMapping("/anomaly/pending")
    public ResponseEntity<List<AnomalyDetection>> getPendingAnomalies() {
        List<AnomalyDetection> anomalies = anomalyService.getPendingAnomalies();
        return new ResponseEntity<>(anomalies, HttpStatus.OK);
    }

    @GetMapping("/anomaly/high-confidence")
    public ResponseEntity<List<AnomalyDetection>> getHighConfidenceAnomalies() {
        List<AnomalyDetection> anomalies = anomalyService.getHighConfidenceAnomalies();
        return new ResponseEntity<>(anomalies, HttpStatus.OK);
    }

    @PutMapping("/anomaly/{anomalyId}/resolve")
    public ResponseEntity<Void> resolveAnomaly(@PathVariable Long anomalyId) {
        anomalyService.resolveAnomaly(anomalyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/anomaly/stats")
    public ResponseEntity<Map<String, Object>> getAnomalyStatistics() {
        Map<String, Object> statistics = anomalyService.getAnomalyStatistics();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    /**
     * 用户行为分析相关接口
     */
    @PostMapping("/behavior")
    public ResponseEntity<?> recordBehavior(@RequestBody Map<String, Object> data) {
        try {
            String userId = (String) data.get("userId");
            String username = (String) data.get("username");
            String behaviorType = (String) data.get("behaviorType");
            String deviceId = (String) data.get("deviceId");
            String deviceName = (String) data.get("deviceName");
            String action = (String) data.get("action");
            String category = (String) data.get("category");
            
            behaviorService.recordBehavior(userId, username, behaviorType, deviceId, deviceName, action, category);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("记录用户行为失败: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/behavior/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserBehaviorHistory(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        // 简化返回
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/behavior/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserBehaviorStatistics(@PathVariable String userId) {
        Map<String, Object> statistics = behaviorService.getUserBehaviorStatistics(userId);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/behavior/user/{userId}/frequent-devices")
    public ResponseEntity<List<Map<String, Object>>> getUserFrequentDevices(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> devices = behaviorService.getUserFrequentDevices(userId, limit);
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    @GetMapping("/behavior/user/{userId}/active-periods")
    public ResponseEntity<Map<String, Object>> getUserActivePeriods(@PathVariable String userId) {
        Map<String, Object> periods = behaviorService.getUserActivePeriods(userId);
        return new ResponseEntity<>(periods, HttpStatus.OK);
    }

    @GetMapping("/behavior/user/{userId}/patterns")
    public ResponseEntity<Map<String, Object>> analyzeUserPatterns(@PathVariable String userId) {
        Map<String, Object> patterns = behaviorService.analyzeUserPatterns(userId);
        return new ResponseEntity<>(patterns, HttpStatus.OK);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return new ResponseEntity<>(Map.of("status", "UP", "service", "analytics-service"), HttpStatus.OK);
    }
}