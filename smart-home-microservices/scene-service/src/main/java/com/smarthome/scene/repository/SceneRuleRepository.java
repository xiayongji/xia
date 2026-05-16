package com.smarthome.scene.repository;

import com.smarthome.scene.entity.SceneRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRuleRepository extends JpaRepository<SceneRule, Long> {
    
    List<SceneRule> findByEnabledTrue();
    
    List<SceneRule> findBySceneId(Long sceneId);
    
    List<SceneRule> findByPriorityGreaterThanEqual(Integer priority);
}