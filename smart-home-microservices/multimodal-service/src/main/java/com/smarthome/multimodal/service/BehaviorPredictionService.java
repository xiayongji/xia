package com.smarthome.multimodal.service;

import com.smarthome.multimodal.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BehaviorPredictionService {

    private final Map<String, UserBehaviorPattern> userPatterns = new ConcurrentHashMap<>();
    private static final double CONFIDENCE_THRESHOLD = 0.85;

    public PredictedBehavior predictBehavior(String userId, ContextSnapshot context) {
        UserBehaviorPattern pattern = userPatterns.computeIfAbsent(userId, k -> new UserBehaviorPattern());

        double maxScore = 0;
        PredictedBehavior bestPrediction = null;

        for (BehaviorRule rule : pattern.getRules()) {
            double score = calculateMatchScore(rule, context);
            if (score > maxScore) {
                maxScore = score;
                bestPrediction = PredictedBehavior.builder()
                        .behaviorType(rule.getBehaviorType())
                        .confidence(score)
                        .predictedAction(rule.getAction())
                        .expectedTime(context.getTimestamp().plusSeconds(rule.getDelaySeconds()))
                        .build();
            }
        }

        if (bestPrediction != null && maxScore >= CONFIDENCE_THRESHOLD) {
            return bestPrediction;
        }

        return null;
    }

    private double calculateMatchScore(BehaviorRule rule, ContextSnapshot context) {
        double score = 0;

        if (rule.getTimeRange() != null) {
            LocalTime currentTime = context.getTimestamp().toLocalTime();
            if (currentTime.isAfter(rule.getTimeRange().getStart()) && 
                currentTime.isBefore(rule.getTimeRange().getEnd())) {
                score += 0.3;
            }
        }

        if (rule.getLocation() != null && rule.getLocation().equals(context.getLocation())) {
            score += 0.25;
        }

        if (rule.getPreviousAction() != null && 
            rule.getPreviousAction().equals(context.getLastAction())) {
            score += 0.2;
        }

        if (rule.getEnvironmentalConditions() != null) {
            for (Map.Entry<String, Object> condition : rule.getEnvironmentalConditions().entrySet()) {
                if (matchesCondition(context, condition.getKey(), condition.getValue())) {
                    score += 0.1;
                }
            }
        }

        score += calculatePatternFrequency(rule);

        return Math.min(score, 1.0);
    }

    private boolean matchesCondition(ContextSnapshot context, String key, Object value) {
        return switch (key) {
            case "temperature" -> context.getTemperature() != null && 
                    Math.abs(context.getTemperature() - (Double) value) < 5;
            case "lightLevel" -> context.getLightLevel() != null && 
                    context.getLightLevel() < (Integer) value;
            case "motionDetected" -> Boolean.TRUE.equals(context.getMotionDetected()) == (Boolean) value;
            default -> false;
        };
    }

    private double calculatePatternFrequency(BehaviorRule rule) {
        int occurrence = rule.getOccurrenceCount();
        return Math.min(occurrence * 0.05, 0.15);
    }

    public void learnBehavior(String userId, BehaviorEvent event) {
        UserBehaviorPattern pattern = userPatterns.computeIfAbsent(userId, k -> new UserBehaviorPattern());
        
        BehaviorRule existingRule = pattern.getRules().stream()
                .filter(r -> r.getBehaviorType().equals(event.getBehaviorType()))
                .findFirst()
                .orElse(null);

        if (existingRule != null) {
            existingRule.setOccurrenceCount(existingRule.getOccurrenceCount() + 1);
        } else {
            BehaviorRule newRule = BehaviorRule.builder()
                    .behaviorType(event.getBehaviorType())
                    .action(event.getAction())
                    .timeRange(new TimeRange(event.getTimestamp().toLocalTime(), 
                            event.getTimestamp().plusMinutes(30).toLocalTime()))
                    .location(event.getLocation())
                    .previousAction(event.getPreviousAction())
                    .occurrenceCount(1)
                    .delaySeconds(60)
                    .build();
            pattern.getRules().add(newRule);
        }
    }

    public double getPredictionAccuracy() {
        return CONFIDENCE_THRESHOLD;
    }
}