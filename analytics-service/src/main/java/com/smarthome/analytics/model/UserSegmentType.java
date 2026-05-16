package com.smarthome.analytics.model;

public enum UserSegmentType {
    EARLY_BIRD("早起型", "习惯早起，活动高峰在早晨6-8点"),
    NIGHT_OWL("夜猫子型", "习惯晚睡，活动高峰在晚上21-23点"),
    FAMILY_ORIENTED("家庭型", "主要在中午和晚间活动，设备使用频繁"),
    ENERGY_CONSCIOUS("节能型", "注重能耗管理，使用时间规律"),
    TECH_ENTHUSIAST("科技爱好者", "喜欢尝试新功能，设备使用率高"),
    BALANCED("均衡型", "使用习惯较为均衡，无明显偏好");

    private final String name;
    private final String description;

    UserSegmentType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}