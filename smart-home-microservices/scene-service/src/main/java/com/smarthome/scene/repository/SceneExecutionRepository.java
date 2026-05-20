package com.smarthome.scene.repository;

import com.smarthome.scene.entity.SceneExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SceneExecutionRepository extends JpaRepository<SceneExecution, Long> {

    List<SceneExecution> findBySceneId(Long sceneId);

    List<SceneExecution> findByStatus(String status);

    List<SceneExecution> findBySceneIdAndStatus(Long sceneId, String status);

    List<SceneExecution> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}