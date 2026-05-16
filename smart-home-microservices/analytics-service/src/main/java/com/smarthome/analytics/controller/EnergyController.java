package com.smarthome.analytics.controller;

import com.smarthome.analytics.entity.EnergyReport;
import com.smarthome.analytics.service.EnergyManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyManagementService energyManagementService;

    @GetMapping("/devices/{deviceId}/report")
    public ResponseEntity<EnergyReport> getDailyReport(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        EnergyReport report = energyManagementService.generateDailyReport(deviceId, date);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/devices/{deviceId}/report/today")
    public ResponseEntity<EnergyReport> getTodayReport(@PathVariable String deviceId) {
        EnergyReport report = energyManagementService.generateDailyReport(deviceId, LocalDate.now());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/devices/{deviceId}/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Object> stats = energyManagementService.getEnergyStatistics(deviceId, days);
        return ResponseEntity.ok(stats);
    }
}