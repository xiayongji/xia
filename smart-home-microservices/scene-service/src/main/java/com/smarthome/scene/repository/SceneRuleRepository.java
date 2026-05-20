package com.smarthome.scene.repository;

import com.smarthome.scene.entity.SceneRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SceneRuleRepository extends JpaRepository<SceneRule, Long> {

    List<SceneRule> findBySceneId(Long sceneId);

    List<SceneRule> findBySceneIdAndEnabled(Long sceneId, Boolean enabled);

    List<SceneRule> findByEnabled(Boolean enabled);

    List<SceneRule> findByRuleType(String ruleType);

    List<SceneRule> findBySceneIdOrderByPriorityDesc(Long sceneId);
}