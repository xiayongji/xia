package com.smarthome.scene.service;

import com.smarthome.scene.entity.SceneAction;
import com.smarthome.scene.entity.SceneRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionOrchestratorService {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 执行动作列表（顺序执行）
     */
    public void executeActionsSequential(List<SceneAction> actions) {
        log.info("开始顺序执行动作，共 {} 个动作", actions.size());
        
        List<SceneAction> sortedActions = actions.stream()
                .filter(SceneAction::isEnabled)
                .sorted(Comparator.comparingInt(SceneAction::getOrderIndex))
                .collect(Collectors.toList());

        for (SceneAction action : sortedActions) {
            executeAction(action);
            handleDelay(action);
        }
        
        log.info("顺序执行完成");
    }

    /**
     * 执行动作列表（并行执行）
     */
    public void executeActionsParallel(List<SceneAction> actions) {
        log.info("开始并行执行动作，共 {} 个动作", actions.size());
        
        List<SceneAction> enabledActions = actions.stream()
                .filter(SceneAction::isEnabled)
                .collect(Collectors.toList());

        List<CompletableFuture<Void>> futures = enabledActions.stream()
                .map(action -> CompletableFuture.runAsync(() -> {
                    executeAction(action);
                    handleDelay(action);
                }, executorService))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        log.info("并行执行完成");
    }

    /**
     * 执行带条件分支的动作
     */
    public void executeActionsWithCondition(List<SceneAction> actions, boolean conditionResult) {
        log.info("条件分支执行，条件结果: {}", conditionResult);
        
        List<SceneAction> filteredActions = actions.stream()
                .filter(SceneAction::isEnabled)
                .filter(action -> matchCondition(action, conditionResult))
                .sorted(Comparator.comparingInt(SceneAction::getOrderIndex))
                .collect(Collectors.toList());

        executeActionsSequential(filteredActions);
    }

    /**
     * 执行规则中的动作
     */
    public void executeRuleActions(SceneRule rule) {
        if (rule.getActions() == null || rule.getActions().isEmpty()) {
            log.warn("规则 {} 没有关联动作", rule.getRuleId());
            return;
        }

        String executionMode = getExecutionMode(rule);
        
        switch (executionMode.toLowerCase()) {
            case "parallel":
                executeActionsParallel(rule.getActions());
                break;
            case "conditional":
                boolean conditionResult = evaluateCondition(rule.getCondition());
                executeActionsWithCondition(rule.getActions(), conditionResult);
                break;
            case "sequential":
            default:
                executeActionsSequential(rule.getActions());
                break;
        }
    }

    /**
     * 执行单个动作
     */
    private void executeAction(SceneAction action) {
        log.info("执行动作: {} -> {} -> {}", action.getActionId(), action.getDeviceId(), action.getCommand());
        
        try {
            Map<String, Object> params = parseParameters(action.getParameters());
            
            switch (action.getType() != null ? action.getType() : "device_control") {
                case "notification":
                    sendNotification(action, params);
                    break;
                case "delay":
                    handleDelay(action);
                    break;
                case "device_control":
                default:
                    sendDeviceCommand(action, params);
                    break;
            }
        } catch (Exception e) {
            log.error("执行动作失败: {}", action.getActionId(), e);
        }
    }

    /**
     * 发送设备命令
     */
    private void sendDeviceCommand(SceneAction action, Map<String, Object> params) {
        String deviceId = action.getDeviceId();
        String command = action.getCommand();
        
        log.info("发送命令到设备: {} - {} ({})", deviceId, command, params);
        
        // 调用设备服务发送命令
        // 这里应该通过Feign客户端或消息队列调用设备服务
    }

    /**
     * 发送通知
     */
    private void sendNotification(SceneAction action, Map<String, Object> params) {
        String message = (String) params.getOrDefault("message", "场景触发通知");
        String target = (String) params.getOrDefault("target", "user");
        
        log.info("发送通知: {} -> {}", target, message);
        
        // 发送通知到用户
    }

    /**
     * 处理延迟
     */
    private void handleDelay(SceneAction action) {
        if (action.getDelaySeconds() > 0) {
            log.info("延迟 {} 秒", action.getDelaySeconds());
            try {
                Thread.sleep(action.getDelaySeconds() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("延迟被中断", e);
            }
        }
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(String condition) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        
        try {
            Map<String, Object> conditionMap = objectMapper.readValue(condition, Map.class);
            Object value = conditionMap.get("value");
            Object expected = conditionMap.get("expected");
            
            return Objects.equals(value, expected);
        } catch (JsonProcessingException e) {
            log.error("条件解析失败: {}", condition, e);
            return true;
        }
    }

    /**
     * 检查动作是否匹配条件
     */
    private boolean matchCondition(SceneAction action, boolean conditionResult) {
        if (action.getParameters() == null || action.getParameters().isEmpty()) {
            return true;
        }
        
        try {
            Map<String, Object> params = objectMapper.readValue(action.getParameters(), Map.class);
            String conditionMatch = (String) params.get("conditionMatch");
            
            if ("true".equalsIgnoreCase(conditionMatch)) {
                return conditionResult;
            } else if ("false".equalsIgnoreCase(conditionMatch)) {
                return !conditionResult;
            }
        } catch (JsonProcessingException e) {
            log.error("参数解析失败: {}", action.getActionId(), e);
        }
        
        return true;
    }

    /**
     * 解析参数
     */
    private Map<String, Object> parseParameters(String parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(parameters, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("参数解析失败: {}", parameters, e);
            return new HashMap<>();
        }
    }

    /**
     * 获取执行模式
     */
    private String getExecutionMode(SceneRule rule) {
        if (rule.getCondition() != null && !rule.getCondition().isEmpty()) {
            String condition = rule.getCondition();
            if (condition.contains("if") || condition.contains("condition")) {
                return "conditional";
            }
        }
        
        return "sequential";
    }
}