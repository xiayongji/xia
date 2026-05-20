package com.smarthome.scene.config;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSceneInitializer {

    private final SceneRepository sceneRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initDefaultScenes() {
        if (sceneRepository.count() > 0) {
            return;
        }

        createScene("回家模式", "开启灯光和空调", "manual");
        createScene("离家模式", "关闭灯光和空调", "manual");
        createScene("睡眠模式", "关闭灯光", "manual");
        createScene("阅读模式", "调节灯光亮度", "manual");

        log.info("已初始化默认智能场景");
    }

    private void createScene(String name, String description, String triggerType) {
        Scene scene = new Scene();
        scene.setName(name);
        scene.setDescription(description);
        scene.setTriggerType(triggerType);
        scene.setUserId(1L);
        scene.setEnabled(true);
        scene.setPriority(0);
        sceneRepository.save(scene);
    }
}
