package com.smarthome.analytics.controller;

import com.smarthome.analytics.model.*;
import com.smarthome.analytics.service.SmartRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/recommend")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final SmartRecommendationService recommendationService;

    public RecommendationController(SmartRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/scenes/{userId}")
    public ResponseEntity<List<RecommendationResult>> getSceneRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendationResult> recommendations = recommendationService.recommend(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/scenes")
    public ResponseEntity<List<Scene>> getAllScenes() {
        List<Scene> scenes = recommendationService.getAllScenes();
        return ResponseEntity.ok(scenes);
    }

    @GetMapping("/scenes/detail/{sceneId}")
    public ResponseEntity<Scene> getSceneDetail(@PathVariable String sceneId) {
        Scene scene = recommendationService.getScene(sceneId);
        if (scene == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(scene);
    }

    @PostMapping("/interactions")
    public ResponseEntity<Void> recordInteraction(@RequestBody UserSceneInteraction interaction) {
        recommendationService.recordInteraction(interaction);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metrics/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendationMetrics(@PathVariable Long userId) {
        Map<String, Object> metrics = recommendationService.getRecommendationMetrics(userId);
        return ResponseEntity.ok(metrics);
    }
}