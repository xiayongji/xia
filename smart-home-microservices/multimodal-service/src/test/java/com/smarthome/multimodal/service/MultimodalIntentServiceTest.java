package com.smarthome.multimodal.service;

import com.smarthome.multimodal.entity.InputType;
import com.smarthome.multimodal.entity.IntentResult;
import com.smarthome.multimodal.entity.IntentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultimodalIntentServiceTest {

    private MultimodalIntentService service;

    @BeforeEach
    void setUp() {
        service = new MultimodalIntentService();
    }

    @Test
    void testRecognizeIntent_ControlDevice() {
        IntentResult result = service.recognizeIntent("打开客厅的灯", InputType.VOICE);

        assertNotNull(result);
        assertEquals("control_device", result.getIntent());
        assertEquals(IntentType.CONTROL, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.6, "Confidence should be >= 0.6 but was " + result.getConfidence());
        assertEquals("灯", result.getTargetDevice());
        assertEquals(InputType.VOICE, result.getInputType());
        assertEquals("打开客厅的灯", result.getRawInput());
    }

    @Test
    void testRecognizeIntent_ControlDevice_TurnOff() {
        IntentResult result = service.recognizeIntent("关闭卧室空调", InputType.VOICE);

        assertNotNull(result);
        assertEquals("control_device", result.getIntent());
        assertEquals(IntentType.CONTROL, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.6, "Confidence should be >= 0.6 but was " + result.getConfidence());
        assertEquals("空调", result.getTargetDevice());
        assertEquals(InputType.VOICE, result.getInputType());
        assertEquals("关闭卧室空调", result.getRawInput());
    }

    @Test
    void testRecognizeIntent_QueryStatus() {
        IntentResult result = service.recognizeIntent("查看温度", InputType.VOICE);

        assertNotNull(result);
        assertEquals("query_status", result.getIntent());
        assertEquals(IntentType.QUERY, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.6, "Confidence should be >= 0.6 but was " + result.getConfidence());
        assertEquals("温度", result.getTargetDevice());
        assertEquals(InputType.VOICE, result.getInputType());
    }

    @Test
    void testRecognizeIntent_SceneTrigger() {
        IntentResult result = service.recognizeIntent("执行回家场景", InputType.VOICE);

        assertNotNull(result);
        assertEquals("scene_trigger", result.getIntent());
        assertEquals(IntentType.SCENE, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.6, "Confidence should be >= 0.6 but was " + result.getConfidence());
        assertEquals("回家", result.getTargetDevice());
        assertEquals(InputType.VOICE, result.getInputType());
    }

    @Test
    void testRecognizeIntent_Unknown() {
        IntentResult result = service.recognizeIntent("你好", InputType.VOICE);

        assertNotNull(result);
        assertEquals("unknown", result.getIntent());
        assertEquals(IntentType.UNKNOWN, result.getIntentType());
        assertEquals(0.0, result.getConfidence());
        assertEquals(InputType.VOICE, result.getInputType());
        assertEquals("你好", result.getRawInput());
    }

    @Test
    void testRecognizeIntent_TextInput() {
        IntentResult result = service.recognizeIntent("打开灯", InputType.TEXT);

        assertNotNull(result);
        assertEquals("control_device", result.getIntent());
        assertEquals(IntentType.CONTROL, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.6, "Confidence should be >= 0.6 but was " + result.getConfidence());
        assertEquals("灯", result.getTargetDevice());
        assertEquals(InputType.TEXT, result.getInputType());
    }

    @Test
    void testRecognizeIntent_NullInput() {
        IntentResult result = service.recognizeIntent(null, InputType.VOICE);

        assertNotNull(result);
        assertEquals("unknown", result.getIntent());
        assertEquals(IntentType.UNKNOWN, result.getIntentType());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void testRecognizeIntent_EmptyInput() {
        IntentResult result = service.recognizeIntent("", InputType.VOICE);

        assertNotNull(result);
        assertEquals("unknown", result.getIntent());
        assertEquals(IntentType.UNKNOWN, result.getIntentType());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void testBatchRecognize() {
        List<String> inputs = Arrays.asList("打开灯", "查看温度", "执行回家场景", "你好");
        List<IntentResult> results = service.batchRecognize(inputs, InputType.VOICE);

        assertNotNull(results);
        assertEquals(4, results.size());
        assertEquals("control_device", results.get(0).getIntent());
        assertEquals("query_status", results.get(1).getIntent());
        assertEquals("scene_trigger", results.get(2).getIntent());
        assertEquals("unknown", results.get(3).getIntent());
    }

    @Test
    void testAddIntentPattern() {
        service.addIntentPattern("custom_greeting",
                List.of("你好", "您好", "嗨"),
                List.of("早上", "下午", "晚上"),
                IntentType.QUERY);

        IntentResult result = service.recognizeIntent("你好早上", InputType.VOICE);

        assertNotNull(result);
        assertEquals("custom_greeting", result.getIntent());
        assertEquals(IntentType.QUERY, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.6, "Confidence should be >= 0.6 but was " + result.getConfidence());
    }
}