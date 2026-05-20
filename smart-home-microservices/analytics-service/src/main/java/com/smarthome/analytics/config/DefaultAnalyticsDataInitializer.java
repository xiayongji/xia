package com.smarthome.analytics.config;

import com.smarthome.analytics.service.EnergyStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAnalyticsDataInitializer {

    private final EnergyStatisticsService energyStatisticsService;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDemoEnergyIfEmpty() {
        var today = energyStatisticsService.getTodayEnergyStatistics();
        double total = ((Number) today.getOrDefault("totalEnergy", 0.0)).doubleValue();
        if (total > 0) {
            return;
        }

        Random random = new Random(42);
        String[][] devices = {
                {"device-demo-ac", "客厅空调", "AC"},
                {"device-demo-light", "客厅主灯", "LIGHT"},
                {"device-demo-fridge", "冰箱", "APPLIANCE"},
                {"device-demo-washer", "洗衣机", "APPLIANCE"}
        };

        for (int day = 6; day >= 0; day--) {
            for (String[] d : devices) {
                double energy = 0.8 + random.nextDouble() * 4.5;
                energyStatisticsService.saveEnergyData(d[0], d[1], energy * 200, energy, d[2]);
            }
        }
        log.info("已初始化演示能耗数据（近7天）");
    }
}
