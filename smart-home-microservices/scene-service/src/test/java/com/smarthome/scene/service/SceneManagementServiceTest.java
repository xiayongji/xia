package com.smarthome.scene.service;

import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.repository.SceneRepository;
import com.smarthome.scene.repository.SceneRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SceneManagementServiceTest {

    @Mock
    private SceneRepository sceneRepository;

    @Mock
    private SceneRuleRepository sceneRuleRepository;

    @Mock
    private DroolsRuleEngineService ruleEngineService;

    @InjectMocks
    private SceneManagementService sceneManagementService;

    @Test
    void testCreateScene() {
        Scene scene = Scene.builder()
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .triggerType("manual")
                .build();

        Scene savedScene = Scene.builder()
                .id(1L)
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .triggerType("manual")
                .enabled(true)
                .executionCount(0)
                .build();

        when(sceneRepository.save(any(Scene.class))).thenReturn(savedScene);

        Scene result = sceneManagementService.createScene(scene);

        assertNotNull(result);
        assertTrue(result.getEnabled());
        assertEquals(0, result.getExecutionCount());
        verify(sceneRepository, times(1)).save(any(Scene.class));
    }

    @Test
    void testCreateScene_WithNullEnabled() {
        Scene scene = Scene.builder()
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .triggerType("manual")
                .enabled(null)
                .build();

        Scene savedScene = Scene.builder()
                .id(1L)
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .triggerType("manual")
                .enabled(true)
                .executionCount(0)
                .build();

        when(sceneRepository.save(any(Scene.class))).thenReturn(savedScene);

        Scene result = sceneManagementService.createScene(scene);

        assertNotNull(result);
        assertTrue(result.getEnabled());
        verify(sceneRepository, times(1)).save(any(Scene.class));
    }

    @Test
    void testUpdateScene() {
        Long sceneId = 1L;

        Scene existingScene = Scene.builder()
                .id(sceneId)
                .name("Old Name")
                .description("Old Description")
                .userId(1L)
                .triggerType("manual")
                .enabled(true)
                .executionCount(5)
                .build();

        Scene updateRequest = Scene.builder()
                .name("Updated Name")
                .description("Updated Description")
                .triggerType("auto")
                .triggerConditions("{\"temp\":30}")
                .actions("{\"action\":\"turn_on\"}")
                .enabled(false)
                .priority(2)
                .build();

        Scene updatedScene = Scene.builder()
                .id(sceneId)
                .name("Updated Name")
                .description("Updated Description")
                .userId(1L)
                .triggerType("auto")
                .triggerConditions("{\"temp\":30}")
                .actions("{\"action\":\"turn_on\"}")
                .enabled(false)
                .priority(2)
                .executionCount(5)
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(existingScene));
        when(sceneRepository.save(any(Scene.class))).thenReturn(updatedScene);

        Scene result = sceneManagementService.updateScene(sceneId, updateRequest);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("auto", result.getTriggerType());
        assertEquals("{\"temp\":30}", result.getTriggerConditions());
        assertEquals("{\"action\":\"turn_on\"}", result.getActions());
        assertFalse(result.getEnabled());
        assertEquals(2, result.getPriority());
        verify(sceneRepository, times(1)).findById(sceneId);
        verify(sceneRepository, times(1)).save(any(Scene.class));
    }

    @Test
    void testUpdateScene_NotFound() {
        Long sceneId = 999L;

        Scene updateRequest = Scene.builder()
                .name("Updated Name")
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sceneManagementService.updateScene(sceneId, updateRequest));

        assertTrue(exception.getMessage().contains("场景不存在"));
        verify(sceneRepository, never()).save(any(Scene.class));
    }

    @Test
    void testDeleteScene() {
        Long sceneId = 1L;

        SceneRule rule1 = SceneRule.builder()
                .id(1L)
                .sceneId(sceneId)
                .ruleName("Rule 1")
                .enabled(true)
                .build();

        SceneRule rule2 = SceneRule.builder()
                .id(2L)
                .sceneId(sceneId)
                .ruleName("Rule 2")
                .enabled(true)
                .build();

        List<SceneRule> rules = List.of(rule1, rule2);

        when(sceneRuleRepository.findBySceneId(sceneId)).thenReturn(rules);
        doNothing().when(sceneRuleRepository).deleteAll(rules);
        doNothing().when(sceneRepository).deleteById(sceneId);

        sceneManagementService.deleteScene(sceneId);

        verify(sceneRuleRepository, times(1)).findBySceneId(sceneId);
        verify(sceneRuleRepository, times(1)).deleteAll(rules);
        verify(sceneRepository, times(1)).deleteById(sceneId);
    }

    @Test
    void testGetScene() {
        Long sceneId = 1L;

        Scene scene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .enabled(true)
                .executionCount(0)
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(scene));

        Scene result = sceneManagementService.getScene(sceneId);

        assertNotNull(result);
        assertEquals(sceneId, result.getId());
        assertEquals("Test Scene", result.getName());
        verify(sceneRepository, times(1)).findById(sceneId);
    }

    @Test
    void testGetScene_NotFound() {
        Long sceneId = 999L;

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sceneManagementService.getScene(sceneId));

        assertTrue(exception.getMessage().contains("场景不存在"));
        verify(sceneRepository, times(1)).findById(sceneId);
    }

    @Test
    void testGetAllScenes() {
        Scene scene1 = Scene.builder()
                .id(1L)
                .name("Scene 1")
                .enabled(true)
                .build();

        Scene scene2 = Scene.builder()
                .id(2L)
                .name("Scene 2")
                .enabled(false)
                .build();

        List<Scene> scenes = List.of(scene1, scene2);

        when(sceneRepository.findAll()).thenReturn(scenes);

        List<Scene> result = sceneManagementService.getAllScenes();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sceneRepository, times(1)).findAll();
    }

    @Test
    void testGetScenesByUser() {
        Long userId = 1L;

        Scene scene1 = Scene.builder()
                .id(1L)
                .name("Scene 1")
                .userId(userId)
                .enabled(true)
                .build();

        Scene scene2 = Scene.builder()
                .id(2L)
                .name("Scene 2")
                .userId(userId)
                .enabled(true)
                .build();

        List<Scene> scenes = List.of(scene1, scene2);

        when(sceneRepository.findByUserId(userId)).thenReturn(scenes);

        List<Scene> result = sceneManagementService.getScenesByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(sceneRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetEnabledScenes() {
        Scene scene1 = Scene.builder()
                .id(1L)
                .name("Scene 1")
                .enabled(true)
                .build();

        Scene scene2 = Scene.builder()
                .id(2L)
                .name("Scene 2")
                .enabled(true)
                .build();

        List<Scene> scenes = List.of(scene1, scene2);

        when(sceneRepository.findByEnabled(true)).thenReturn(scenes);

        List<Scene> result = sceneManagementService.getEnabledScenes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getEnabled());
        assertTrue(result.get(1).getEnabled());
        verify(sceneRepository, times(1)).findByEnabled(true);
    }

    @Test
    void testToggleScene_Enable() {
        Long sceneId = 1L;

        Scene existingScene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .enabled(false)
                .executionCount(5)
                .build();

        Scene toggledScene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .enabled(true)
                .executionCount(5)
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(existingScene));
        when(sceneRepository.save(any(Scene.class))).thenReturn(toggledScene);

        Scene result = sceneManagementService.toggleScene(sceneId, true);

        assertNotNull(result);
        assertTrue(result.getEnabled());
        verify(sceneRepository, times(1)).findById(sceneId);
        verify(sceneRepository, times(1)).save(any(Scene.class));
    }

    @Test
    void testToggleScene_Disable() {
        Long sceneId = 1L;

        Scene existingScene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .enabled(true)
                .executionCount(5)
                .build();

        Scene toggledScene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .enabled(false)
                .executionCount(5)
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(existingScene));
        when(sceneRepository.save(any(Scene.class))).thenReturn(toggledScene);

        Scene result = sceneManagementService.toggleScene(sceneId, false);

        assertNotNull(result);
        assertFalse(result.getEnabled());
        verify(sceneRepository, times(1)).findById(sceneId);
        verify(sceneRepository, times(1)).save(any(Scene.class));
    }

    @Test
    void testAddRuleToScene() {
        Long sceneId = 1L;

        Scene scene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .enabled(true)
                .executionCount(0)
                .build();

        SceneRule rule = SceneRule.builder()
                .ruleName("Test Rule")
                .ruleType("temperature")
                .ruleCondition("temperature > 30")
                .ruleAction("turnOnAC()")
                .priority(1)
                .enabled(true)
                .build();

        SceneRule savedRule = SceneRule.builder()
                .id(1L)
                .sceneId(sceneId)
                .ruleName("Test Rule")
                .ruleType("temperature")
                .ruleCondition("temperature > 30")
                .ruleAction("turnOnAC()")
                .priority(1)
                .enabled(true)
                .build();

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(scene));
        when(sceneRuleRepository.save(any(SceneRule.class))).thenReturn(savedRule);
        doNothing().when(ruleEngineService).loadRules();

        SceneRule result = sceneManagementService.addRuleToScene(sceneId, rule);

        assertNotNull(result);
        assertEquals(sceneId, result.getSceneId());
        assertEquals("Test Rule", result.getRuleName());
        verify(sceneRepository, times(1)).findById(sceneId);
        verify(sceneRuleRepository, times(1)).save(any(SceneRule.class));
        verify(ruleEngineService, times(1)).loadRules();
    }

    @Test
    void testGetRulesForScene() {
        Long sceneId = 1L;

        SceneRule rule1 = SceneRule.builder()
                .id(1L)
                .sceneId(sceneId)
                .ruleName("Rule 1")
                .priority(2)
                .build();

        SceneRule rule2 = SceneRule.builder()
                .id(2L)
                .sceneId(sceneId)
                .ruleName("Rule 2")
                .priority(1)
                .build();

        List<SceneRule> rules = List.of(rule1, rule2);

        when(sceneRuleRepository.findBySceneIdOrderByPriorityDesc(sceneId)).thenReturn(rules);

        List<SceneRule> result = sceneManagementService.getRulesForScene(sceneId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Rule 1", result.get(0).getRuleName());
        verify(sceneRuleRepository, times(1)).findBySceneIdOrderByPriorityDesc(sceneId);
    }

    @Test
    void testRemoveRule() {
        Long ruleId = 1L;

        doNothing().when(sceneRuleRepository).deleteById(ruleId);
        doNothing().when(ruleEngineService).loadRules();

        sceneManagementService.removeRule(ruleId);

        verify(sceneRuleRepository, times(1)).deleteById(ruleId);
        verify(ruleEngineService, times(1)).loadRules();
    }

    @Test
    void testUpdateRule() {
        Long ruleId = 1L;

        SceneRule existingRule = SceneRule.builder()
                .id(ruleId)
                .sceneId(1L)
                .ruleName("Old Rule")
                .ruleType("temperature")
                .ruleCondition("temp > 25")
                .ruleAction("doNothing()")
                .priority(1)
                .enabled(true)
                .build();

        SceneRule updateRequest = SceneRule.builder()
                .ruleName("Updated Rule")
                .ruleType("humidity")
                .ruleCondition("humidity > 60")
                .ruleAction("turnOnDehumidifier()")
                .priority(2)
                .enabled(false)
                .build();

        SceneRule updatedRule = SceneRule.builder()
                .id(ruleId)
                .sceneId(1L)
                .ruleName("Updated Rule")
                .ruleType("humidity")
                .ruleCondition("humidity > 60")
                .ruleAction("turnOnDehumidifier()")
                .priority(2)
                .enabled(false)
                .build();

        when(sceneRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(sceneRuleRepository.save(any(SceneRule.class))).thenReturn(updatedRule);
        doNothing().when(ruleEngineService).loadRules();

        SceneRule result = sceneManagementService.updateRule(ruleId, updateRequest);

        assertNotNull(result);
        assertEquals("Updated Rule", result.getRuleName());
        assertEquals("humidity", result.getRuleType());
        assertEquals("humidity > 60", result.getRuleCondition());
        assertEquals("turnOnDehumidifier()", result.getRuleAction());
        assertEquals(2, result.getPriority());
        assertFalse(result.getEnabled());
        verify(sceneRuleRepository, times(1)).findById(ruleId);
        verify(sceneRuleRepository, times(1)).save(any(SceneRule.class));
        verify(ruleEngineService, times(1)).loadRules();
    }

    @Test
    void testUpdateRule_NotFound() {
        Long ruleId = 999L;

        SceneRule updateRequest = SceneRule.builder()
                .ruleName("Updated Rule")
                .build();

        when(sceneRuleRepository.findById(ruleId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sceneManagementService.updateRule(ruleId, updateRequest));

        assertTrue(exception.getMessage().contains("规则不存在"));
        verify(sceneRuleRepository, never()).save(any(SceneRule.class));
        verify(ruleEngineService, never()).loadRules();
    }

    @Test
    void testSearchScenes() {
        String keyword = "Living";

        Scene scene1 = Scene.builder()
                .id(1L)
                .name("Living Room Scene")
                .description("Living room automation")
                .enabled(true)
                .build();

        Scene scene2 = Scene.builder()
                .id(2L)
                .name("Bedroom Scene")
                .description("Bedroom automation")
                .enabled(true)
                .build();

        Scene scene3 = Scene.builder()
                .id(3L)
                .name("Kitchen Scene")
                .description("Living area kitchen")
                .enabled(true)
                .build();

        List<Scene> allScenes = List.of(scene1, scene2, scene3);

        when(sceneRepository.findAll()).thenReturn(allScenes);

        List<Scene> result = sceneManagementService.searchScenes(keyword);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getName().contains(keyword)
                || (s.getDescription() != null && s.getDescription().contains(keyword))));
        verify(sceneRepository, times(1)).findAll();
    }

    @Test
    void testSearchScenes_NoMatch() {
        String keyword = "Nonexistent";

        Scene scene1 = Scene.builder()
                .id(1L)
                .name("Living Room Scene")
                .description("Living room automation")
                .enabled(true)
                .build();

        List<Scene> allScenes = List.of(scene1);

        when(sceneRepository.findAll()).thenReturn(allScenes);

        List<Scene> result = sceneManagementService.searchScenes(keyword);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sceneRepository, times(1)).findAll();
    }
}