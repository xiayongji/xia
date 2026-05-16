package com.smarthome.analytics.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CloudLLMIntegrationService {

    private static final Map<String, String> SCENE_PROMPT_TEMPLATES = new HashMap<>();

    static {
        SCENE_PROMPT_TEMPLATES.put("morning_routine",
                "用户早上起床，请推荐适合的智能家居场景，包括灯光、窗帘、空调等设备的设置");
        SCENE_PROMPT_TEMPLATES.put("leave_home",
                "用户离家外出，请推荐适合的智能家居场景，包括设备关闭和安防设置");
        SCENE_PROMPT_TEMPLATES.put("evening_relax",
                "用户晚间休息，请推荐适合的智能家居场景，包括灯光调暗、空调设置等");
        SCENE_PROMPT_TEMPLATES.put("sleep_mode",
                "用户准备睡觉，请推荐适合的智能家居场景，包括灯光关闭和夜灯设置");
        SCENE_PROMPT_TEMPLATES.put("energy_saver",
                "用户想要节能，请推荐适合的智能家居场景，包括功率调节和用电优化");
    }

    public String generateSceneRecommendationPrompt(Long userId, String timeSlot, String userSegment) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为智能家居助手，请根据以下信息推荐适合的场景：\n\n");
        prompt.append("用户ID: ").append(userId).append("\n");
        prompt.append("当前时段: ").append(getTimeSlotDescription(timeSlot)).append("\n");
        prompt.append("用户类型: ").append(getSegmentDescription(userSegment)).append("\n");
        prompt.append("\n请根据这些信息推荐一个最合适的智能家居场景，并说明推荐理由。");
        return prompt.toString();
    }

    public String generateNaturalLanguageCommand(String userCommand, Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户说: \"").append(userCommand).append("\"\n\n");
        prompt.append("请将这个自然语言命令转换为智能家居控制指令。\n");
        prompt.append("用户ID: ").append(userId).append("\n");
        prompt.append("\n请提供JSON格式的指令，包括：\n");
        prompt.append("- action: 操作类型（如 turn_on, turn_off, set_temperature 等）\n");
        prompt.append("- target_devices: 目标设备列表\n");
        prompt.append("- parameters: 操作参数\n");
        prompt.append("- suggested_scene: 如果适合，推荐的场景名称\n");
        return prompt.toString();
    }

    public String analyzeUserPreference(String userFeedback, Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户反馈: \"").append(userFeedback).append("\"\n");
        prompt.append("用户ID: ").append(userId).append("\n");
        prompt.append("\n请分析用户的偏好和意图，并更新用户画像。");
        return prompt.toString();
    }

    public String generateSceneDescription(String sceneName, List<String> devices) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下智能家居场景生成一段吸引人的描述：\n\n");
        prompt.append("场景名称: ").append(sceneName).append("\n");
        prompt.append("包含设备: ").append(String.join(", ", devices)).append("\n");
        prompt.append("\n请生成一段简洁（不超过50字）的场景描述，突出场景的主要功能和用户体验。");
        return prompt.toString();
    }

    public Map<String, Object> parseLLMResponse(String response) {
        Map<String, Object> result = new HashMap<>();

        result.put("original_response", response);
        result.put("timestamp", System.currentTimeMillis());

        if (response.contains("推荐")) {
            result.put("intent", "RECOMMEND");
        } else if (response.contains("控制") || response.contains("打开") || response.contains("关闭")) {
            result.put("intent", "CONTROL");
        } else if (response.contains("查询") || response.contains("状态")) {
            result.put("intent", "QUERY");
        } else {
            result.put("intent", "UNKNOWN");
        }

        return result;
    }

    public String callCloudLLM(String prompt, String apiType) {
        switch (apiType.toUpperCase()) {
            case "BAIDU_WENXIN":
                return callBaiduWenxin(prompt);
            case "ALIBABA_DASHscope":
                return callAlibabaDashscope(prompt);
            case "OPENAI":
                return callOpenAI(prompt);
            default:
                return generateLocalResponse(prompt);
        }
    }

    private String callBaiduWenxin(String prompt) {
        return "【百度文心大模型API调用占位】\n" +
                "实际使用时需要：\n" +
                "1. 申请百度文心API Key\n" +
                "2. 配置API Endpoint\n" +
                "3. 实现HTTP调用逻辑\n\n" +
                "Prompt: " + prompt;
    }

    private String callAlibabaDashscope(String prompt) {
        return "【阿里通义大模型API调用占位】\n" +
                "实际使用时需要：\n" +
                "1. 申请阿里云Dashscope API Key\n" +
                "2. 配置API Endpoint\n" +
                "3. 实现HTTP调用逻辑\n\n" +
                "Prompt: " + prompt;
    }

    private String callOpenAI(String prompt) {
        return "【OpenAI GPT API调用占位】\n" +
                "实际使用时需要：\n" +
                "1. 配置OpenAI API Key\n" +
                "2. 设置代理（如需要）\n" +
                "3. 实现API调用逻辑\n\n" +
                "Prompt: " + prompt;
    }

    private String generateLocalResponse(String prompt) {
        if (prompt.contains("推荐场景")) {
            return "根据您的情况，推荐您使用'回家欢迎模式'，它会自动开启玄关灯、空调，并播放欢迎语音。";
        } else if (prompt.contains("早上") || prompt.contains("起床")) {
            return "建议使用'晨起模式'，它会自动拉开窗帘、开启咖啡机、播放轻音乐。";
        } else if (prompt.contains("离家")) {
            return "建议使用'离家模式'，它会关闭所有灯光、空调切换节能模式、启动安防设备。";
        } else if (prompt.contains("晚上") || prompt.contains("休息")) {
            return "建议使用'晚间休息模式'，客厅灯光会调暗、空调调至舒适温度、播放舒缓音乐。";
        } else {
            return "抱歉，我不太理解您的意思。您可以试试说'打开客厅灯'或'开启回家模式'。";
        }
    }

    private String getTimeSlotDescription(String timeSlot) {
        return switch (timeSlot) {
            case "MORNING" -> "早晨（5:00-9:00）";
            case "DAY" -> "白天（9:00-18:00）";
            case "EVENING" -> "晚间（18:00-22:00）";
            case "NIGHT" -> "夜间（22:00-5:00）";
            default -> "未知时段";
        };
    }

    private String getSegmentDescription(String segment) {
        return switch (segment) {
            case "EARLY_BIRD" -> "早起型用户";
            case "NIGHT_OWL" -> "夜猫子型用户";
            case "FAMILY_ORIENTED" -> "家庭型用户";
            case "ENERGY_CONSCIOUS" -> "节能型用户";
            case "TECH_ENTHUSIAST" -> "科技爱好者用户";
            default -> "普通用户";
        };
    }
}