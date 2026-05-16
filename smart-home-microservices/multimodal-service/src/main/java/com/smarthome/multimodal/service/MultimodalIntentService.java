package com.smarthome.multimodal.service;

import com.smarthome.multimodal.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MultimodalIntentService {

    private final Map<String, IntentPattern> intentPatterns = new ConcurrentHashMap<>();

    public MultimodalIntentService() {
        initializeIntentPatterns();
    }

    private void initializeIntentPatterns() {
        intentPatterns.put("control_device", new IntentPattern(
                List.of("打开", "关闭", "开启", "关掉", "启动", "停止"),
                List.of("灯", "空调", "电视", "窗帘", "风扇", "加湿器"),
                IntentType.CONTROL
        ));
        
        intentPatterns.put("query_status", new IntentPattern(
                List.of("查", "看看", "状态", "多少", "温度", "湿度"),
                List.of("灯", "空调", "温度", "湿度", "设备"),
                IntentType.QUERY
        ));
        
        intentPatterns.put("scene_trigger", new IntentPattern(
                List.of("执行", "启动", "进入", "离开"),
                List.of("场景", "模式", "回家", "离家", "睡眠", "阅读"),
                IntentType.SCENE
        ));
    }

    public IntentResult recognizeIntent(String input, InputType inputType) {
        String normalizedInput = normalizeInput(input);
        
        for (Map.Entry<String, IntentPattern> entry : intentPatterns.entrySet()) {
            IntentPattern pattern = entry.getValue();
            double score = calculateMatchScore(normalizedInput, pattern);
            
            if (score >= 0.6) {
                String targetDevice = extractTargetDevice(normalizedInput, pattern);
                
                return IntentResult.builder()
                        .intent(entry.getKey())
                        .intentType(pattern.getIntentType())
                        .confidence(score)
                        .targetDevice(targetDevice)
                        .inputType(inputType)
                        .rawInput(input)
                        .build();
            }
        }
        
        return IntentResult.builder()
                .intent("unknown")
                .intentType(IntentType.UNKNOWN)
                .confidence(0.0)
                .inputType(inputType)
                .rawInput(input)
                .build();
    }

    private String normalizeInput(String input) {
        if (input == null) return "";
        return input.toLowerCase().trim();
    }

    private double calculateMatchScore(String input, IntentPattern pattern) {
        int matchCount = 0;
        int totalKeywords = pattern.getVerbs().size() + pattern.getNouns().size();
        
        for (String verb : pattern.getVerbs()) {
            if (input.contains(verb)) {
                matchCount++;
            }
        }
        
        for (String noun : pattern.getNouns()) {
            if (input.contains(noun)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / totalKeywords;
    }

    private String extractTargetDevice(String input, IntentPattern pattern) {
        for (String noun : pattern.getNouns()) {
            if (input.contains(noun)) {
                return noun;
            }
        }
        return null;
    }

    public List<IntentResult> batchRecognize(List<String> inputs, InputType inputType) {
        return inputs.stream()
                .map(input -> recognizeIntent(input, inputType))
                .toList();
    }

    public void addIntentPattern(String intentName, List<String> verbs, List<String> nouns, IntentType type) {
        intentPatterns.put(intentName, new IntentPattern(verbs, nouns, type));
    }
}