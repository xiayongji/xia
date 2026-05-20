package com.smarthome.scene.repository;

import com.smarthome.scene.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {

    List<Scene> findByUserId(Long userId);

    List<Scene> findByUserIdAndEnabled(Long userId, Boolean enabled);

    List<Scene> findByEnabled(Boolean enabled);

    List<Scene> findByTriggerType(String triggerType);

    List<Scene> findByUserIdOrderByPriorityDesc(Long userId);
}