package com.smarthome.multimodal.service;

import com.smarthome.multimodal.entity.BehaviorEvent;
import com.smarthome.multimodal.entity.ContextSnapshot;
import com.smarthome.multimodal.entity.PredictedBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorPredictionServiceTest {

    private static final LocalDateTime NOON = LocalDateTime.of(2026, 5, 23, 12, 0, 0);

    private BehaviorPredictionService service;

    @BeforeEach
    void setUp() {
        service = new BehaviorPredictionService();
    }

    @Test
    void testPredictBehavior_NoPatterns() {
        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(NOON)
                .location("客厅")
                .lastAction("turn_on_light")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNull(result);
    }

    @Test
    void testPredictBehavior_WithMatchingPattern() {
        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
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
        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNull(result);
    }

    @Test
    void testLearnBehavior_NewRule() {
        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNotNull(result);
        assertEquals("watch_tv", result.getBehaviorType());
        assertEquals("turn_on_tv", result.getPredictedAction());
        assertTrue(result.getConfidence() >= 0.85,
                "Confidence should be >= 0.85 but was " + result.getConfidence());
    }

    @Test
    void testLearnBehavior_ExistingRule() {
        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);

        ContextSnapshot context1 = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result1 = service.predictBehavior("user1", context1);
        assertNull(result1,
                "Single occurrence should be below threshold, but confidence was " +
                (result1 != null ? result1.getConfidence() : "null"));

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context2 = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        PredictedBehavior result2 = service.predictBehavior("user1", context2);
        assertNotNull(result2,
                "After 3 occurrences, confidence should meet threshold but was null");
        assertTrue(result2.getConfidence() >= 0.85,
                "Confidence after 3 occurrences should be >= 0.85 but was " + result2.getConfidence());
    }

    @Test
    void testPredictBehavior_PartialMatch_BelowThreshold() {
        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(NOON.plusHours(5))
                .location("卧室")
                .lastAction("something_else")
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNull(result,
                "Different time/location/action should not match well enough");
    }

    @Test
    void testGetPredictionAccuracy() {
        double accuracy = service.getPredictionAccuracy();

        assertEquals(0.85, accuracy);
    }

    @Test
    void testPredictBehavior_MultipleUsers() {
        BehaviorEvent event1 = BehaviorEvent.builder()
                .behaviorType("watch_tv")
                .action("turn_on_tv")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        BehaviorEvent event2 = BehaviorEvent.builder()
                .behaviorType("read_book")
                .action("turn_on_reading_light")
                .timestamp(NOON)
                .location("书房")
                .previousAction("enter_study")
                .build();

        service.learnBehavior("user1", event1);
        service.learnBehavior("user1", event1);
        service.learnBehavior("user1", event1);
        service.learnBehavior("user2", event2);
        service.learnBehavior("user2", event2);
        service.learnBehavior("user2", event2);

        ContextSnapshot context1 = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .build();

        ContextSnapshot context2 = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("书房")
                .lastAction("enter_study")
                .build();

        PredictedBehavior result1 = service.predictBehavior("user1", context1);
        PredictedBehavior result2 = service.predictBehavior("user2", context2);

        assertNotNull(result1, "user1 prediction should not be null");
        assertEquals("watch_tv", result1.getBehaviorType());
        assertNotNull(result2, "user2 prediction should not be null");
        assertEquals("read_book", result2.getBehaviorType());
        assertNotEquals(result1.getBehaviorType(), result2.getBehaviorType());
    }

    @Test
    void testLearnBehavior_EnvironmentalConditions() {
        BehaviorEvent event = BehaviorEvent.builder()
                .behaviorType("turn_on_ac")
                .action("set_ac_cool")
                .timestamp(NOON)
                .location("客厅")
                .previousAction("enter_room")
                .build();

        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);
        service.learnBehavior("user1", event);

        ContextSnapshot context = ContextSnapshot.builder()
                .timestamp(NOON.plusMinutes(10))
                .location("客厅")
                .lastAction("enter_room")
                .temperature(32.0)
                .lightLevel(50)
                .motionDetected(true)
                .build();

        PredictedBehavior result = service.predictBehavior("user1", context);

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.85,
                "With all context matched, confidence should be >= 0.85 but was " + result.getConfidence());
    }
}