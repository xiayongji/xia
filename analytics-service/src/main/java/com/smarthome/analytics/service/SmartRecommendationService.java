package com.smarthome.analytics.service;

import com.smarthome.analytics.model.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SmartRecommendationService {

    private final Map<Long, Map<String, Double>> userSceneRatings = new ConcurrentHashMap<>();
    private final Map<String, Scene> sceneDatabase = new ConcurrentHashMap<>();
    private final Map<Long, List<UserSceneInteraction>> userInteractions = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> sceneInterestedUsers = new ConcurrentHashMap<>();

    private static final double COLLABORATIVE_WEIGHT = 0.4;
    private static final double CONTENT_WEIGHT = 0.3;
    private static final double CONTEXT_WEIGHT = 0.2;
    private static final double POPULARITY_WEIGHT = 0.1;

    @PostConstruct
    public void init() {
        initializeSampleScenes();
        initializeSampleInteractions();
    }

    private void initializeSampleScenes() {
        addScene(Scene.builder()
                .sceneId("morning_routine")
                .name("晨起模式")
                .description("自动开启晨起流程：拉开窗帘、开启咖啡机、播放轻音乐")
                .category(Scene.SceneCategory.MORNING_ROUTINE)
                .timeSlot("MORNING")
                .userSegment("EARLY_BIRD,FAMILY_ORIENTED")
                .usageCount(156)
                .avgRating(4.8)
                .createdAt(LocalDateTime.now().minusDays(30))
                .lastUsedAt(LocalDateTime.now().minusHours(2))
                .build());

        addScene(Scene.builder()
                .sceneId("leave_home")
                .name("离家模式")
                .description("一键离家：关闭所有灯光、空调切换节能模式、启动安防")
                .category(Scene.SceneCategory.LEAVE_HOME)
                .timeSlot("DAY")
                .userSegment("BALANCED")
                .usageCount(289)
                .avgRating(4.9)
                .createdAt(LocalDateTime.now().minusDays(60))
                .lastUsedAt(LocalDateTime.now().minusMinutes(30))
                .build());

        addScene(Scene.builder()
                .sceneId("evening_relax")
                .name("晚间休息模式")
                .description("温馨晚间：客厅灯光调暗、空调调至舒适温度、播放舒缓音乐")
                .category(Scene.SceneCategory.EVENING_RELAX)
                .timeSlot("EVENING")
                .userSegment("NIGHT_OWL,FAMILY_ORIENTED")
                .usageCount(134)
                .avgRating(4.6)
                .createdAt(LocalDateTime.now().minusDays(20))
                .lastUsedAt(LocalDateTime.now().minusHours(1))
                .build());

        addScene(Scene.builder()
                .sceneId("sleep_mode")
                .name("睡眠模式")
                .description("安心入眠：关闭所有灯光、空调低噪运行、夜灯开启")
                .category(Scene.SceneCategory.SLEEP_MODE)
                .timeSlot("NIGHT")
                .userSegment("BALANCED,ENERGY_CONSCIOUS")
                .usageCount(98)
                .avgRating(4.7)
                .createdAt(LocalDateTime.now().minusDays(15))
                .lastUsedAt(LocalDateTime.now().minusMinutes(15))
                .build());

        addScene(Scene.builder()
                .sceneId("energy_saver")
                .name("节能模式")
                .description("智能节能：根据作息自动调节设备功率、优化用电")
                .category(Scene.SceneCategory.ENERGY_SAVING)
                .timeSlot("OFF_PEAK")
                .userSegment("ENERGY_CONSCIOUS")
                .usageCount(67)
                .avgRating(4.5)
                .createdAt(LocalDateTime.now().minusDays(10))
                .lastUsedAt(LocalDateTime.now().minusHours(3))
                .build());

        addScene(Scene.builder()
                .sceneId("movie_mode")
                .name("影院模式")
                .description("影院级享受：灯光渐暗、窗帘关闭、音响环绕")
                .category(Scene.SceneCategory.ENTERTAINMENT)
                .timeSlot("EVENING")
                .userSegment("BALANCED")
                .usageCount(45)
                .avgRating(4.4)
                .createdAt(LocalDateTime.now().minusDays(7))
                .lastUsedAt(LocalDateTime.now().minusDays(1))
                .build());

        addScene(Scene.builder()
                .sceneId("security_mode")
                .name("安防模式")
                .description("全面安防：门窗传感器激活、视频监控开启")
                .category(Scene.SceneCategory.SECURITY)
                .timeSlot("NIGHT")
                .userSegment("BALANCED")
                .usageCount(112)
                .avgRating(4.3)
                .createdAt(LocalDateTime.now().minusDays(25))
                .lastUsedAt(LocalDateTime.now().minusHours(5))
                .build());

        addScene(Scene.builder()
                .sceneId("return_home")
                .name("回家欢迎模式")
                .description("欢迎回家：玄关灯亮起、空调开启、播放欢迎语音")
                .category(Scene.SceneCategory.RETURN_HOME)
                .timeSlot("EVENING")
                .userSegment("FAMILY_ORIENTED")
                .usageCount(203)
                .avgRating(4.9)
                .createdAt(LocalDateTime.now().minusDays(45))
                .lastUsedAt(LocalDateTime.now().minusMinutes(10))
                .build());
    }

    private void addScene(Scene scene) {
        sceneDatabase.put(scene.getSceneId(), scene);
    }

    private void initializeSampleInteractions() {
        List<Long> sampleUsers = List.of(1L, 2L, 3L, 4L, 5L);
        List<String> sampleScenes = List.of(
                "morning_routine", "leave_home", "evening_relax", "sleep_mode",
                "energy_saver", "movie_mode", "security_mode", "return_home"
        );

        Random random = new Random(42);

        for (Long userId : sampleUsers) {
            Map<String, Double> ratings = new HashMap<>();
            List<UserSceneInteraction> interactions = new ArrayList<>();

            for (String sceneId : sampleScenes) {
                double rating = 3.0 + random.nextDouble() * 2.0;
                ratings.put(sceneId, rating);

                UserSceneInteraction interaction = UserSceneInteraction.builder()
                        .userId(userId)
                        .sceneId(sceneId)
                        .timestamp(LocalDateTime.now().minusHours(random.nextInt(72)))
                        .type(UserSceneInteraction.InteractionType.TRIGGER)
                        .completed(random.nextBoolean())
                        .durationSeconds(30 + random.nextInt(120))
                        .build();
                interactions.add(interaction);
            }

            userSceneRatings.put(userId, ratings);
            userInteractions.put(userId, interactions);
        }
    }

    public List<RecommendationResult> recommend(Long userId, int limit) {
        UserProfile profile = getUserProfile(userId);
        String userSegment = profile != null && profile.getSegmentType() != null
                ? profile.getSegmentType().name() : "BALANCED";

        Map<String, Double> scores = new HashMap<>();
        Map<String, String> reasons = new HashMap<>();
        Map<String, String> algorithms = new HashMap<>();

        Map<String, Double> collaborativeScores = collaborativeFilteringRecommend(userId);
        for (Map.Entry<String, Double> entry : collaborativeScores.entrySet()) {
            scores.merge(entry.getKey(), entry.getValue() * COLLABORATIVE_WEIGHT, Double::sum);
            algorithms.put(entry.getKey(), "COLLABORATIVE");
        }

        Map<String, Double> contentScores = contentBasedRecommend(userId, userSegment);
        for (Map.Entry<String, Double> entry : contentScores.entrySet()) {
            scores.merge(entry.getKey(), entry.getValue() * CONTENT_WEIGHT, Double::sum);
            algorithms.put(entry.getKey(), "COLLABORATIVE+CONTENT");
        }

        Map<String, Double> contextScores = contextAwareRecommend(userSegment);
        for (Map.Entry<String, Double> entry : contextScores.entrySet()) {
            scores.merge(entry.getKey(), entry.getValue() * CONTEXT_WEIGHT, Double::sum);
            algorithms.put(entry.getKey(), "CONTEXT");
        }

        Map<String, Double> popularityScores = popularityBasedRecommend();
        for (Map.Entry<String, Double> entry : popularityScores.entrySet()) {
            scores.merge(entry.getKey(), entry.getValue() * POPULARITY_WEIGHT, Double::sum);
            algorithms.putIfAbsent(entry.getKey(), "POPULARITY");
        }

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String sceneId = entry.getKey();
            Scene scene = sceneDatabase.get(sceneId);
            if (scene != null) {
                reasons.put(sceneId, generateRecommendationReason(sceneId, userId, userSegment));
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Scene scene = sceneDatabase.get(entry.getKey());
                    return RecommendationResult.builder()
                            .sceneId(entry.getKey())
                            .sceneName(scene != null ? scene.getName() : entry.getKey())
                            .score(entry.getValue())
                            .reason(reasons.get(entry.getKey()))
                            .algorithm(algorithms.get(entry.getKey()))
                            .tags(extractSceneTags(scene))
                            .segmentMatch(userSegment)
                            .rank(scores.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Double> collaborativeFilteringRecommend(Long userId) {
        Map<String, Double> targetUserRatings = userSceneRatings.get(userId);
        if (targetUserRatings == null || targetUserRatings.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Map<String, Double>> allUserRatings = new HashMap<>(userSceneRatings);
        allUserRatings.remove(userId);

        Map<String, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : allUserRatings.entrySet()) {
            double similarity = cosineSimilarity(targetUserRatings, entry.getValue());
            if (similarity > 0.1) {
                for (Map.Entry<String, Double> sceneEntry : entry.getValue().entrySet()) {
                    if (!targetUserRatings.containsKey(sceneEntry.getKey())) {
                        similarities.merge(sceneEntry.getKey(),
                                similarity * sceneEntry.getValue(), Double::sum);
                    }
                }
            }
        }

        return normalizeScores(similarities);
    }

    private Map<String, Double> contentBasedRecommend(Long userId, String userSegment) {
        Map<String, Double> scores = new HashMap<>();

        for (Map.Entry<String, Scene> entry : sceneDatabase.entrySet()) {
            Scene scene = entry.getValue();
            double score = 0.0;

            if (scene.getUserSegment() != null &&
                    scene.getUserSegment().contains(userSegment)) {
                score += 0.5;
            }

            if (scene.getAvgRating() != null) {
                score += (scene.getAvgRating() - 3.0) / 2.0 * 0.3;
            }

            String sceneTimeSlot = scene.getTimeSlot();
            if (sceneTimeSlot != null) {
                LocalDateTime now = LocalDateTime.now();
                String currentSlot = classifyTimeSlot(now.getHour());
                if (sceneTimeSlot.equals(currentSlot)) {
                    score += 0.2;
                }
            }

            scores.put(entry.getKey(), score);
        }

        return normalizeScores(scores);
    }

    private Map<String, Double> contextAwareRecommend(String userSegment) {
        Map<String, Double> scores = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        String currentTimeSlot = classifyTimeSlot(now.getHour());
        DayOfWeek day = now.getDayOfWeek();
        boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;

        for (Map.Entry<String, Scene> entry : sceneDatabase.entrySet()) {
            Scene scene = entry.getValue();
            double score = 0.0;

            if (scene.getTimeSlot() != null &&
                    scene.getTimeSlot().equals(currentTimeSlot)) {
                score += 0.6;
            }

            if (scene.getUserSegment() != null &&
                    scene.getUserSegment().contains(userSegment)) {
                score += 0.3;
            }

            if (isWeekend && "EVENING".equals(currentTimeSlot)) {
                if (scene.getCategory() == Scene.SceneCategory.ENTERTAINMENT ||
                        scene.getCategory() == Scene.SceneCategory.FAMILY_ORIENTED) {
                    score += 0.2;
                }
            }

            scores.put(entry.getKey(), score);
        }

        return normalizeScores(scores);
    }

    private Map<String, Double> popularityBasedRecommend() {
        Map<String, Double> scores = new HashMap<>();

        for (Map.Entry<String, Scene> entry : sceneDatabase.entrySet()) {
            Scene scene = entry.getValue();
            double score = 0.0;

            if (scene.getUsageCount() != null) {
                score += Math.log1p(scene.getUsageCount()) / 10.0;
            }

            if (scene.getAvgRating() != null) {
                score += (scene.getAvgRating() - 3.0) / 2.0 * 0.5;
            }

            long hoursSinceLastUsed = Duration.between(
                    scene.getLastUsedAt(), LocalDateTime.now()).toHours();
            if (hoursSinceLastUsed < 24) {
                score += 0.2;
            }

            scores.put(entry.getKey(), score);
        }

        return normalizeScores(scores);
    }

    private double cosineSimilarity(Map<String, Double> ratings1, Map<String, Double> ratings2) {
        Set<String> commonKeys = new HashSet<>(ratings1.keySet());
        commonKeys.retainAll(ratings2.keySet());

        if (commonKeys.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : commonKeys) {
            dotProduct += ratings1.get(key) * ratings2.get(key);
        }

        for (Double value : ratings1.values()) {
            norm1 += value * value;
        }
        norm1 = Math.sqrt(norm1);

        for (Double value : ratings2.values()) {
            norm2 += value * value;
        }
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    private Map<String, Double> normalizeScores(Map<String, Double> scores) {
        if (scores.isEmpty()) {
            return scores;
        }

        double max = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double min = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        if (max == min) {
            return scores;
        }

        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            normalized.put(entry.getKey(),
                    (entry.getValue() - min) / (max - min) * 0.5 + 0.5);
        }

        return normalized;
    }

    private String classifyTimeSlot(int hour) {
        if (hour >= 5 && hour < 9) return "MORNING";
        else if (hour >= 9 && hour < 18) return "DAY";
        else if (hour >= 18 && hour < 22) return "EVENING";
        else return "NIGHT";
    }

    private String generateRecommendationReason(String sceneId, Long userId, String userSegment) {
        Scene scene = sceneDatabase.get(sceneId);
        if (scene == null) {
            return "推荐您试试这个场景";
        }

        List<String> reasons = new ArrayList<>();

        if (scene.getUserSegment() != null && scene.getUserSegment().contains(userSegment)) {
            reasons.add("非常适合您的" + getSegmentDescription(userSegment) + "特点");
        }

        if (scene.getTimeSlot() != null) {
            LocalDateTime now = LocalDateTime.now();
            String currentSlot = classifyTimeSlot(now.getHour());
            if (scene.getTimeSlot().equals(currentSlot)) {
                reasons.add("现在是使用" + getTimeSlotDescription(currentSlot) + "的最佳时机");
            }
        }

        if (scene.getAvgRating() != null && scene.getAvgRating() > 4.5) {
            reasons.add("该场景评分很高（" + String.format("%.1f", scene.getAvgRating()) + "分）");
        }

        if (scene.getUsageCount() != null && scene.getUsageCount() > 100) {
            reasons.add("已有" + scene.getUsageCount() + "次使用记录");
        }

        if (reasons.isEmpty()) {
            reasons.add("根据您的使用习惯推荐");
        }

        return String.join("，", reasons);
    }

    private String getSegmentDescription(String segment) {
        return switch (segment) {
            case "EARLY_BIRD" -> "早起型";
            case "NIGHT_OWL" -> "夜猫子型";
            case "FAMILY_ORIENTED" -> "家庭型";
            case "ENERGY_CONSCIOUS" -> "节能型";
            case "TECH_ENTHUSIAST" -> "科技爱好者";
            default -> "均衡型";
        };
    }

    private String getTimeSlotDescription(String timeSlot) {
        return switch (timeSlot) {
            case "MORNING" -> "早晨";
            case "DAY" -> "白天";
            case "EVENING" -> "晚间";
            case "NIGHT" -> "夜间";
            default -> "日常";
        };
    }

    private List<String> extractSceneTags(Scene scene) {
        List<String> tags = new ArrayList<>();
        if (scene == null) return tags;

        tags.add(scene.getCategory().getDisplayName());
        if (scene.getTimeSlot() != null) {
            tags.add(getTimeSlotDescription(scene.getTimeSlot()));
        }
        if (scene.getAvgRating() != null && scene.getAvgRating() > 4.5) {
            tags.add("高评分");
        }
        if (scene.getUsageCount() != null && scene.getUsageCount() > 100) {
            tags.add("热门");
        }

        return tags;
    }

    private UserProfile getUserProfile(Long userId) {
        return null;
    }

    public void recordInteraction(UserSceneInteraction interaction) {
        userInteractions.computeIfAbsent(interaction.getUserId(),
                k -> new ArrayList<>()).add(interaction);

        Map<String, Double> ratings = userSceneRatings.computeIfAbsent(
                interaction.getUserId(), k -> new HashMap<>());

        double currentRating = ratings.getOrDefault(interaction.getSceneId(), 0.0);
        double newRating = interaction.getType() == UserSceneInteraction.InteractionType.COMPLETE
                ? Math.min(5.0, currentRating + 0.5)
                : currentRating;
        ratings.put(interaction.getSceneId(), newRating);
    }

    public Map<String, Object> getRecommendationMetrics(Long userId) {
        Map<String, Object> metrics = new HashMap<>();

        List<UserSceneInteraction> interactions = userInteractions.get(userId);
        metrics.put("totalInteractions", interactions != null ? interactions.size() : 0);

        Map<String, Double> ratings = userSceneRatings.get(userId);
        if (ratings != null && !ratings.isEmpty()) {
            double avgRating = ratings.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            metrics.put("avgRating", avgRating);
            metrics.put("ratedScenes", ratings.size());
        } else {
            metrics.put("avgRating", 0.0);
            metrics.put("ratedScenes", 0);
        }

        return metrics;
    }

    public List<Scene> getAllScenes() {
        return new ArrayList<>(sceneDatabase.values());
    }

    public Scene getScene(String sceneId) {
        return sceneDatabase.get(sceneId);
    }
}