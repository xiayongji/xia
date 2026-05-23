package com.smarthome.multimodal.service;

import com.smarthome.multimodal.entity.BehaviorEvent;
import com.smarthome.multimodal.entity.ContextSnapshot;
import com.smarthome.multimodal.entity.PredictedBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorPredictionServiceTest {

    private BehaviorPredictionService service;

    @BeforeEach
    void setUp() {
        service = new BehaviorPredictionService();
    }

    @Test
    void testPredictBehavior_NoPatterns() {
        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(LocalDateTime.now())
                .location("客厅")
                .lastAction("turn_on_light")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNull(result);
    }

    @Test
    void testPredictBehavior_WithMatchingPattern() {
        LocalDateTime now = LocalDateTime.now();

        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(now)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(now.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNotNull(result);
        assertEquals("watch_tv", result.getBehaviorType());
        assertEquals("turn_on_tv", result.getPredictedAction());
        assertTrue(result.getConfidence() >= 0.85,
                "Confidence should be >= 0.85 but was " + result.getConfidence());
        assertNotNull(result.getExpectedTime());
    }

    @Test
    void testPredictBehavior_BelowThreshold() {
        LocalDateTime now = LocalDateTime.now();

        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(now)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(now.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNull(result);
    }

    @Test
    void testLearnBehavior_NewRule() {
        LocalDateTime now = LocalDateTime.now();

        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(now)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(now.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNotNull(result);
        assertEquals("watch_tv", result.getBehaviorType());
        assertEquals("turn_on_tv", result.getPredictedAction());
    }

    @Test
    void testLearnBehavior_ExistingRule() {
        LocalDateTime now = LocalDateTime.now();

        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(now)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);

        ContextSnapshot context1 = ContextSnapshot.builder()
                .timestamp(now.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result1 = service.predictBehavior("user1", context1);
        assertNull(result1, "Single occurrence should be below threshold");

        service.learnBehavior("user1", event);

        ContextSnapshot context2 = ContextSnapshot.builder()
                .timestamp(now.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result2 = service.predictBehavior("user1", context2);
        assertNotNull(result2, "After second occurrence, confidence should meet threshold");
        assertTrue(result2.getConfidence() >= 0.85,
                "Confidence after 2 occurrences should be >= 0.85 but was " + result2.getConfidence());
    }

    @Test
    void testGetPredictionAccuracy() {
        double accuracy = service.getPredictionAccuracy();

        assertEquals(0.85, accuracy);
    }
}