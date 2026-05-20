package com.smarthome.analytics.controller;

import com.smarthome.analytics.service.SceneRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final SceneRecommendationService recommendationService;

    @GetMapping("/scenes/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getSceneRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(recommendationService.recommendScenes(userId, limit));
    }
}
