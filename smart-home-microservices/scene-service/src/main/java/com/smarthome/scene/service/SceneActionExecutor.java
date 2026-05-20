package com.smarthome.scene.service;

import com.smarthome.scene.entity.SceneRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SceneActionExecutor {

    public void executeRule(SceneRule rule, SceneExecutionContext context) {
        log.info("执行规则动作: {} for scene {}", rule.getRuleName(), context.getSceneId());

        String actionType = determineActionType(rule);

        switch (actionType) {
            case "DEVICE_CONTROL":
                executeDeviceControl(rule, context);
                break;
            case "NOTIFICATION":
                executeNotification(rule, context);
                break;
            case "SCENE_TRIGGER":
                executeSceneTrigger(rule, context);
                break;
            case "DELAY":
                executeDelay(rule, context);
                break;
            default:
                log.warn("未知的动作类型: {}", actionType);
                executeGenericAction(rule, context);
        }
    }

    private String determineActionType(SceneRule rule) {
        String action = rule.getRuleAction();
        if (action == null) return "UNKNOWN";

        action = action.toLowerCase();

        if (action.contains("device") || action.contains("turn_on") ||
            action.contains("turn_off") || action.contains("set_")) {
            return "DEVICE_CONTROL";
        }
        if (action.contains("notify") || action.contains("send") ||
            action.contains("alert")) {
            return "NOTIFICATION";
        }
        if (action.contains("scene") || action.contains("trigger")) {
            return "SCENE_TRIGGER";
        }
        if (action.contains("delay") || action.contains("sleep")) {
            return "DELAY";
        }

        return "GENERIC";
    }

    private void executeDeviceControl(SceneRule rule, SceneExecutionContext context) {
        log.debug("执行设备控制动作: {}", rule.getRuleAction());

        context.addUserContext("lastAction", "DEVICE_CONTROL");
        context.addUserContext("lastActionTime", System.currentTimeMillis());
    }

    private void executeNotification(SceneRule rule, SceneExecutionContext context) {
        log.debug("执行通知动作: {}", rule.getRuleAction());

        context.addUserContext("lastAction", "NOTIFICATION");
        context.addUserContext("notificationSent", true);
    }

    private void executeSceneTrigger(SceneRule rule, SceneExecutionContext context) {
        log.debug("执行场景触发动作: {}", rule.getRuleAction());

        context.addUserContext("lastAction", "SCENE_TRIGGER");
        context.addUserContext("triggeredScene", rule.getSceneId());
    }

    private void executeDelay(SceneRule rule, SceneExecutionContext context) {
        log.debug("执行延迟动作: {}", rule.getRuleAction());

        context.addUserContext("lastAction", "DELAY");
    }

    private void executeGenericAction(SceneRule rule, SceneExecutionContext context) {
        log.debug("执行通用动作: {}", rule.getRuleAction());

        context.addUserContext("lastAction", "GENERIC");
        context.addUserContext("actionResult", "executed");
    }

    public Map<String, Object> getActionResult(SceneExecutionContext context) {
        return context.getUserContext();
    }
}