package com.smarthome.device.service;

import com.smarthome.device.entity.DeviceShadow;
import com.smarthome.device.repository.DeviceShadowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceShadowServiceTest {

    @Mock
    private DeviceShadowRepository shadowRepository;

    private ObjectMapper objectMapper;

    private DeviceShadowService shadowService;

    private DeviceShadow shadow;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        shadowService = new DeviceShadowService(shadowRepository, objectMapper);

        shadow = new DeviceShadow();
        shadow.setId(1L);
        shadow.setDeviceId("device-001");
        shadow.setDesiredState("{\"power\":\"on\",\"brightness\":80}");
        shadow.setReportedState("{\"power\":\"on\",\"brightness\":80}");
        shadow.setLastUpdated(LocalDateTime.now());
        shadow.setVersion(1);
    }

    @Test
    void testGetShadow() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));

        Optional<DeviceShadow> result = shadowService.getShadow("device-001");

        assertTrue(result.isPresent());
        assertEquals("device-001", result.get().getDeviceId());
        verify(shadowRepository).findByDeviceId("device-001");
    }

    @Test
    void testGetShadow_NotFound() {
        when(shadowRepository.findByDeviceId("device-nonexistent")).thenReturn(Optional.empty());

        Optional<DeviceShadow> result = shadowService.getShadow("device-nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void testSaveShadow_Create() {
        when(shadowRepository.findByDeviceId("device-new")).thenReturn(Optional.empty());
        when(shadowRepository.save(any(DeviceShadow.class))).thenAnswer(invocation -> {
            DeviceShadow s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        DeviceShadow result = shadowService.saveShadow("device-new",
                "{\"power\":\"on\"}", "{\"power\":\"off\"}");

        assertNotNull(result);
        assertEquals("device-new", result.getDeviceId());
        assertEquals("{\"power\":\"on\"}", result.getDesiredState());
        assertEquals("{\"power\":\"off\"}", result.getReportedState());
        verify(shadowRepository).save(any(DeviceShadow.class));
    }

    @Test
    void testSaveShadow_Update() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));
        when(shadowRepository.save(any(DeviceShadow.class))).thenReturn(shadow);

        DeviceShadow result = shadowService.saveShadow("device-001",
                "{\"power\":\"off\"}", "{\"power\":\"off\"}");

        assertNotNull(result);
        assertEquals("{\"power\":\"off\"}", result.getDesiredState());
        assertEquals("{\"power\":\"off\"}", result.getReportedState());
        verify(shadowRepository).save(shadow);
    }

    @Test
    void testUpdateDesiredState() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));
        when(shadowRepository.save(any(DeviceShadow.class))).thenReturn(shadow);

        Map<String, Object> desiredState = Map.of("power", "on", "brightness", 100);
        shadowService.updateDesiredState("device-001", desiredState);

        verify(shadowRepository).save(shadow);
        assertNotNull(shadow.getDesiredState());
        assertTrue(shadow.getDesiredState().contains("brightness"));
    }

    @Test
    void testUpdateReportedState() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));
        when(shadowRepository.save(any(DeviceShadow.class))).thenReturn(shadow);

        Map<String, Object> reportedState = Map.of("power", "on", "temperature", 25.5);
        shadowService.updateReportedState("device-001", reportedState);

        verify(shadowRepository).save(shadow);
        assertNotNull(shadow.getReportedState());
        assertTrue(shadow.getReportedState().contains("temperature"));
    }

    @Test
    void testGetDesiredState() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));

        Map<String, Object> result = shadowService.getDesiredState("device-001");

        assertNotNull(result);
        assertEquals("on", result.get("power"));
        assertEquals(80, result.get("brightness"));
    }

    @Test
    void testGetReportedState() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));

        Map<String, Object> result = shadowService.getReportedState("device-001");

        assertNotNull(result);
        assertEquals("on", result.get("power"));
        assertEquals(80, result.get("brightness"));
    }

    @Test
    void testDeleteShadow() {
        shadowService.deleteShadow("device-001");

        verify(shadowRepository).deleteByDeviceId("device-001");
    }

    @Test
    void testIsStateSynchronized_True() {
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));

        boolean result = shadowService.isStateSynchronized("device-001");

        assertTrue(result);
    }

    @Test
    void testIsStateSynchronized_False() {
        shadow.setReportedState("{\"power\":\"off\",\"brightness\":50}");
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));

        boolean result = shadowService.isStateSynchronized("device-001");

        assertFalse(result);
    }

    @Test
    void testIsStateSynchronized_NoShadow() {
        when(shadowRepository.findByDeviceId("device-nonexistent")).thenReturn(Optional.empty());

        boolean result = shadowService.isStateSynchronized("device-nonexistent");

        assertFalse(result);
    }

    @Test
    void testGetStateDiff() {
        shadow.setDesiredState("{\"power\":\"on\",\"brightness\":100,\"color\":\"red\"}");
        shadow.setReportedState("{\"power\":\"on\",\"brightness\":80}");
        when(shadowRepository.findByDeviceId("device-001")).thenReturn(Optional.of(shadow));

        Map<String, Object> diff = shadowService.getStateDiff("device-001");

        assertNotNull(diff);
        assertTrue(diff.containsKey("brightness"));
        assertEquals(100, diff.get("brightness"));
        assertTrue(diff.containsKey("color"));
        assertEquals("red", diff.get("color"));
        assertFalse(diff.containsKey("power"));
    }

    @Test
    void testUpdateDesiredState_EmptyShadow() {
        when(shadowRepository.findByDeviceId("device-new")).thenReturn(Optional.empty());
        when(shadowRepository.save(any(DeviceShadow.class))).thenAnswer(invocation -> {
            DeviceShadow s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        Map<String, Object> desiredState = Map.of("power", "on", "mode", "auto");
        shadowService.updateDesiredState("device-new", desiredState);

        verify(shadowRepository).save(any(DeviceShadow.class));
    }
}