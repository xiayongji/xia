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
        IntentResult result = service.recognizeIntent("打开关闭开启关掉灯空调电视窗帘", InputType.VOICE);

        assertNotNull(result);
        assertEquals("control_device", result.getIntent());
        assertEquals(IntentType.CONTROL, result.getIntentType());
        double expectedScore = 8.0 / 12.0;
        assertEquals(expectedScore, result.getConfidence(), 0.001);
        assertNotNull(result.getTargetDevice());
        assertEquals(InputType.VOICE, result.getInputType());
    }

    @Test
    void testRecognizeIntent_ScoreBelowThreshold() {
        IntentResult result = service.recognizeIntent("打开客厅的灯", InputType.VOICE);

        assertNotNull(result);
        assertEquals("unknown", result.getIntent());
        assertEquals(IntentType.UNKNOWN, result.getIntentType());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void testRecognizeIntent_QueryStatus() {
        IntentResult result = service.recognizeIntent("查看看看状态多少温度湿度灯温度湿度", InputType.VOICE);

        assertNotNull(result);
        assertEquals("query_status", result.getIntent());
        assertEquals(IntentType.QUERY, result.getIntentType());
        double expectedScore = 7.0 / 11.0;
        assertTrue(result.getConfidence() >= 0.6,
                "Confidence should be >= 0.6 but was " + result.getConfidence());
    }

    @Test
    void testRecognizeIntent_SceneTrigger() {
        IntentResult result = service.recognizeIntent("启动进入场景回家离家睡眠", InputType.VOICE);

        assertNotNull(result);
        assertEquals("scene_trigger", result.getIntent());
        assertEquals(IntentType.SCENE, result.getIntentType());
        double expectedScore = 6.0 / 10.0;
        assertEquals(expectedScore, result.getConfidence(), 0.001);
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
        IntentResult result = service.recognizeIntent("打开关闭开启关掉灯空调电视窗帘", InputType.TEXT);

        assertNotNull(result);
        assertEquals("control_device", result.getIntent());
        assertEquals(IntentType.CONTROL, result.getIntentType());
        double expectedScore = 8.0 / 12.0;
        assertEquals(expectedScore, result.getConfidence(), 0.001);
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
        List<String> inputs = Arrays.asList(
                "打开关闭开启关掉灯空调电视窗帘",
                "查看看看状态多少温度湿度灯温度湿度",
                "你好");
        List<IntentResult> results = service.batchRecognize(inputs, InputType.VOICE);

        assertNotNull(results);
        assertEquals(3, results.size());
        assertEquals("control_device", results.get(0).getIntent());
        assertEquals("query_status", results.get(1).getIntent());
        assertEquals("unknown", results.get(2).getIntent());
    }

    @Test
    void testAddIntentPattern() {
        service.addIntentPattern("custom_greeting",
                List.of("你好", "您好"),
                List.of("早上", "下午"),
                IntentType.QUERY);

        IntentResult result = service.recognizeIntent("你好您好早上下午", InputType.VOICE);

        assertNotNull(result);
        assertEquals("custom_greeting", result.getIntent());
        assertEquals(IntentType.QUERY, result.getIntentType());
        assertEquals(4.0 / 4.0, result.getConfidence(), 0.001);
    }

    @Test
    void testRecognizeIntent_ControlDeviceHighScore() {
        IntentResult result = service.recognizeIntent("打开关闭开启关掉启动停止灯空调电视窗帘风扇加湿器",
                InputType.VOICE);

        assertNotNull(result);
        assertEquals("control_device", result.getIntent());
        assertEquals(IntentType.CONTROL, result.getIntentType());
        assertTrue(result.getConfidence() >= 0.9,
                "High keyword match should have confidence >= 0.9 but was " + result.getConfidence());
        assertNotNull(result.getTargetDevice());
    }

    @Test
    void testRecognizeIntent_QueryStatus_ModerateScore() {
        IntentResult result = service.recognizeIntent("查看看看状态多少温度温度湿度设备",
                InputType.VOICE);

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.5);
    }
}