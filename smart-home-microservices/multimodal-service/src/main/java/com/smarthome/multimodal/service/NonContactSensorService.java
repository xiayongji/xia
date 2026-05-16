package com.smarthome.multimodal.service;

import com.smarthome.multimodal.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NonContactSensorService {

    private final Map<String, SensorDataPoint> sensorData = new ConcurrentHashMap<>();

    public SensorEvent processSensorData(String sensorId, SensorReading reading) {
        SensorDataPoint dataPoint = sensorData.computeIfAbsent(sensorId, k -> new SensorDataPoint());
        
        dataPoint.addReading(reading);
        
        if (dataPoint.getReadings().size() >= 5) {
            SensorEvent event = analyzeSensorPattern(dataPoint);
            dataPoint.clearReadings();
            return event;
        }
        
        return null;
    }

    private SensorEvent analyzeSensorPattern(SensorDataPoint dataPoint) {
        List<SensorReading> readings = dataPoint.getReadings();
        
        double avgValue = readings.stream()
                .mapToDouble(SensorReading::getValue)
                .average()
                .orElse(0);
        
        double variance = readings.stream()
                .mapToDouble(r -> Math.pow(r.getValue() - avgValue, 2))
                .average()
                .orElse(0);

        SensorEventType eventType = classifyEvent(avgValue, variance, dataPoint.getSensorType());
        
        if (eventType != SensorEventType.NONE) {
            return SensorEvent.builder()
                    .sensorId(dataPoint.getSensorId())
                    .sensorType(dataPoint.getSensorType())
                    .eventType(eventType)
                    .confidence(calculateConfidence(readings, eventType))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        return null;
    }

    private SensorEventType classifyEvent(double avgValue, double variance, SensorType sensorType) {
        return switch (sensorType) {
            case MOTION -> variance > 10 ? SensorEventType.MOTION_DETECTED : SensorEventType.NONE;
            case PROXIMITY -> avgValue < 50 ? SensorEventType.APPROACHING : 
                             avgValue > 100 ? SensorEventType.LEAVING : SensorEventType.NONE;
            case GESTURE -> classifyGesture(avgValue, variance);
            case FACE_RECOGNITION -> avgValue > 0.8 ? SensorEventType.FACE_DETECTED : SensorEventType.NONE;
            case VOICE -> variance > 5 ? SensorEventType.VOICE_DETECTED : SensorEventType.NONE;
            default -> SensorEventType.NONE;
        };
    }

    private SensorEventType classifyGesture(double avgValue, double variance) {
        if (avgValue > 100 && variance > 50) return SensorEventType.GESTURE_SWIPE_RIGHT;
        if (avgValue < -50 && variance > 50) return SensorEventType.GESTURE_SWIPE_LEFT;
        if (avgValue > 150) return SensorEventType.GESTURE_WAVE;
        return SensorEventType.NONE;
    }

    private double calculateConfidence(List<SensorReading> readings, SensorEventType eventType) {
        if (eventType == SensorEventType.NONE) return 0.0;
        
        long consistentReadings = readings.stream()
                .filter(r -> r.getConfidence() > 0.7)
                .count();
        
        return Math.min(consistentReadings * 0.2, 1.0);
    }

    public List<SensorEvent> batchProcess(List<SensorReading> readings) {
        List<SensorEvent> events = new ArrayList<>();
        
        for (SensorReading reading : readings) {
            SensorEvent event = processSensorData(reading.getSensorId(), reading);
            if (event != null) {
                events.add(event);
            }
        }
        
        return events;
    }

    public Map<String, Object> getSensorStatus(String sensorId) {
        SensorDataPoint data = sensorData.get(sensorId);
        if (data == null) {
            return Map.of("status", "offline");
        }
        
        return Map.of(
                "status", "online",
                "sensorType", data.getSensorType(),
                "readingCount", data.getReadings().size()
        );
    }
}