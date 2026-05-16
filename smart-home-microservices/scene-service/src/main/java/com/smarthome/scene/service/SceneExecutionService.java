package com.smarthome.scene.service;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneExecution;
import com.smarthome.scene.entity.SceneRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 场景执行服务
 * 采用异步处理机制，确保场景联动的实时性和可靠性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneExecutionService {
    
    private final ActionOrchestratorService actionOrchestratorService;
    
    /**
     * 异步执行场景
     * @param scene 场景信息
     * @param triggerType 触发类型
     * @param triggerCondition 触发条件
     * @return 执行结果
     */
    @Async
    public CompletableFuture<SceneExecution> executeSceneAsync(Scene scene, String triggerType, String triggerCondition) {
        return CompletableFuture.supplyAsync(() -> {
            SceneExecution execution = new SceneExecution();
            execution.setSceneId(scene.getSceneId());
            execution.setTriggerType(triggerType);
            execution.setTriggerCondition(triggerCondition);
            
            try {
                log.info("开始执行场景: {}", scene.getName());
                
                // 执行场景中的所有规则
                for (SceneRule rule : scene.getRules()) {
                    if (rule.isEnabled()) {
                        executeRuleActions(rule, execution);
                    }
                }
                
                execution.setStatus("success");
                execution.setEndTime(LocalDateTime.now());
                log.info("场景执行完成: {}", scene.getName());
                
            } catch (Exception e) {
                execution.setStatus("failed");
                execution.setExecutionLog("执行失败: " + e.getMessage());
                log.error("场景执行失败: {}", scene.getName(), e);
            }
            
            return execution;
        });
    }
    
    /**
     * 执行规则动作
     * @param rule 场景规则
     * @param execution 执行记录
     */
    private void executeRuleActions(SceneRule rule, SceneExecution execution) {
        log.debug("执行规则: {}", rule.getRuleId());
        
        try {
            actionOrchestratorService.executeRuleActions(rule);
            execution.setSuccessActions(execution.getSuccessActions() + 
                (rule.getActions() != null ? rule.getActions().size() : 0));
        } catch (Exception e) {
            execution.setFailedActions(execution.getFailedActions() + 1);
            log.error("规则执行失败: {}", rule.getRuleId(), e);
        }
        execution.setTotalActions(execution.getTotalActions() + 
            (rule.getActions() != null ? rule.getActions().size() : 0));
    }
}