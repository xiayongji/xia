package com.smarthome.scene.service;

import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.entity.TriggerEvent;
import com.smarthome.scene.repository.SceneRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DroolsRuleEngineService {

    private final SceneRuleRepository ruleRepository;
    
    private KieContainer kieContainer;

    @PostConstruct
    public void init() {
        try {
            reloadAllRules();
            log.info("Drools规则引擎初始化完成");
        } catch (Exception e) {
            log.warn("Drools规则引擎初始化失败，将使用简化规则引擎: {}", e.getMessage());
        }
    }

    public void reloadAllRules() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        List<SceneRule> rules = ruleRepository.findByEnabled(true);
        for (SceneRule rule : rules) {
            String fileName = "rules/" + rule.getId() + "_" + rule.getRuleName() + ".drl";
            kfs.write(ResourceFactory.newByteArrayResource(
                rule.getDrlContent().getBytes(StandardCharsets.UTF_8)
            ).setResourceType(ResourceType.DRL).setSourcePath(fileName));
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("规则编译失败: " + kieBuilder.getResults());
        }

        KieModule kieModule = kieBuilder.getKieModule();
        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        
        log.info("规则引擎重新加载完成，加载 {} 条规则", rules.size());
    }

    public void addRule(SceneRule rule) {
        if (kieContainer == null) {
            log.warn("Drools引擎未初始化，跳过规则添加");
            return;
        }

        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        KieBase kieBase = kieContainer.getKieBase();
        for (org.kie.api.definition.rule.Rule drlRule : kieBase.getRules()) {
            kfs.write("rules/" + drlRule.getName() + ".drl", drlRule.toString());
        }

        String fileName = "rules/" + rule.getId() + "_" + rule.getRuleName() + ".drl";
        kfs.write(ResourceFactory.newByteArrayResource(
            rule.getDrlContent().getBytes(StandardCharsets.UTF_8)
        ).setResourceType(ResourceType.DRL).setSourcePath(fileName));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        
        log.info("规则添加成功: {}", rule.getRuleName());
    }

    public void executeRules(TriggerEvent event) {
        if (kieContainer == null) {
            executeSimpleRules(event);
            return;
        }

        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.setGlobal("log", log);
            kieSession.insert(event);
            int firedCount = kieSession.fireAllRules();
            log.debug("执行 {} 条规则", firedCount);
        } finally {
            kieSession.dispose();
        }
    }

    private void executeSimpleRules(TriggerEvent event) {
        List<SceneRule> rules = ruleRepository.findByEnabled(true);
        
        for (SceneRule rule : rules) {
            if (matchesCondition(rule, event)) {
                executeRuleActions(rule, event);
            }
        }
    }

    private boolean matchesCondition(SceneRule rule, TriggerEvent event) {
        String condition = rule.getDrlContent().toLowerCase();
        
        if (condition.contains("temperature") && event.getSensorType() != null) {
            if (condition.contains(">") && event.getValue() != null) {
                double threshold = extractThreshold(condition, ">");
                return event.getValue() > threshold;
            }
            if (condition.contains("<") && event.getValue() != null) {
                double threshold = extractThreshold(condition, "<");
                return event.getValue() < threshold;
            }
        }
        
        return condition.contains(event.getTriggerType());
    }

    private double extractThreshold(String condition, String operator) {
        int index = condition.indexOf(operator);
        if (index > 0) {
            String substring = condition.substring(index + 1).trim();
            try {
                return Double.parseDouble(substring.split(" ")[0]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private void executeRuleActions(SceneRule rule, TriggerEvent event) {
        log.info("执行简化规则: {} - 触发事件: {}", rule.getRuleName(), event.getTriggerType());
    }

    public boolean isEngineInitialized() {
        return kieContainer != null;
    }
}