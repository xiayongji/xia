package com.smarthome.scene.service;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 场景管理服务
 * 实现场景的创建、配置、执行等核心功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {
    
    private final SceneRepository sceneRepository;
    private final SceneExecutionService executionService;
    private final RuleEngineService ruleEngineService;
    
    /**
     * 创建场景
     */
    public Scene createScene(Scene scene) {
        log.info("创建场景: {}", scene.getName());
        
        scene.setCreatedAt(LocalDateTime.now());
        
        // 设置场景中规则的关联关系
        if (scene.getRules() != null) {
            for (SceneRule rule : scene.getRules()) {
                rule.setScene(scene);
                if (rule.getActions() != null) {
                    rule.getActions().forEach(action -> action.setRule(rule));
                }
            }
        }
        
        Scene savedScene = sceneRepository.save(scene);
        log.info("场景创建成功: {}", savedScene.getSceneId());
        return savedScene;
    }
    
    /**
     * 获取场景信息
     */
    public Optional<Scene> getScene(String sceneId) {
        return sceneRepository.findBySceneId(sceneId);
    }
    
    /**
     * 获取所有场景
     */
    public List<Scene> getAllScenes() {
        return sceneRepository.findAll();
    }
    
    /**
     * 获取启用的场景
     */
    public List<Scene> getActiveScenes() {
        return sceneRepository.findActiveScenes();
    }
    
    /**
     * 更新场景
     */
    public Scene updateScene(Scene scene) {
        log.info("更新场景: {}", scene.getSceneId());
        
        Optional<Scene> existingScene = sceneRepository.findBySceneId(scene.getSceneId());
        if (existingScene.isEmpty()) {
            throw new RuntimeException("场景不存在: " + scene.getSceneId());
        }
        
        scene.setId(existingScene.get().getId());
        scene.setUpdatedAt(LocalDateTime.now());
        
        // 更新关联关系
        if (scene.getRules() != null) {
            for (SceneRule rule : scene.getRules()) {
                rule.setScene(scene);
                if (rule.getActions() != null) {
                    rule.getActions().forEach(action -> action.setRule(rule));
                }
            }
        }
        
        Scene updatedScene = sceneRepository.save(scene);
        log.info("场景更新成功: {}", scene.getSceneId());
        return updatedScene;
    }
    
    /**
     * 切换场景状态
     */
    public void toggleScene(String sceneId, boolean enabled) {
        Optional<Scene> sceneOptional = sceneRepository.findBySceneId(sceneId);
        sceneOptional.ifPresent(scene -> {
            scene.setEnabled(enabled);
            scene.setUpdatedAt(LocalDateTime.now());
            sceneRepository.save(scene);
            log.info("场景状态更新: {} -> {}", sceneId, enabled);
        });
    }
    
    /**
     * 执行场景
     */
    public void executeScene(String sceneId, String triggerType, String triggerCondition) {
        Optional<Scene> sceneOptional = sceneRepository.findBySceneId(sceneId);
        if (sceneOptional.isEmpty()) {
            throw new RuntimeException("场景不存在: " + sceneId);
        }
        
        Scene scene = sceneOptional.get();
        if (!scene.isEnabled()) {
            throw new RuntimeException("场景未启用: " + sceneId);
        }
        
        // 异步执行场景
        executionService.executeSceneAsync(scene, triggerType, triggerCondition)
            .thenAccept(execution -> {
                log.info("场景执行完成: {}, 状态: {}", sceneId, execution.getStatus());
                // 这里可以保存执行记录到数据库
            });
    }
    
    /**
     * 删除场景
     */
    public void deleteScene(String sceneId) {
        Optional<Scene> sceneOptional = sceneRepository.findBySceneId(sceneId);
        sceneOptional.ifPresent(scene -> {
            sceneRepository.delete(scene);
            log.info("场景删除成功: {}", sceneId);
        });
    }
    
    /**
     * 检查触发条件
     */
    public boolean checkTriggerCondition(String condition) {
        // 使用规则引擎检查触发条件
        TriggerFact fact = new TriggerFact(condition);
        return ruleEngineService.executeRule(fact);
    }
    
    /**
     * 触发条件事实类
     */
    public static class TriggerFact {
        private String condition;
        private boolean matched = false;
        
        public TriggerFact(String condition) {
            this.condition = condition;
        }
        
        // getters and setters
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
    }
}