package com.smarthome.analytics.service;

import com.smarthome.analytics.entity.*;
import com.smarthome.analytics.repository.EnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyManagementService {

    private final EnergyConsumptionRepository energyConsumptionRepository;

    public EnergyReport generateDailyReport(String deviceId, LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(LocalTime.MAX);
        
        List<EnergyConsumption> records = energyConsumptionRepository.findByDeviceIdAndTimeRange(
                deviceId, startTime, endTime);

        if (records.isEmpty()) {
            return EnergyReport.builder()
                    .deviceId(deviceId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .totalConsumption(0)
                    .avgPower(0)
                    .peakPower(0)
                    .highEnergyDevices(List.of())
                    .consumptionByHour(new HashMap<>())
                    .recommendation("暂无数据")
                    .build();
        }

        double totalConsumption = records.stream()
                .mapToDouble(EnergyConsumption::getEnergy)
                .sum();

        double avgPower = records.stream()
                .mapToDouble(EnergyConsumption::getPower)
                .average()
                .orElse(0);

        double peakPower = records.stream()
                .mapToDouble(EnergyConsumption::getPower)
                .max()
                .orElse(0);

        Map<String, Double> consumptionByHour = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTimestamp().getHour() + ":00",
                        Collectors.summingDouble(EnergyConsumption::getEnergy)
                ));

        List<LoadType> highEnergyDevices = identifyHighEnergyDevices(records);

        String recommendation = generateRecommendation(totalConsumption, avgPower, highEnergyDevices);

        return EnergyReport.builder()
                .deviceId(deviceId)
                .startTime(startTime)
                .endTime(endTime)
                .totalConsumption(totalConsumption)
                .avgPower(avgPower)
                .peakPower(peakPower)
                .highEnergyDevices(highEnergyDevices)
                .consumptionByHour(consumptionByHour)
                .recommendation(recommendation)
                .build();
    }

    public List<LoadType> identifyHighEnergyDevices(List<EnergyConsumption> records) {
        Map<String, LoadType> deviceLoadMap = new HashMap<>();

        for (EnergyConsumption record : records) {
            String deviceId = record.getDeviceId();
            deviceLoadMap.compute(deviceId, (id, existing) -> {
                if (existing == null) {
                    return LoadType.builder()
                            .deviceId(id)
                            .consumption(record.getEnergy())
                            .avgPower(record.getPower())
                            .peakPower(record.getPower())
                            .isHighEnergy(record.getPower() > 100)
                            .build();
                } else {
                    existing.setConsumption(existing.getConsumption() + record.getEnergy());
                    existing.setAvgPower((existing.getAvgPower() + record.getPower()) / 2);
                    existing.setPeakPower(Math.max(existing.getPeakPower(), record.getPower()));
                    existing.setHighEnergy(existing.getPeakPower() > 100);
                    return existing;
                }
            });
        }

        return deviceLoadMap.values().stream()
                .filter(LoadType::isHighEnergy)
                .sorted(Comparator.comparingDouble(LoadType::getConsumption).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getEnergyStatistics(String deviceId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        List<EnergyConsumption> records = energyConsumptionRepository.findByDeviceIdAndTimeRange(
                deviceId, startTime, endTime);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConsumption", records.stream().mapToDouble(EnergyConsumption::getEnergy).sum());
        stats.put("avgDailyConsumption", stats.get("totalConsumption"));
        stats.put("peakPower", records.stream().mapToDouble(EnergyConsumption::getPower).max().orElse(0));
        stats.put("recordCount", records.size());

        return stats;
    }

    private String generateRecommendation(double totalConsumption, double avgPower, List<LoadType> highEnergyDevices) {
        StringBuilder sb = new StringBuilder();

        if (totalConsumption > 10) {
            sb.append("今日用电量较高，建议检查高功耗设备使用情况。");
        }
        if (avgPower > 500) {
            sb.append("平均功率较高，建议关闭不必要的电器设备。");
        }
        if (!highEnergyDevices.isEmpty()) {
            sb.append(String.format("检测到%d个高能耗设备，建议优化使用时间。", highEnergyDevices.size()));
        }
        if (sb.isEmpty()) {
            sb.append("今日用电情况正常，继续保持。");
        }

        return sb.toString();
    }
}