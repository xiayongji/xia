package com.smarthome.analytics.entity;

public enum AnomalyLevel {
    NORMAL(0.0, 0.3),
    WARNING(0.3, 0.6),
    CRITICAL(0.6, 0.85),
    SEVERE(0.85, 1.0);

    private final double minScore;
    private final double maxScore;

    AnomalyLevel(double minScore, double maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static AnomalyLevel fromScore(double score) {
        for (AnomalyLevel level : values()) {
            if (score >= level.minScore && score < level.maxScore) {
                return level;
            }
        }
        return SEVERE;
    }

    public boolean isCritical() {
        return this == CRITICAL || this == SEVERE;
    }
}