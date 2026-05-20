package com.smarthome.scene.service;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneExecution;
import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.repository.SceneExecutionRepository;
import com.smarthome.scene.repository.SceneRepository;
import com.smarthome.scene.repository.SceneRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SceneExecutionService {

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneRuleRepository sceneRuleRepository;

    @Autowired
    private SceneExecutionRepository sceneExecutionRepository;

    @Autowired
    private DroolsRuleEngineService ruleEngineService;

    @Autowired
    private SceneActionExecutor actionExecutor;

    @Autowired
    private SceneDeviceActionService sceneDeviceActionService;

    public SceneExecution executeScene(Long sceneId, String triggerType, String triggerSource) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("场景不存在: " + sceneId));

        if (Boolean.FALSE.equals(scene.getEnabled())) {
            throw new RuntimeException("场景已禁用: " + scene.getName());
        }

        SceneExecution execution = SceneExecution.builder()
                .sceneId(sceneId)
                .triggerType(triggerType)
                .triggerSource(triggerSource)
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .build();
        execution = sceneExecutionRepository.save(execution);

        try {
            SceneExecutionContext context = buildContext(scene, triggerType, triggerSource);

            boolean manualTrigger = "MANUAL".equalsIgnoreCase(triggerType);
            boolean rulesMatch = manualTrigger || ruleEngineService.evaluateRules(context);

            if (!rulesMatch || !context.getExecutionAllowed()) {
                execution.setStatus("SKIPPED");
                execution.setResult("规则评估未通过或执行被阻止");
                execution.setEndTime(LocalDateTime.now());
                return sceneExecutionRepository.save(execution);
            }

            int deviceActions = sceneDeviceActionService.executeSceneActions(scene);
            executeActions(scene, context, execution);
            execution.setActionsExecuted(execution.getActionsExecuted() + deviceActions);
            execution.setResult("成功执行设备动作 " + deviceActions + " 项，规则动作 "
                    + execution.getActionsExecuted() + "/" + execution.getActionsTotal());

            execution.setStatus("SUCCESS");
            execution.setEndTime(LocalDateTime.now());

            scene.setLastExecutedAt(LocalDateTime.now());
            scene.setExecutionCount(scene.getExecutionCount() + 1);
            sceneRepository.save(scene);

        } catch (Exception e) {
            log.error("场景执行失败: {}", sceneId, e);
            execution.setStatus("FAILED");
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
        }

        return sceneExecutionRepository.save(execution);
    }

    @Async
    public void executeSceneAsync(Long sceneId, String triggerType, String triggerSource) {
        executeScene(sceneId, triggerType, triggerSource);
    }

    private SceneExecutionContext buildContext(Scene scene, String triggerType, String triggerSource) {
        SceneExecutionContext context = new SceneExecutionContext();
        context.setSceneId(scene.getId());
        context.setSceneName(scene.getName());
        context.setUserId(scene.getUserId());
        context.setTriggerType(triggerType);
        context.setTriggerSource(triggerSource);

        return context;
    }

    private void executeActions(Scene scene, SceneExecutionContext context, SceneExecution execution) {
        List<SceneRule> rules = sceneRuleRepository.findBySceneIdAndEnabled(scene.getId(), true);

        int totalActions = rules.size();
        int executedActions = 0;

        for (SceneRule rule : rules) {
            try {
                actionExecutor.executeRule(rule, context);
                executedActions++;
            } catch (Exception e) {
                log.error("执行规则动作失败: {}", rule.getRuleName(), e);
            }
        }

        execution.setActionsTotal(totalActions);
        execution.setActionsExecuted(executedActions);
        execution.setResult("成功执行 " + executedActions + "/" + totalActions + " 个动作");
    }

    public List<SceneExecution> getExecutionHistory(Long sceneId, int limit) {
        List<SceneExecution> executions = sceneExecutionRepository.findBySceneId(sceneId);
        return executions.size() > limit
                ? executions.subList(0, limit)
                : executions;
    }

    public Map<String, Object> getSceneStatistics(Long sceneId) {
        List<SceneExecution> executions = sceneExecutionRepository.findBySceneId(sceneId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExecutions", executions.size());

        long successCount = executions.stream()
                .filter(e -> "SUCCESS".equals(e.getStatus()))
                .count();
        stats.put("successCount", successCount);
        stats.put("successRate", executions.isEmpty() ? 0 : (double) successCount / executions.size());

        double avgDuration = executions.stream()
                .filter(e -> e.getEndTime() != null)
                .mapToLong(e -> java.time.Duration.between(e.getStartTime(), e.getEndTime()).toMillis())
                .average()
                .orElse(0);
        stats.put("avgDurationMs", (long) avgDuration);

        return stats;
    }
}