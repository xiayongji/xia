package com.smarthome.scene.service;

import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.repository.SceneRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DroolsRuleEngineService {

    @Autowired
    private SceneRuleRepository sceneRuleRepository;

    private final Map<String, KieContainer> kieContainers = new ConcurrentHashMap<>();
    private KieSession kieSession;

    @PostConstruct
    public void init() {
        loadRules();
    }

    public void loadRules() {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            List<SceneRule> rules = sceneRuleRepository.findByEnabled(true);

            int ruleIndex = 1;
            for (SceneRule rule : rules) {
                String drlContent = generateDrlFromRule(rule);
                kieFileSystem.write("src/main/resources/rules/rule" + ruleIndex + ".drl", drlContent);
                ruleIndex++;
            }

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            kieSession = kieContainer.newKieSession();
            log.info("Drools规则引擎初始化成功，加载了 {} 条规则", rules.size());

        } catch (Exception e) {
            log.error("Drools规则引擎初始化失败", e);
            createDefaultKieSession();
        }
    }

    private String generateDrlFromRule(SceneRule rule) {
        StringBuilder drl = new StringBuilder();
        drl.append("package com.smarthome.scene.rules\n\n");
        drl.append("import com.smarthome.scene.service.SceneExecutionContext\n\n");
        drl.append("rule \"").append(rule.getRuleName()).append("\"\n");
        drl.append("when\n");

        if (rule.getRuleCondition() != null && !rule.getRuleCondition().isEmpty()) {
            drl.append("  $context : SceneExecutionContext(").append(rule.getRuleCondition()).append(")\n");
        }

        drl.append("then\n");
        if (rule.getRuleAction() != null && !rule.getRuleAction().isEmpty()) {
            drl.append("  ").append(rule.getRuleAction()).append("\n");
        }
        drl.append("end\n");

        return drl.toString();
    }

    private void createDefaultKieSession() {
        kieSession = null;
        log.warn("Drools 规则未加载，将使用默认场景评估逻辑");
    }

    public boolean evaluateRules(SceneExecutionContext context) {
        if (kieSession == null) {
            log.warn("KieSession未初始化，使用默认逻辑评估");
            return evaluateDefault(context);
        }

        try {
            kieSession.insert(context);
            int firedRules = kieSession.fireAllRules();
            kieSession.dispose();
            log.debug("规则引擎评估完成，触发了 {} 条规则", firedRules);
            return firedRules > 0;
        } catch (Exception e) {
            log.error("规则引擎评估失败", e);
            return evaluateDefault(context);
        }
    }

    private boolean evaluateDefault(SceneExecutionContext context) {
        return context.getSceneId() != null;
    }

    public void addRule(SceneRule rule) {
        sceneRuleRepository.save(rule);
        loadRules();
        log.info("添加新规则: {}", rule.getRuleName());
    }

    public void updateRule(SceneRule rule) {
        sceneRuleRepository.save(rule);
        loadRules();
        log.info("更新规则: {}", rule.getRuleName());
    }

    public void deleteRule(Long ruleId) {
        sceneRuleRepository.deleteById(ruleId);
        loadRules();
        log.info("删除规则: {}", ruleId);
    }

    public List<SceneRule> getAllRules() {
        return sceneRuleRepository.findAll();
    }
}