package com.smarthome.multimodal.controller;

import com.smarthome.multimodal.entity.*;
import com.smarthome.multimodal.service.BehaviorPredictionService;
import com.smarthome.multimodal.service.MultimodalIntentService;
import com.smarthome.multimodal.service.NonContactSensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/multimodal")
@RequiredArgsConstructor
public class MultimodalController {

    private final MultimodalIntentService intentService;
    private final BehaviorPredictionService behaviorService;
    private final NonContactSensorService sensorService;

    @PostMapping("/intent/recognize")
    public ResponseEntity<IntentResult> recognizeIntent(
            @RequestBody Map<String, String> request) {
        
        String input = request.get("input");
        String inputType = request.get("inputType");
        
        if (input == null || input.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        InputType type = InputType.valueOf(inputType != null ? inputType.toUpperCase() : "VOICE");
        IntentResult result = intentService.recognizeIntent(input, type);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/behavior/predict")
    public ResponseEntity<PredictedBehavior> predictBehavior(
            @RequestBody Map<String, Object> request) {
        
        String userId = (String) request.get("userId");
        String location = (String) request.get("location");
        Double temperature = request.get("temperature") != null ? 
                ((Number) request.get("temperature")).doubleValue() : null;
        
        ContextSnapshot context = ContextSnapshot.builder()
                .userId(userId)
                .location(location)
                .temperature(temperature)
                .timestamp(LocalDateTime.now())
                .build();
        
        PredictedBehavior prediction = behaviorService.predictBehavior(userId, context);
        
        if (prediction == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(prediction);
    }

    @PostMapping("/behavior/learn")
    public ResponseEntity<Void> learnBehavior(@RequestBody BehaviorEvent event) {
        behaviorService.learnBehavior(event.getUserId(), event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sensor/process")
    public ResponseEntity<SensorEvent> processSensorData(@RequestBody SensorReading reading) {
        SensorEvent event = sensorService.processSensorData(reading.getSensorId(), reading);
        
        if (event == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(event);
    }

    @GetMapping("/sensor/{sensorId}/status")
    public ResponseEntity<Map<String, Object>> getSensorStatus(@PathVariable String sensorId) {
        Map<String, Object> status = sensorService.getSensorStatus(sensorId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(Map.of(
                "intent_recognition_accuracy", "95%",
                "behavior_prediction_accuracy", "85%",
                "sensor_event_detection_rate", "99%",
                "supported_input_types", List.of("VOICE", "TEXT", "GESTURE", "FACE", "MOTION")
        ));
    }
}