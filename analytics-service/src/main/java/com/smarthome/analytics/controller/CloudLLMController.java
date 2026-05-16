package com.smarthome.analytics.controller;

import com.smarthome.analytics.service.CloudLLMIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics/llm")
@CrossOrigin(origins = "*")
public class CloudLLMController {

    private final CloudLLMIntegrationService llmService;

    public CloudLLMController(CloudLLMIntegrationService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String apiType = request.getOrDefault("apiType", "LOCAL");

        String response = llmService.callCloudLLM(prompt, apiType);
        Map<String, Object> parsedResponse = llmService.parseLLMResponse(response);

        return ResponseEntity.ok(Map.of(
                "response", response,
                "parsed", parsedResponse,
                "apiType", apiType
        ));
    }

    @PostMapping("/recommend")
    public ResponseEntity<String> generateSceneRecommendation(
            @RequestParam Long userId,
            @RequestParam String timeSlot,
            @RequestParam String userSegment) {

        String prompt = llmService.generateSceneRecommendationPrompt(userId, timeSlot, userSegment);
        String response = llmService.callCloudLLM(prompt, "LOCAL");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/command")
    public ResponseEntity<Map<String, Object>> parseNaturalLanguageCommand(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {

        String userCommand = request.get("command");
        String prompt = llmService.generateNaturalLanguageCommand(userCommand, userId);
        String response = llmService.callCloudLLM(prompt, "LOCAL");

        return ResponseEntity.ok(Map.of(
                "original_command", userCommand,
                "parsed_response", response,
                "processed", llmService.parseLLMResponse(response)
        ));
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> analyzeFeedback(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {

        String feedback = request.get("feedback");
        String prompt = llmService.analyzeUserPreference(feedback, userId);
        String response = llmService.callCloudLLM(prompt, "LOCAL");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/templates")
    public ResponseEntity<Map<String, String>> getPromptTemplates() {
        return ResponseEntity.ok(Map.of(
                "scene_recommendation", "生成场景推荐Prompt模板",
                "natural_command", "解析自然语言命令Prompt模板",
                "user_feedback", "分析用户反馈Prompt模板",
                "note", "实际使用时请配置云端大模型API（百度文心/阿里通义/OpenAI）"
        ));
    }
}