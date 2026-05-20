package com.smarthome.scene.controller;

import com.smarthome.scene.dto.SceneRequest;
import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneExecution;
import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.service.SceneExecutionService;
import com.smarthome.scene.service.SceneManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scene")
@CrossOrigin(origins = "*")
public class SceneController {

    @Autowired
    private SceneManagementService sceneManagementService;

    @Autowired
    private SceneExecutionService sceneExecutionService;

    @PostMapping("/scenes")
    public ResponseEntity<Scene> createScene(@RequestBody SceneRequest request) {
        Scene created = sceneManagementService.createScene(request.toEntity());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/scenes")
    public ResponseEntity<List<Scene>> getAllScenes() {
        List<Scene> scenes = sceneManagementService.getAllScenes();
        return ResponseEntity.ok(scenes);
    }

    @GetMapping("/scenes/{id}")
    public ResponseEntity<Scene> getScene(@PathVariable Long id) {
        Scene scene = sceneManagementService.getScene(id);
        return ResponseEntity.ok(scene);
    }

    @PutMapping("/scenes/{id}")
    public ResponseEntity<Scene> updateScene(@PathVariable Long id, @RequestBody SceneRequest request) {
        Scene updated = sceneManagementService.updateScene(id, request.toEntity());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/scenes/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        sceneManagementService.deleteScene(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scenes/user/{userId}")
    public ResponseEntity<List<Scene>> getScenesByUser(@PathVariable Long userId) {
        List<Scene> scenes = sceneManagementService.getScenesByUser(userId);
        return ResponseEntity.ok(scenes);
    }

    @PutMapping("/scenes/{id}/toggle")
    public ResponseEntity<Scene> toggleScene(@PathVariable Long id, @RequestParam Boolean enabled) {
        Scene scene = sceneManagementService.toggleScene(id, enabled);
        return ResponseEntity.ok(scene);
    }

    @PostMapping("/scenes/{id}/execute")
    public ResponseEntity<SceneExecution> executeScene(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MANUAL") String triggerType,
            @RequestParam(defaultValue = "ROLE_USER") String triggerSource) {

        SceneExecution execution = sceneExecutionService.executeScene(id, triggerType, triggerSource);
        return ResponseEntity.ok(execution);
    }

    @GetMapping("/scenes/{id}/executions")
    public ResponseEntity<List<SceneExecution>> getSceneExecutions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {

        List<SceneExecution> executions = sceneExecutionService.getExecutionHistory(id, limit);
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/scenes/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getSceneStatistics(@PathVariable Long id) {
        Map<String, Object> statistics = sceneExecutionService.getSceneStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/scenes/{id}/rules")
    public ResponseEntity<SceneRule> addRule(
            @PathVariable Long id,
            @RequestBody SceneRule rule) {

        SceneRule created = sceneManagementService.addRuleToScene(id, rule);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/scenes/{id}/rules")
    public ResponseEntity<List<SceneRule>> getSceneRules(@PathVariable Long id) {
        List<SceneRule> rules = sceneManagementService.getRulesForScene(id);
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<SceneRule> updateRule(
            @PathVariable Long ruleId,
            @RequestBody SceneRule rule) {

        SceneRule updated = sceneManagementService.updateRule(ruleId, rule);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        sceneManagementService.removeRule(ruleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scenes/search")
    public ResponseEntity<List<Scene>> searchScenes(@RequestParam String keyword) {
        List<Scene> scenes = sceneManagementService.searchScenes(keyword);
        return ResponseEntity.ok(scenes);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "scene-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}