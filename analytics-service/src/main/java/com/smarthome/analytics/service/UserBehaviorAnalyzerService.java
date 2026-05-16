package com.smarthome.analytics.service;

import com.smarthome.analytics.model.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserBehaviorAnalyzerService {

    private static final int CLUSTER_COUNT = 6;
    private static final int MIN_SAMPLES_FOR_CLUSTERING = 5;
    private static final double CONVERGENCE_THRESHOLD = 0.001;

    private final Map<Long, UserBehavior> userBehaviors = new ConcurrentHashMap<>();
    private final Map<Long, UserProfile> userProfiles = new ConcurrentHashMap<>();
    private final Map<String, List<UserSegment>> segmentProfiles = new ConcurrentHashMap<>();
    private final Map<String, List<BehaviorPattern>> detectedPatterns = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        initializeSegmentProfiles();
        initializeSamplePatterns();
    }

    private void initializeSegmentProfiles() {
        segmentProfiles.put("EARLY_BIRD", List.of(
                UserSegment.builder()
                        .segmentId("EARLY_BIRD")
                        .segmentName("早起型")
                        .description("习惯早起，活动高峰在早晨6-8点")
                        .avgActivityScore(0.8)
                        .preferredTimeSlot("MORNING")
                        .avgDevicesUsed(5)
                        .avgScenesCreated(3)
                        .lastAnalyzed(LocalDateTime.now())
                        .build()
        ));

        segmentProfiles.put("NIGHT_OWL", List.of(
                UserSegment.builder()
                        .segmentId("NIGHT_OWL")
                        .segmentName("夜猫子型")
                        .description("习惯晚睡，活动高峰在晚上21-23点")
                        .avgActivityScore(0.7)
                        .preferredTimeSlot("NIGHT")
                        .avgDevicesUsed(4)
                        .avgScenesCreated(2)
                        .lastAnalyzed(LocalDateTime.now())
                        .build()
        ));

        segmentProfiles.put("FAMILY_ORIENTED", List.of(
                UserSegment.builder()
                        .segmentId("FAMILY_ORIENTED")
                        .segmentName("家庭型")
                        .description("主要在中午和晚间活动，设备使用频繁")
                        .avgActivityScore(0.9)
                        .preferredTimeSlot("EVENING")
                        .avgDevicesUsed(8)
                        .avgScenesCreated(5)
                        .lastAnalyzed(LocalDateTime.now())
                        .build()
        ));

        segmentProfiles.put("ENERGY_CONSCIOUS", List.of(
                UserSegment.builder()
                        .segmentId("ENERGY_CONSCIOUS")
                        .segmentName("节能型")
                        .description("注重能耗管理，使用时间规律")
                        .avgActivityScore(0.6)
                        .preferredTimeSlot("OFF_PEAK")
                        .avgDevicesUsed(3)
                        .avgScenesCreated(4)
                        .lastAnalyzed(LocalDateTime.now())
                        .build()
        ));

        segmentProfiles.put("TECH_ENTHUSIAST", List.of(
                UserSegment.builder()
                        .segmentId("TECH_ENTHUSIAST")
                        .segmentName("科技爱好者")
                        .description("喜欢尝试新功能，设备使用率高")
                        .avgActivityScore(0.95)
                        .preferredTimeSlot("VARIABLE")
                        .avgDevicesUsed(10)
                        .avgScenesCreated(8)
                        .lastAnalyzed(LocalDateTime.now())
                        .build()
        ));
    }

    private void initializeSamplePatterns() {
        detectedPatterns.put("MORNING_ROUTINE", List.of(
                BehaviorPattern.builder()
                        .patternId("MORNING_ROUTINE")
                        .patternName("晨起模式")
                        .type(PatternType.ROUTINE)
                        .triggerDevices(List.of("bedroom_light"))
                        .actionDevices(List.of("living_room_light", "air_conditioner", "coffee_machine"))
                        .occurrenceProbability(0.85)
                        .totalOccurrences(156)
                        .timeSlot("MORNING")
                        .lastDetected(LocalDateTime.now())
                        .build()
        ));

        detectedPatterns.put("LEAVE_HOME", List.of(
                BehaviorPattern.builder()
                        .patternId("LEAVE_HOME")
                        .patternName("离家模式")
                        .type(PatternType.ROUTINE)
                        .triggerDevices(List.of("door_lock"))
                        .actionDevices(List.of("all_lights", "air_conditioner"))
                        .occurrenceProbability(0.92)
                        .totalOccurrences(289)
                        .timeSlot("DAY")
                        .lastDetected(LocalDateTime.now())
                        .build()
        ));

        detectedPatterns.put("EVENING_RELAX", List.of(
                BehaviorPattern.builder()
                        .patternId("EVENING_RELAX")
                        .patternName("晚间休息")
                        .type(PatternType.CONTEXT_AWARE)
                        .triggerDevices(List.of("tv", "living_room_light"))
                        .actionDevices(List.of("sofa_light", "air_conditioner"))
                        .occurrenceProbability(0.78)
                        .totalOccurrences(134)
                        .timeSlot("EVENING")
                        .lastDetected(LocalDateTime.now())
                        .build()
        ));
    }

    public UserBehavior analyzeUserBehavior(Long userId, List<BehaviorEvent> events) {
        if (events == null || events.isEmpty()) {
            return createDefaultBehavior(userId);
        }

        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .events(events)
                .analysisTime(LocalDateTime.now())
                .build();

        calculateActivityScore(behavior);
        determinePreferredTimeSlot(behavior);
        analyzeDevicePreferences(behavior);
        analyzeScenePreferences(behavior);
        classifyUserSegment(behavior);

        userBehaviors.put(userId, behavior);
        updateUserProfile(userId, behavior);

        return behavior;
    }

    public UserProfile buildUserProfile(Long userId, List<BehaviorEvent> events) {
        UserBehavior behavior = analyzeUserBehavior(userId, events);

        Map<String, Integer> deviceUsageCount = events.stream()
                .collect(Collectors.groupingBy(
                        BehaviorEvent::getDeviceId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> sceneUsageCount = events.stream()
                .filter(e -> e.getSceneContext() != null)
                .collect(Collectors.groupingBy(
                        BehaviorEvent::getSceneContext,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .lastActiveTime(LocalDateTime.now())
                .profileUpdatedTime(LocalDateTime.now())
                .deviceUsageCount(deviceUsageCount)
                .sceneUsageCount(sceneUsageCount)
                .activityLevel(behavior.getActivityScore())
                .segmentType(UserSegmentType.valueOf(behavior.getUserSegment()))
                .lifePattern(determineLifestylePattern(behavior))
                .isEnergyConscious(determineEnergyConsciousness(events))
                .build();

        userProfiles.put(userId, profile);
        return profile;
    }

    private String determineLifestylePattern(UserBehavior behavior) {
        String timeSlot = behavior.getPreferredTimeSlot();
        return switch (timeSlot) {
            case "MORNING" -> "早起早睡型";
            case "NIGHT" -> "夜猫子型";
            case "EVENING" -> "家庭优先型";
            case "DAY" -> "规律作息型";
            default -> "自由灵活型";
        };
    }

    private boolean determineEnergyConsciousness(List<BehaviorEvent> events) {
        Map<Integer, Long> hourlyDistribution = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().getHour(),
                        Collectors.counting()
                ));

        long offPeakUsage = hourlyDistribution.entrySet().stream()
                .filter(e -> e.getKey() >= 23 || e.getKey() <= 6)
                .mapToLong(Map.Entry::getValue)
                .sum();

        double totalUsage = hourlyDistribution.values().stream().mapToLong(Long::longValue).sum();
        return totalUsage > 0 && (offPeakUsage / totalUsage) > 0.3;
    }

    public List<SceneRecommendation> recommendScenes(Long userId) {
        UserProfile profile = userProfiles.get(userId);
        UserBehavior behavior = userBehaviors.get(userId);

        if (profile == null && behavior == null) {
            return getDefaultRecommendations();
        }

        List<SceneRecommendation> recommendations = new ArrayList<>();

        addTimeBasedRecommendations(recommendations, profile, behavior);
        addPatternBasedRecommendations(recommendations);
        addDeviceBasedRecommendations(recommendations, profile);
        addCollaborativeFilteringRecommendations(recommendations, profile);

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(SceneRecommendation::getConfidence).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private void addTimeBasedRecommendations(List<SceneRecommendation> recommendations,
                                            UserProfile profile, UserBehavior behavior) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        String currentSlot = classifyCurrentTimeSlot(hour);
        String segment = behavior != null ? behavior.getUserSegment() : "BALANCED";

        if ("MORNING".equals(currentSlot) && ("EARLY_BIRD".equals(segment) || "FAMILY_ORIENTED".equals(segment))) {
            recommendations.add(SceneRecommendation.builder()
                    .sceneId("morning_routine")
                    .sceneName("晨起模式")
                    .reason("基于您的作息习惯，清晨是您最活跃的时段")
                    .confidence(0.88)
                    .triggerTime(now.toString())
                    .context("TIME_AND_SEGMENT_BASED")
                    .build());
        }

        if ("EVENING".equals(currentSlot) && "NIGHT_OWL".equals(segment)) {
            recommendations.add(SceneRecommendation.builder()
                    .sceneId("evening_relax")
                    .sceneName("晚间休息模式")
                    .reason("根据您的晚间使用习惯推荐")
                    .confidence(0.85)
                    .triggerTime(now.toString())
                    .context("EVENING_HABIT")
                    .build());
        }

        if ("NIGHT".equals(currentSlot)) {
            recommendations.add(SceneRecommendation.builder()
                    .sceneId("sleep_mode")
                    .sceneName("睡眠模式")
                    .reason("检测到夜间时段，建议开启节能模式")
                    .confidence(0.82)
                    .triggerTime(now.toString())
                    .context("NIGHT_DETECTION")
                    .build());
        }
    }

    private void addPatternBasedRecommendations(List<SceneRecommendation> recommendations) {
        List<BehaviorPattern> patterns = detectedPatterns.values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getOccurrenceProbability() > 0.7)
                .sorted(Comparator.comparingDouble(BehaviorPattern::getOccurrenceProbability).reversed())
                .limit(3)
                .toList();

        for (BehaviorPattern pattern : patterns) {
            recommendations.add(SceneRecommendation.builder()
                    .sceneId(pattern.getPatternId())
                    .sceneName(pattern.getPatternName())
                    .reason("根据您近期的行为模式 '" + pattern.getPatternName() + "' 推荐")
                    .confidence(pattern.getOccurrenceProbability())
                    .triggerTime(LocalDateTime.now().toString())
                    .context("PATTERN_BASED:" + pattern.getType().getName())
                    .build());
        }
    }

    private void addDeviceBasedRecommendations(List<SceneRecommendation> recommendations, UserProfile profile) {
        if (profile == null || profile.getDeviceUsageCount() == null) {
            return;
        }

        String mostUsedDevice = profile.getDeviceUsageCount().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostUsedDevice != null) {
            recommendations.add(SceneRecommendation.builder()
                    .sceneId("device_" + mostUsedDevice + "_auto")
                    .sceneName("设备智能联动")
                    .reason("根据您最常使用的设备: " + mostUsedDevice)
                    .confidence(0.75)
                    .triggerTime(LocalDateTime.now().toString())
                    .context("DEVICE_BASED")
                    .build());
        }
    }

    private void addCollaborativeFilteringRecommendations(List<SceneRecommendation> recommendations, UserProfile profile) {
        recommendations.add(SceneRecommendation.builder()
                .sceneId("energy_saving_recommended")
                .sceneName("节能优化模式")
                .reason((profile != null && Boolean.TRUE.equals(profile.getIsEnergyConscious()))
                        ? "根据您的节能习惯推荐"
                        : "尝试节能模式，降低用电成本")
                .confidence(0.72)
                .triggerTime(LocalDateTime.now().toString())
                .context("COLLABORATIVE_FILTERING")
                .build());
    }

    private List<SceneRecommendation> getDefaultRecommendations() {
        return List.of(
                SceneRecommendation.builder()
                        .sceneId("welcome_scene")
                        .sceneName("欢迎回家")
                        .reason("欢迎使用智能家居系统")
                        .confidence(0.9)
                        .triggerTime(LocalDateTime.now().toString())
                        .context("DEFAULT")
                        .build(),
                SceneRecommendation.builder()
                        .sceneId("energy_saving")
                        .sceneName("节能模式")
                        .reason("推荐开启节能模式")
                        .confidence(0.7)
                        .triggerTime(LocalDateTime.now().toString())
                        .context("DEFAULT")
                        .build()
        );
    }

    public List<UserSegment> performKMeansClustering(List<Long> userIds) {
        if (userIds.size() < MIN_SAMPLES_FOR_CLUSTERING) {
            return getAllSegments();
        }

        List<UserProfile> profiles = userIds.stream()
                .map(userProfiles::get)
                .filter(Objects::nonNull)
                .toList();

        if (profiles.size() < MIN_SAMPLES_FOR_CLUSTERING) {
            return getAllSegments();
        }

        double[][] features = extractClusteringFeatures(profiles);
        int[] assignments = kMeansCluster(features, CLUSTER_COUNT);

        return buildClustersFromAssignments(profiles, assignments);
    }

    private double[][] extractClusteringFeatures(List<UserProfile> profiles) {
        double[][] features = new double[profiles.size()][5];

        for (int i = 0; i < profiles.size(); i++) {
            UserProfile profile = profiles.get(i);
            features[i][0] = profile.getActivityLevel() != null ? profile.getActivityLevel() : 0.5;

            int deviceCount = profile.getDeviceUsageCount() != null ? profile.getDeviceUsageCount().size() : 0;
            features[i][1] = Math.min(deviceCount / 10.0, 1.0);

            features[i][2] = "ENERGY_CONSCIOUS".equals(profile.getSegmentType().name()) ? 1.0 : 0.0;
            features[i][3] = "TECH_ENTHUSIAST".equals(profile.getSegmentType().name()) ? 1.0 : 0.0;
            features[i][4] = Boolean.TRUE.equals(profile.getIsEnergyConscious()) ? 1.0 : 0.0;
        }

        return features;
    }

    private int[] kMeansCluster(double[][] features, int k) {
        int n = features.length;
        double[][] centroids = initializeCentroids(features, k);
        int[] assignments = new int[n];
        boolean converged = false;
        int maxIterations = 100;
        int iteration = 0;

        while (!converged && iteration < maxIterations) {
            double[][] oldCentroids = copyCentroids(centroids);

            for (int i = 0; i < n; i++) {
                assignments[i] = findNearestCentroid(features[i], centroids);
            }

            updateCentroids(features, assignments, centroids, k);
            converged = hasConverged(oldCentroids, centroids);
            iteration++;
        }

        return assignments;
    }

    private double[][] initializeCentroids(double[][] features, int k) {
        int n = features.length;
        double[][] centroids = new double[k][features[0].length];
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        Collections.shuffle(indices);

        for (int i = 0; i < k; i++) {
            centroids[i] = features[indices.get(i)].clone();
        }
        return centroids;
    }

    private int findNearestCentroid(double[] point, double[][] centroids) {
        int bestCluster = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < centroids.length; i++) {
            double distance = euclideanDistance(point, centroids[i]);
            if (distance < minDistance) {
                minDistance = distance;
                bestCluster = i;
            }
        }
        return bestCluster;
    }

    private void updateCentroids(double[][] features, int[] assignments, double[][] centroids, int k) {
        int dims = features[0].length;
        int[] counts = new int[k];

        for (int i = 0; i < k; i++) {
            counts[i] = 0;
            Arrays.fill(centroids[i], 0);
        }

        for (int i = 0; i < features.length; i++) {
            int cluster = assignments[i];
            counts[cluster]++;
            for (int j = 0; j < dims; j++) {
                centroids[cluster][j] += features[i][j];
            }
        }

        for (int i = 0; i < k; i++) {
            if (counts[i] > 0) {
                for (int j = 0; j < dims; j++) {
                    centroids[i][j] /= counts[i];
                }
            }
        }
    }

    private boolean hasConverged(double[][] oldCentroids, double[][] newCentroids) {
        double maxShift = 0;
        for (int i = 0; i < oldCentroids.length; i++) {
            double shift = euclideanDistance(oldCentroids[i], newCentroids[i]);
            maxShift = Math.max(maxShift, shift);
        }
        return maxShift < CONVERGENCE_THRESHOLD;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(sum);
    }

    private double[][] copyCentroids(double[][] centroids) {
        double[][] copy = new double[centroids.length][centroids[0].length];
        for (int i = 0; i < centroids.length; i++) {
            copy[i] = centroids[i].clone();
        }
        return copy;
    }

    private List<UserSegment> buildClustersFromAssignments(List<UserProfile> profiles, int[] assignments) {
        Map<Integer, List<UserProfile>> clusterGroups = new HashMap<>();

        for (int i = 0; i < assignments.length; i++) {
            int cluster = assignments[i];
            clusterGroups.computeIfAbsent(cluster, k -> new ArrayList<>()).add(profiles.get(i));
        }

        return clusterGroups.entrySet().stream()
                .map(entry -> {
                    List<UserProfile> clusterProfiles = entry.getValue();
                    double avgActivity = clusterProfiles.stream()
                            .mapToDouble(p -> p.getActivityLevel() != null ? p.getActivityLevel() : 0)
                            .average().orElse(0);

                    int avgDevices = (int) clusterProfiles.stream()
                            .mapToInt(p -> p.getDeviceUsageCount() != null ? p.getDeviceUsageCount().size() : 0)
                            .average().orElse(0);

                    UserSegmentType type = determineClusterType(avgActivity, avgDevices);

                    return UserSegment.builder()
                            .segmentId("CLUSTER_" + entry.getKey())
                            .segmentName("用户群体 " + (entry.getKey() + 1))
                            .description(type.getDescription())
                            .avgActivityScore(avgActivity)
                            .preferredTimeSlot("MIXED")
                            .avgDevicesUsed(avgDevices)
                            .avgScenesCreated(3)
                            .lastAnalyzed(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private UserSegmentType determineClusterType(double activityLevel, int deviceCount) {
        if (activityLevel > 0.8 && deviceCount > 7) {
            return UserSegmentType.TECH_ENTHUSIAST;
        } else if (activityLevel > 0.7) {
            return UserSegmentType.FAMILY_ORIENTED;
        } else if (deviceCount < 4) {
            return UserSegmentType.ENERGY_CONSCIOUS;
        } else {
            return UserSegmentType.BALANCED;
        }
    }

    private String classifyCurrentTimeSlot(int hour) {
        if (hour >= 5 && hour < 9) return "MORNING";
        else if (hour >= 9 && hour < 18) return "DAY";
        else if (hour >= 18 && hour < 22) return "EVENING";
        else return "NIGHT";
    }

    private void updateUserProfile(Long userId, UserBehavior behavior) {
        userProfiles.computeIfAbsent(userId, k -> UserProfile.builder()
                .userId(userId)
                .profileUpdatedTime(LocalDateTime.now())
                .build());

        UserProfile profile = userProfiles.get(userId);
        profile.setActivityLevel(behavior.getActivityScore());
        profile.setSegmentType(UserSegmentType.valueOf(behavior.getUserSegment()));
        profile.setLastActiveTime(LocalDateTime.now());
        profile.setProfileUpdatedTime(LocalDateTime.now());
    }

    public UserBehavior getUserBehavior(Long userId) {
        return userBehaviors.get(userId);
    }

    public UserProfile getUserProfile(Long userId) {
        return userProfiles.get(userId);
    }

    public Map<String, Object> getBehaviorAnalysisSummary(Long userId) {
        UserBehavior behavior = userBehaviors.get(userId);
        UserProfile profile = userProfiles.get(userId);

        if (behavior == null && profile == null) {
            return Map.of(
                    "userId", userId,
                    "status", "NO_DATA",
                    "message", "暂无行为数据"
            );
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);

        if (behavior != null) {
            summary.put("segment", behavior.getUserSegment());
            summary.put("activityScore", behavior.getActivityScore());
            summary.put("preferredTimeSlot", behavior.getPreferredTimeSlot());
            summary.put("totalEvents", behavior.getEvents().size());
        }

        if (profile != null) {
            summary.put("segmentType", profile.getSegmentType());
            summary.put("lifePattern", profile.getLifePattern());
            summary.put("isEnergyConscious", profile.getIsEnergyConscious());
            summary.put("lastActiveTime", profile.getLastActiveTime());
        }

        return summary;
    }

    public Map<String, List<AnomalyResult>> detectUserBehaviorAnomalies(Long userId) {
        UserBehavior behavior = userBehaviors.get(userId);

        if (behavior == null || behavior.getEvents().isEmpty()) {
            return Collections.emptyMap();
        }

        List<AnomalyResult> anomalies = new ArrayList<>();

        if (behavior.getActivityScore() < 0.2) {
            anomalies.add(AnomalyResult.builder()
                    .deviceId("user_" + userId)
                    .score(0.8)
                    .isAnomaly(true)
                    .level("MEDIUM")
                    .anomalyType("LOW_ACTIVITY")
                    .description("用户活跃度异常低")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        return Map.of("user_" + userId, anomalies);
    }

    public List<UserSegment> getAllSegments() {
        return segmentProfiles.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private UserBehavior createDefaultBehavior(Long userId) {
        return UserBehavior.builder()
                .userId(userId)
                .events(Collections.emptyList())
                .analysisTime(LocalDateTime.now())
                .userSegment("BALANCED")
                .activityScore(0.0)
                .preferredTimeSlot("UNKNOWN")
                .frequentlyUsedDevices(Collections.emptyList())
                .favoriteScenes(Collections.emptyList())
                .build();
    }

    private void calculateActivityScore(UserBehavior behavior) {
        List<BehaviorEvent> events = behavior.getEvents();
        if (events.isEmpty()) {
            behavior.setActivityScore(0.0);
            return;
        }

        Map<LocalDate, Long> dailyEventCount = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().toLocalDate(),
                        Collectors.counting()
                ));

        double avgDailyEvents = dailyEventCount.values().stream()
                .mapToLong(Long::longValue)
                .average().orElse(0.0);

        behavior.setActivityScore(Math.min(avgDailyEvents / 20.0, 1.0));
    }

    private void determinePreferredTimeSlot(UserBehavior behavior) {
        List<BehaviorEvent> events = behavior.getEvents();
        if (events.isEmpty()) {
            behavior.setPreferredTimeSlot("UNKNOWN");
            return;
        }

        Map<TimeSlot, Long> slotCounts = events.stream()
                .collect(Collectors.groupingBy(
                        this::classifyTimeSlot,
                        Collectors.counting()
                ));

        TimeSlot preferredSlot = slotCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TimeSlot.UNKNOWN);

        behavior.setPreferredTimeSlot(preferredSlot.name());
    }

    private TimeSlot classifyTimeSlot(BehaviorEvent event) {
        int hour = event.getTimestamp().getHour();
        if (hour >= 5 && hour < 9) return TimeSlot.MORNING;
        else if (hour >= 9 && hour < 12) return TimeSlot.FORENOON;
        else if (hour >= 12 && hour < 14) return TimeSlot.NOON;
        else if (hour >= 14 && hour < 18) return TimeSlot.AFTERNOON;
        else if (hour >= 18 && hour < 22) return TimeSlot.EVENING;
        else return TimeSlot.NIGHT;
    }

    private enum TimeSlot {
        MORNING, FORENOON, NOON, AFTERNOON, EVENING, NIGHT, UNKNOWN
    }

    private void analyzeDevicePreferences(UserBehavior behavior) {
        List<String> frequentlyUsed = behavior.getEvents().stream()
                .collect(Collectors.groupingBy(
                        BehaviorEvent::getDeviceId,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        behavior.setFrequentlyUsedDevices(frequentlyUsed);
    }

    private void analyzeScenePreferences(UserBehavior behavior) {
        Map<String, Long> sceneContextCount = behavior.getEvents().stream()
                .filter(e -> e.getSceneContext() != null)
                .collect(Collectors.groupingBy(
                        BehaviorEvent::getSceneContext,
                        Collectors.counting()
                ));

        List<String> favoriteScenes = sceneContextCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        behavior.setFavoriteScenes(favoriteScenes);
    }

    private void classifyUserSegment(UserBehavior behavior) {
        String preferredSlot = behavior.getPreferredTimeSlot();
        double activityScore = behavior.getActivityScore();
        int deviceCount = behavior.getFrequentlyUsedDevices().size();

        String segment = "BALANCED";
        if ("NIGHT".equals(preferredSlot) && activityScore > 0.6) {
            segment = "NIGHT_OWL";
        } else if ("MORNING".equals(preferredSlot) && activityScore > 0.7) {
            segment = "EARLY_BIRD";
        } else if ("EVENING".equals(preferredSlot) && deviceCount >= 5) {
            segment = "FAMILY_ORIENTED";
        } else if (activityScore < 0.4 && deviceCount <= 3) {
            segment = "ENERGY_CONSCIOUS";
        }

        behavior.setUserSegment(segment);
    }
}