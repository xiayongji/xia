package com.smarthome.scene.controller;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.service.SceneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 场景控制控制器
 * 提供场景管理、执行等REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
public class SceneController {
    
    private final SceneService sceneService;
    
    /**
     * 创建场景
     */
    @PostMapping
    public ResponseEntity<?> createScene(@RequestBody Scene scene) {
        try {
            Scene createdScene = sceneService.createScene(scene);
            return new ResponseEntity<>(createdScene, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("场景创建失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 获取所有场景
     */
    @GetMapping
    public ResponseEntity<List<Scene>> getAllScenes() {
        List<Scene> scenes = sceneService.getAllScenes();
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 获取启用的场景
     */
    @GetMapping("/active")
    public ResponseEntity<List<Scene>> getActiveScenes() {
        List<Scene> scenes = sceneService.getActiveScenes();
        return new ResponseEntity<>(scenes, HttpStatus.OK);
    }
    
    /**
     * 获取场景详情
     */
    @GetMapping("/{sceneId}")
    public ResponseEntity<Scene> getScene(@PathVariable String sceneId) {
        Optional<Scene> scene = sceneService.getScene(sceneId);
        return scene.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 更新场景
     */
    @PutMapping("/{sceneId}")
    public ResponseEntity<?> updateScene(@PathVariable String sceneId, @RequestBody Scene scene) {
        try {
            scene.setSceneId(sceneId);
            Scene updatedScene = sceneService.updateScene(scene);
            return new ResponseEntity<>(updatedScene, HttpStatus.OK);
        } catch (Exception e) {
            log.error("场景更新失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 切换场景状态
     */
    @PutMapping("/{sceneId}/toggle")
    public ResponseEntity<Void> toggleScene(@PathVariable String sceneId, @RequestParam boolean enabled) {
        sceneService.toggleScene(sceneId, enabled);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    /**
     * 执行场景
     */
    @PostMapping("/{sceneId}/execute")
    public ResponseEntity<?> executeScene(@PathVariable String sceneId,
                                         @RequestParam(required = false) String triggerType,
                                         @RequestParam(required = false) String triggerCondition) {
        try {
            sceneService.executeScene(sceneId, 
                triggerType != null ? triggerType : "manual", 
                triggerCondition != null ? triggerCondition : "");
            return new ResponseEntity<>("场景执行已启动", HttpStatus.OK);
        } catch (Exception e) {
            log.error("场景执行失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 删除场景
     */
    @DeleteMapping("/{sceneId}")
    public ResponseEntity<Void> deleteScene(@PathVariable String sceneId) {
        sceneService.deleteScene(sceneId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 检查触发条件
     */
    @PostMapping("/trigger/check")
    public ResponseEntity<Boolean> checkTriggerCondition(@RequestBody String condition) {
        boolean result = sceneService.checkTriggerCondition(condition);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}