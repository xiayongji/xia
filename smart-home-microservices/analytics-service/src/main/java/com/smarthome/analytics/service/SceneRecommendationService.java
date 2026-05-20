package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.UserBehavior;
import com.smarthome.analytics.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SceneRecommendationService {

    private final UserBehaviorRepository behaviorRepository;

    public List<Map<String, Object>> recommendScenes(Long userId, int limit) {
        String uid = String.valueOf(userId);
        LocalDateTime since = LocalDateTime.now().minusDays(14);
        List<UserBehavior> behaviors = behaviorRepository.findByUserIdAndTimeRange(
                uid, since, LocalDateTime.now());

        Map<String, Long> sceneCounts = behaviors.stream()
                .filter(b -> b.getAction() != null && b.getAction().contains("SCENE"))
                .collect(Collectors.groupingBy(UserBehavior::getAction, Collectors.counting()));

        List<Map<String, Object>> presets = List.of(
                preset("home", "回家模式", "开启灯光和空调", 0.92),
                preset("leave", "离家模式", "关闭灯光和空调", 0.88),
                preset("sleep", "睡眠模式", "关闭灯光", 0.85),
                preset("read", "阅读模式", "调节灯光亮度", 0.80)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> p : presets) {
            String key = (String) p.get("sceneKey");
            long boost = sceneCounts.getOrDefault("SCENE_" + key.toUpperCase(), 0L);
            double score = (double) p.get("score") + Math.min(boost * 0.02, 0.08);
            p.put("score", Math.min(score, 0.99));
            p.put("reason", boost > 0 ? "根据您近期使用习惯推荐" : "热门场景推荐");
            result.add(p);
        }

        result.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    private Map<String, Object> preset(String key, String name, String desc, double score) {
        Map<String, Object> m = new HashMap<>();
        m.put("sceneKey", key);
        m.put("sceneName", name);
        m.put("description", desc);
        m.put("score", score);
        return m;
    }
}
