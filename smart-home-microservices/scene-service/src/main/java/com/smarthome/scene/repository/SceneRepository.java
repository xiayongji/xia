package com.smarthome.scene.repository;

import com.smarthome.scene.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    Optional<Scene> findBySceneId(String sceneId);
    List<Scene> findByEnabled(boolean enabled);
    
    @Query("SELECT s FROM Scene s WHERE s.enabled = true")
    List<Scene> findActiveScenes();
}