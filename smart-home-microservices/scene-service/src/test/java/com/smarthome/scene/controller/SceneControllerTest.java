package com.smarthome.scene.controller;

import com.smarthome.scene.client.DeviceControlClient;
import com.smarthome.scene.entity.Scene;
import com.smarthome.scene.entity.SceneRule;
import com.smarthome.scene.service.SceneDeviceActionService;
import com.smarthome.scene.service.SceneExecutionService;
import com.smarthome.scene.service.SceneManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SceneController.class)
@AutoConfigureMockMvc(addFilters = false)
class SceneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SceneManagementService sceneManagementService;

    @MockBean
    private SceneExecutionService sceneExecutionService;

    @MockBean
    private SceneDeviceActionService sceneDeviceActionService;

    @MockBean
    private DeviceControlClient deviceControlClient;

    @Test
    void testCreateScene() throws Exception {
        Scene createdScene = Scene.builder()
                .id(1L)
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .triggerType("manual")
                .enabled(true)
                .executionCount(0)
                .build();

        when(sceneManagementService.createScene(any(Scene.class))).thenReturn(createdScene);

        String requestJson = """
                {
                    "name": "Test Scene",
                    "description": "Test Description",
                    "triggerType": "manual"
                }
                """;

        mockMvc.perform(post("/api/scene/scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Scene"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.executionCount").value(0));

        verify(sceneManagementService, times(1)).createScene(any(Scene.class));
    }

    @Test
    void testUpdateScene() throws Exception {
        Long sceneId = 1L;

        Scene updatedScene = Scene.builder()
                .id(sceneId)
                .name("Updated Scene")
                .description("Updated Description")
                .userId(1L)
                .triggerType("auto")
                .enabled(false)
                .executionCount(3)
                .build();

        when(sceneManagementService.updateScene(eq(sceneId), any(Scene.class))).thenReturn(updatedScene);

        String requestJson = """
                {
                    "name": "Updated Scene",
                    "description": "Updated Description",
                    "triggerType": "auto",
                    "enabled": false
                }
                """;

        mockMvc.perform(put("/api/scene/scenes/{id}", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Scene"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.enabled").value(false));

        verify(sceneManagementService, times(1)).updateScene(eq(sceneId), any(Scene.class));
    }

    @Test
    void testDeleteScene() throws Exception {
        Long sceneId = 1L;

        doNothing().when(sceneManagementService).deleteScene(sceneId);

        mockMvc.perform(delete("/api/scene/scenes/{id}", sceneId))
                .andExpect(status().isOk());

        verify(sceneManagementService, times(1)).deleteScene(sceneId);
    }

    @Test
    void testGetScene() throws Exception {
        Long sceneId = 1L;

        Scene scene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .description("Test Description")
                .userId(1L)
                .enabled(true)
                .executionCount(10)
                .build();

        when(sceneManagementService.getScene(sceneId)).thenReturn(scene);

        mockMvc.perform(get("/api/scene/scenes/{id}", sceneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Scene"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.executionCount").value(10));

        verify(sceneManagementService, times(1)).getScene(sceneId);
    }

    @Test
    void testGetAllScenes() throws Exception {
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

        when(sceneManagementService.getAllScenes()).thenReturn(scenes);

        mockMvc.perform(get("/api/scene/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Scene 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Scene 2"));

        verify(sceneManagementService, times(1)).getAllScenes();
    }

    @Test
    void testGetScenesByUser() throws Exception {
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

        when(sceneManagementService.getScenesByUser(userId)).thenReturn(scenes);

        mockMvc.perform(get("/api/scene/scenes/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[1].userId").value(1));

        verify(sceneManagementService, times(1)).getScenesByUser(userId);
    }

    @Test
    void testToggleScene() throws Exception {
        Long sceneId = 1L;

        Scene toggledScene = Scene.builder()
                .id(sceneId)
                .name("Test Scene")
                .enabled(true)
                .executionCount(5)
                .build();

        when(sceneManagementService.toggleScene(sceneId, true)).thenReturn(toggledScene);

        mockMvc.perform(put("/api/scene/scenes/{id}/toggle", sceneId)
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(sceneManagementService, times(1)).toggleScene(sceneId, true);
    }

    @Test
    void testAddRule() throws Exception {
        Long sceneId = 1L;

        SceneRule createdRule = SceneRule.builder()
                .id(1L)
                .sceneId(sceneId)
                .ruleName("Test Rule")
                .ruleType("temperature")
                .ruleCondition("temperature > 30")
                .ruleAction("turnOnAC()")
                .priority(1)
                .enabled(true)
                .build();

        when(sceneManagementService.addRuleToScene(eq(sceneId), any(SceneRule.class))).thenReturn(createdRule);

        String requestJson = """
                {
                    "ruleName": "Test Rule",
                    "ruleType": "temperature",
                    "ruleCondition": "temperature > 30",
                    "ruleAction": "turnOnAC()",
                    "priority": 1,
                    "enabled": true
                }
                """;

        mockMvc.perform(post("/api/scene/scenes/{id}/rules", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ruleName").value("Test Rule"))
                .andExpect(jsonPath("$.ruleType").value("temperature"))
                .andExpect(jsonPath("$.sceneId").value(1));

        verify(sceneManagementService, times(1)).addRuleToScene(eq(sceneId), any(SceneRule.class));
    }

    @Test
    void testGetSceneRules() throws Exception {
        Long sceneId = 1L;

        SceneRule rule1 = SceneRule.builder()
                .id(1L)
                .sceneId(sceneId)
                .ruleName("Rule 1")
                .priority(2)
                .enabled(true)
                .build();

        SceneRule rule2 = SceneRule.builder()
                .id(2L)
                .sceneId(sceneId)
                .ruleName("Rule 2")
                .priority(1)
                .enabled(true)
                .build();

        List<SceneRule> rules = List.of(rule1, rule2);

        when(sceneManagementService.getRulesForScene(sceneId)).thenReturn(rules);

        mockMvc.perform(get("/api/scene/scenes/{id}/rules", sceneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ruleName").value("Rule 1"))
                .andExpect(jsonPath("$[1].ruleName").value("Rule 2"));

        verify(sceneManagementService, times(1)).getRulesForScene(sceneId);
    }

    @Test
    void testDeleteRule() throws Exception {
        Long ruleId = 1L;

        doNothing().when(sceneManagementService).removeRule(ruleId);

        mockMvc.perform(delete("/api/scene/rules/{ruleId}", ruleId))
                .andExpect(status().isOk());

        verify(sceneManagementService, times(1)).removeRule(ruleId);
    }

    @Test
    void testUpdateRule() throws Exception {
        Long ruleId = 1L;

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

        when(sceneManagementService.updateRule(eq(ruleId), any(SceneRule.class))).thenReturn(updatedRule);

        String requestJson = """
                {
                    "ruleName": "Updated Rule",
                    "ruleType": "humidity",
                    "ruleCondition": "humidity > 60",
                    "ruleAction": "turnOnDehumidifier()",
                    "priority": 2,
                    "enabled": false
                }
                """;

        mockMvc.perform(put("/api/scene/rules/{ruleId}", ruleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ruleName").value("Updated Rule"))
                .andExpect(jsonPath("$.ruleType").value("humidity"))
                .andExpect(jsonPath("$.enabled").value(false));

        verify(sceneManagementService, times(1)).updateRule(eq(ruleId), any(SceneRule.class));
    }

    @Test
    void testSearchScenes() throws Exception {
        String keyword = "Living";

        Scene scene1 = Scene.builder()
                .id(1L)
                .name("Living Room Scene")
                .description("Living room automation")
                .enabled(true)
                .build();

        Scene scene2 = Scene.builder()
                .id(2L)
                .name("Living Kitchen Scene")
                .description("Kitchen automation")
                .enabled(true)
                .build();

        List<Scene> scenes = List.of(scene1, scene2);

        when(sceneManagementService.searchScenes(keyword)).thenReturn(scenes);

        mockMvc.perform(get("/api/scene/scenes/search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Living Room Scene"))
                .andExpect(jsonPath("$[1].name").value("Living Kitchen Scene"));

        verify(sceneManagementService, times(1)).searchScenes(keyword);
    }
}