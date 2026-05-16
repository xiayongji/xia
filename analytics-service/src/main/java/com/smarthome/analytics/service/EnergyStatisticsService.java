package com.smarthome.analytics.service;

import com.smarthome.analytics.model.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EnergyStatisticsService {

    private static final double ELECTRICITY_PRICE = 0.6;

    private final Map<String, List<EnergyRecord>> deviceEnergyHistory = new ConcurrentHashMap<>();

    public EnergyStat getDailyEnergy(String deviceId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<EnergyRecord> records = getEnergyRecordsInRange(deviceId, start, end);

        if (records.isEmpty()) {
            return EnergyStat.builder()
                    .deviceId(deviceId)
                    .date(date)
                    .totalEnergy(0.0)
                    .avgPower(0.0)
                    .maxPower(0.0)
                    .minPower(0.0)
                    .usageCount(0)
                    .records(Collections.emptyList())
                    .cost(0.0)
                    .build();
        }

        DoubleSummaryStatistics stats = records.stream()
                .mapToDouble(EnergyRecord::getEnergy)
                .summaryStatistics();

        double totalCost = stats.getSum() * ELECTRICITY_PRICE;

        return EnergyStat.builder()
                .deviceId(deviceId)
                .date(date)
                .totalEnergy(stats.getSum())
                .avgPower(stats.getAverage())
                .maxPower(stats.getMax())
                .minPower(stats.getMin())
                .usageCount((int) stats.getCount())
                .records(records)
                .cost(totalCost)
                .build();
    }

    public EnergyStat getWeeklyEnergy(String deviceId, LocalDate startDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusDays(7).atStartOfDay();

        List<EnergyRecord> records = getEnergyRecordsInRange(deviceId, start, end);

        DoubleSummaryStatistics stats = records.stream()
                .mapToDouble(EnergyRecord::getEnergy)
                .summaryStatistics();

        double dailyAvg = stats.getSum() / 7;
        double cost = stats.getSum() * ELECTRICITY_PRICE;

        return EnergyStat.builder()
                .deviceId(deviceId)
                .date(startDate)
                .totalEnergy(stats.getSum())
                .avgPower(dailyAvg)
                .maxPower(stats.getMax())
                .minPower(stats.getMin())
                .usageCount((int) stats.getCount())
                .records(records)
                .cost(cost)
                .build();
    }

    public EnergyStat getMonthlyEnergy(String deviceId, LocalDate startDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusMonths(1).atStartOfDay();

        List<EnergyRecord> records = getEnergyRecordsInRange(deviceId, start, end);

        DoubleSummaryStatistics stats = records.stream()
                .mapToDouble(EnergyRecord::getEnergy)
                .summaryStatistics();

        double dailyAvg = stats.getSum() / startDate.lengthOfMonth();
        double cost = stats.getSum() * ELECTRICITY_PRICE;

        return EnergyStat.builder()
                .deviceId(deviceId)
                .date(startDate)
                .totalEnergy(stats.getSum())
                .avgPower(dailyAvg)
                .maxPower(stats.getMax())
                .minPower(stats.getMin())
                .usageCount((int) stats.getCount())
                .records(records)
                .cost(cost)
                .build();
    }

    public CompareResult compareEnergy(String deviceId, LocalDate startDate, LocalDate endDate) {
        double current = getTotalEnergyInRange(deviceId, startDate, endDate);

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate lastStart = startDate.minusDays(days);
        LocalDate lastEnd = startDate.minusDays(1);
        double previous = getTotalEnergyInRange(deviceId, lastStart, lastEnd);

        double changeRate = previous > 0 ? (current - previous) / previous * 100 : 0;

        String trend = changeRate > 5 ? "UP" : changeRate < -5 ? "DOWN" : "STABLE";
        String description = generateComparisonDescription(changeRate, trend);

        return CompareResult.builder()
                .current(current)
                .previous(previous)
                .changeRate(changeRate)
                .trend(trend)
                .description(description)
                .build();
    }

    private String generateComparisonDescription(double changeRate, String trend) {
        if ("UP".equals(trend)) {
            return String.format("能耗较上一周期上升 %.1f%%，建议检查设备运行状态", changeRate);
        } else if ("DOWN".equals(trend)) {
            return String.format("能耗较上一周期下降 %.1f%%，节能效果良好", Math.abs(changeRate));
        } else {
            return "能耗较上一周期基本持平";
        }
    }

    private List<EnergyRecord> getEnergyRecordsInRange(String deviceId, LocalDateTime start, LocalDateTime end) {
        List<EnergyRecord> history = deviceEnergyHistory.getOrDefault(deviceId, Collections.emptyList());

        return history.stream()
                .filter(r -> !r.getTimestamp().isBefore(start) && r.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }

    private double getTotalEnergyInRange(String deviceId, LocalDate start, LocalDate end) {
        List<EnergyRecord> records = getEnergyRecordsInRange(
                deviceId,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay()
        );

        return records.stream()
                .mapToDouble(EnergyRecord::getEnergy)
                .sum();
    }

    public void addEnergyRecord(EnergyRecord record) {
        deviceEnergyHistory.computeIfAbsent(record.getDeviceId(), k -> Collections.synchronizedList(new ArrayList<>()));
        deviceEnergyHistory.get(record.getDeviceId()).add(record);
    }

    public EnergyAnalysisReport generateAnalysisReport(String deviceId, LocalDate date) {
        EnergyStat daily = getDailyEnergy(deviceId, date);
        EnergyStat weekly = getWeeklyEnergy(deviceId, date.minusDays(date.getDayOfWeek().getValue() - 1));
        CompareResult comparison = compareEnergy(deviceId, date.minusDays(7), date);

        String efficiencyGrade = calculateEfficiencyGrade(daily.getAvgPower(), deviceId);
        List<String> suggestions = generateEnergySavingSuggestions(deviceId, daily, weekly);

        return EnergyAnalysisReport.builder()
                .deviceId(deviceId)
                .reportTime(LocalDateTime.now())
                .totalEnergy(daily.getTotalEnergy())
                .avgDailyEnergy(weekly.getTotalEnergy() / 7)
                .peakPower(daily.getMaxPower())
                .costEstimate(daily.getCost())
                .efficiencyGrade(efficiencyGrade)
                .suggestions(suggestions)
                .comparedToLastPeriod(comparison)
                .build();
    }

    private String calculateEfficiencyGrade(double avgPower, String deviceId) {
        if (avgPower < 50) {
            return "A+";
        } else if (avgPower < 100) {
            return "A";
        } else if (avgPower < 200) {
            return "B";
        } else if (avgPower < 500) {
            return "C";
        } else if (avgPower < 1000) {
            return "D";
        } else {
            return "E";
        }
    }

    private List<String> generateEnergySavingSuggestions(String deviceId, EnergyStat daily, EnergyStat weekly) {
        List<String> suggestions = new ArrayList<>();

        if (daily.getAvgPower() > weekly.getTotalEnergy() / 7 * 1.2) {
            suggestions.add("当前设备能耗高于日常平均水平，建议检查设备是否处于异常状态");
        }

        if (daily.getMaxPower() > daily.getAvgPower() * 2) {
            suggestions.add("检测到功率峰值，建议避免同时使用多个高功率设备");
        }

        int hour = LocalDateTime.now().getHour();
        if (hour >= 18 && hour <= 21 && daily.getAvgPower() > 500) {
            suggestions.add("当前处于用电高峰时段（18:00-21:00），建议错峰使用高功率设备");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("设备运行状态良好，能耗正常");
        }

        return suggestions;
    }

    public Map<String, Object> getOverallEnergySummary(LocalDate date) {
        Map<String, Object> summary = new HashMap<>();

        double totalEnergy = deviceEnergyHistory.values().stream()
                .flatMap(List::stream)
                .filter(r -> r.getTimestamp().toLocalDate().equals(date))
                .mapToDouble(EnergyRecord::getEnergy)
                .sum();

        summary.put("date", date);
        summary.put("totalEnergy", totalEnergy);
        summary.put("totalCost", totalEnergy * ELECTRICITY_PRICE);
        summary.put("deviceCount", deviceEnergyHistory.size());

        Map<String, Double> deviceEnergy = new HashMap<>();
        deviceEnergyHistory.forEach((deviceId, records) -> {
            double energy = records.stream()
                    .filter(r -> r.getTimestamp().toLocalDate().equals(date))
                    .mapToDouble(EnergyRecord::getEnergy)
                    .sum();
            if (energy > 0) {
                deviceEnergy.put(deviceId, energy);
            }
        });

        summary.put("deviceBreakdown", deviceEnergy);

        return summary;
    }
}