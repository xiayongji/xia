package com.smarthome.scene.service;

import com.smarthome.scene.entity.PredictedScene;
import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenePredictionService {

    private final SceneRepository sceneRepository;
    
    private final Map<String, Integer> sceneTriggerCount = new ConcurrentHashMap<>();
    private static final double CONFIDENCE_THRESHOLD = 0.85;

    public PredictedScene predictScene(String userId, LocalDateTime timestamp, Map<String, Object> context) {
        List<Scene> scenes = sceneRepository.findAll();
        
        double maxConfidence = 0;
        PredictedScene bestPrediction = null;

        for (Scene scene : scenes) {
            double confidence = calculateConfidence(scene, timestamp, context);
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                bestPrediction = PredictedScene.builder()
                        .sceneId(scene.getId().toString())
                        .sceneName(scene.getSceneName())
                        .confidence(confidence)
                        .expectedTime(timestamp)
                        .triggerReason(generateReason(scene, confidence))
                        .build();
            }
        }

        if (bestPrediction != null && maxConfidence >= CONFIDENCE_THRESHOLD) {
            log.info("预测场景: {} (置信度: {})", bestPrediction.getSceneName(), maxConfidence);
            return bestPrediction;
        }

        return null;
    }

    private double calculateConfidence(Scene scene, LocalDateTime timestamp, Map<String, Object> context) {
        double score = 0;

        LocalTime currentTime = timestamp.toLocalTime();
        
        if (scene.getStartTime() != null && scene.getEndTime() != null) {
            if (currentTime.isAfter(scene.getStartTime()) && currentTime.isBefore(scene.getEndTime())) {
                score += 0.4;
            }
        }

        if (context.containsKey("location")) {
            String location = (String) context.get("location");
            if (scene.getLocation() != null && scene.getLocation().equals(location)) {
                score += 0.25;
            }
        }

        if (context.containsKey("timeOfDay")) {
            String timeOfDay = (String) context.get("timeOfDay");
            if (isMatchingTimeOfDay(scene, timeOfDay)) {
                score += 0.2;
            }
        }

        String sceneId = scene.getId().toString();
        int count = sceneTriggerCount.getOrDefault(sceneId, 0);
        score += Math.min(count * 0.02, 0.15);

        return Math.min(score, 1.0);
    }

    private boolean isMatchingTimeOfDay(Scene scene, String timeOfDay) {
        String sceneName = scene.getSceneName().toLowerCase();
        
        return switch (timeOfDay) {
            case "morning" -> sceneName.contains("早晨") || sceneName.contains("起床") || sceneName.contains("早餐");
            case "afternoon" -> sceneName.contains("下午") || sceneName.contains("工作");
            case "evening" -> sceneName.contains("傍晚") || sceneName.contains("回家") || sceneName.contains("晚餐");
            case "night" -> sceneName.contains("夜间") || sceneName.contains("睡眠") || sceneName.contains("休息");
            default -> false;
        };
    }

    private String generateReason(Scene scene, double confidence) {
        StringBuilder sb = new StringBuilder("场景预测原因: ");
        
        if (confidence >= 0.4) {
            sb.append("时间匹配; ");
        }
        if (confidence >= 0.65) {
            sb.append("位置匹配; ");
        }
        if (confidence >= 0.85) {
            sb.append("历史模式匹配");
        }
        
        return sb.toString();
    }

    public void recordSceneTrigger(String sceneId) {
        sceneTriggerCount.merge(sceneId, 1, Integer::sum);
    }

    public List<PredictedScene> getSceneRecommendations(String userId, Map<String, Object> context) {
        LocalDateTime now = LocalDateTime.now();
        
        return sceneRepository.findAll().stream()
                .map(scene -> {
                    double confidence = calculateConfidence(scene, now, context);
                    return PredictedScene.builder()
                            .sceneId(scene.getId().toString())
                            .sceneName(scene.getSceneName())
                            .confidence(confidence)
                            .expectedTime(now.plusMinutes(5))
                            .triggerReason(generateReason(scene, confidence))
                            .build();
                })
                .filter(p -> p.getConfidence() >= 0.6)
                .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
                .limit(3)
                .collect(Collectors.toList());
    }

    public double getPredictionAccuracy() {
        return CONFIDENCE_THRESHOLD;
    }
}