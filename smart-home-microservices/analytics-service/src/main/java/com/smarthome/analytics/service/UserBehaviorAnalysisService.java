package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.UserBehavior;
import com.smarthome.analytics.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorAnalysisService {

    private final UserBehaviorRepository behaviorRepository;

    /**
     * 记录用户行为
     */
    @Transactional
    public UserBehavior recordBehavior(String userId, String username, String behaviorType,
                                      String deviceId, String deviceName, String action, String category) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setUsername(username);
        behavior.setBehaviorType(behaviorType);
        behavior.setDeviceId(deviceId);
        behavior.setDeviceName(deviceName);
        behavior.setAction(action);
        behavior.setCategory(category);
        behavior.setTimestamp(LocalDateTime.now());
        
        return behaviorRepository.save(behavior);
    }

    /**
     * 获取用户行为历史
     */
    public List<UserBehavior> getUserBehaviorHistory(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return behaviorRepository.findByUserIdAndTimeRange(userId, startTime, endTime);
    }

    /**
     * 获取用户行为统计
     */
    public Map<String, Object> getUserBehaviorStatistics(String userId) {
        Map<String, Object> statistics = new HashMap<>();
        
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        
        List<Object[]> behaviorTypeStats = behaviorRepository.countByBehaviorType(userId, lastWeek);
        statistics.put("behaviorTypeStats", behaviorTypeStats);
        
        List<Object[]> deviceStats = behaviorRepository.countByDevice(userId, lastWeek);
        statistics.put("deviceStats", deviceStats);
        
        return statistics;
    }

    /**
     * 获取用户常用设备
     */
    public List<Map<String, Object>> getUserFrequentDevices(String userId, int limit) {
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        List<Object[]> deviceStats = behaviorRepository.countByDevice(userId, lastMonth);
        
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (Object[] row : deviceStats) {
            if (count >= limit) break;
            
            Map<String, Object> stat = new HashMap<>();
            stat.put("deviceId", row[0]);
            stat.put("count", row[1]);
            result.add(stat);
            count++;
        }
        
        return result;
    }

    /**
     * 获取用户活跃时段
     */
    public Map<String, Object> getUserActivePeriods(String userId) {
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<UserBehavior> behaviors = behaviorRepository.findByUserIdAndTimeRange(userId, lastWeek, LocalDateTime.now());
        
        int[] hourlyCounts = new int[24];
        int[] weekdayCounts = new int[7];
        
        for (UserBehavior behavior : behaviors) {
            int hour = behavior.getTimestamp().getHour();
            int dayOfWeek = behavior.getTimestamp().getDayOfWeek().getValue() - 1;
            
            hourlyCounts[hour]++;
            weekdayCounts[dayOfWeek]++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("hourlyCounts", hourlyCounts);
        result.put("weekdayCounts", weekdayCounts);
        
        return result;
    }

    /**
     * 分析用户使用模式
     */
    public Map<String, Object> analyzeUserPatterns(String userId) {
        Map<String, Object> patterns = new HashMap<>();
        
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        List<UserBehavior> behaviors = behaviorRepository.findByUserIdAndTimeRange(userId, lastMonth, LocalDateTime.now());
        
        Map<String, List<LocalTime>> deviceUsageTimes = new HashMap<>();
        
        for (UserBehavior behavior : behaviors) {
            String deviceKey = behavior.getDeviceId();
            LocalTime time = behavior.getTimestamp().toLocalTime();
            
            deviceUsageTimes.computeIfAbsent(deviceKey, k -> new ArrayList<>()).add(time);
        }
        
        Map<String, String> suggestedScenes = new HashMap<>();
        
        for (Map.Entry<String, List<LocalTime>> entry : deviceUsageTimes.entrySet()) {
            List<LocalTime> times = entry.getValue();
            if (times.size() >= 5) {
                LocalTime avgTime = calculateAverageTime(times);
                suggestedScenes.put(entry.getKey(), avgTime.toString());
            }
        }
        
        patterns.put("suggestedScenes", suggestedScenes);
        
        Map<String, Integer> actionCounts = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            String action = behavior.getAction();
            actionCounts.merge(action, 1, Integer::sum);
        }
        patterns.put("actionCounts", actionCounts);
        
        return patterns;
    }

    /**
     * 获取总体行为统计
     */
    public Map<String, Object> getOverallBehaviorStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<Object[]> categoryStats = behaviorRepository.countByCategory(lastWeek);
        statistics.put("byCategory", categoryStats);
        
        return statistics;
    }

    /**
     * 定时任务：分析用户行为模式
     */
    @Scheduled(fixedRateString = "${analytics.behavior.analysis-interval:3600000}")
    public void periodicBehaviorAnalysis() {
        log.info("执行定时用户行为分析");
        // 可以扩展实现定期分析逻辑
    }

    private LocalTime calculateAverageTime(List<LocalTime> times) {
        long totalSeconds = times.stream()
                .mapToLong(t -> t.toSecondOfDay())
                .sum();
        long avgSeconds = totalSeconds / times.size();
        
        return LocalTime.ofSecondOfDay(avgSeconds);
    }
}