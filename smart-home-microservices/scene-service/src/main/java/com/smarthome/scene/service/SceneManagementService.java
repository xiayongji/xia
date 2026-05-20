package com.smarthome.scene.service;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.repository.SceneRepository;
import com.smarthome.scene.repository.SceneRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SceneManagementService {

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneRuleRepository sceneRuleRepository;

    @Autowired
    private DroolsRuleEngineService ruleEngineService;

    public Scene createScene(Scene scene) {
        scene.setCreatedAt(LocalDateTime.now());
        scene.setUpdatedAt(LocalDateTime.now());
        if (scene.getEnabled() == null) scene.setEnabled(true);
        if (scene.getExecutionCount() == null) scene.setExecutionCount(0);

        Scene saved = sceneRepository.save(scene);
        log.info("创建场景: {}", saved.getName());

        return saved;
    }

    public Scene updateScene(Long sceneId, Scene scene) {
        Scene existing = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("场景不存在: " + sceneId));

        existing.setName(scene.getName());
        existing.setDescription(scene.getDescription());
        existing.setTriggerType(scene.getTriggerType());
        existing.setTriggerConditions(scene.getTriggerConditions());
        existing.setActions(scene.getActions());
        existing.setEnabled(scene.getEnabled());
        existing.setPriority(scene.getPriority());
        existing.setUpdatedAt(LocalDateTime.now());

        Scene updated = sceneRepository.save(existing);
        log.info("更新场景: {}", updated.getName());

        return updated;
    }

    public void deleteScene(Long sceneId) {
        List<SceneRule> rules = sceneRuleRepository.findBySceneId(sceneId);
        sceneRuleRepository.deleteAll(rules);

        sceneRepository.deleteById(sceneId);
        log.info("删除场景: {}", sceneId);
    }

    public Scene getScene(Long sceneId) {
        return sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("场景不存在: " + sceneId));
    }

    public List<Scene> getAllScenes() {
        return sceneRepository.findAll();
    }

    public List<Scene> getScenesByUser(Long userId) {
        return sceneRepository.findByUserId(userId);
    }

    public List<Scene> getEnabledScenes() {
        return sceneRepository.findByEnabled(true);
    }

    public Scene toggleScene(Long sceneId, Boolean enabled) {
        Scene scene = getScene(sceneId);
        scene.setEnabled(enabled);
        scene.setUpdatedAt(LocalDateTime.now());

        Scene updated = sceneRepository.save(scene);
        log.info("{} 场景: {}", enabled ? "启用" : "禁用", updated.getName());

        return updated;
    }

    public SceneRule addRuleToScene(Long sceneId, SceneRule rule) {
        Scene scene = getScene(sceneId);
        rule.setSceneId(sceneId);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());

        SceneRule saved = sceneRuleRepository.save(rule);

        ruleEngineService.loadRules();

        log.info("为场景 {} 添加规则: {}", scene.getName(), saved.getRuleName());

        return saved;
    }

    public List<SceneRule> getRulesForScene(Long sceneId) {
        return sceneRuleRepository.findBySceneIdOrderByPriorityDesc(sceneId);
    }

    public void removeRule(Long ruleId) {
        sceneRuleRepository.deleteById(ruleId);
        ruleEngineService.loadRules();
        log.info("删除规则: {}", ruleId);
    }

    public SceneRule updateRule(Long ruleId, SceneRule rule) {
        SceneRule existing = sceneRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("规则不存在: " + ruleId));

        existing.setRuleName(rule.getRuleName());
        existing.setRuleType(rule.getRuleType());
        existing.setRuleCondition(rule.getRuleCondition());
        existing.setRuleAction(rule.getRuleAction());
        existing.setPriority(rule.getPriority());
        existing.setEnabled(rule.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());

        SceneRule updated = sceneRuleRepository.save(existing);

        ruleEngineService.loadRules();

        log.info("更新规则: {}", updated.getRuleName());

        return updated;
    }

    public List<Scene> searchScenes(String keyword) {
        List<Scene> allScenes = sceneRepository.findAll();
        return allScenes.stream()
                .filter(s -> s.getName().contains(keyword) ||
                        (s.getDescription() != null && s.getDescription().contains(keyword)))
                .toList();
    }
}