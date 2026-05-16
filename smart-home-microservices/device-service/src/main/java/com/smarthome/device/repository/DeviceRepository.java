package com.smarthome.device.repository;

import com.smarthome.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceId(String deviceId);
    Optional<Device> findByMacAddress(String macAddress);
    List<Device> findByProtocol(String protocol);
    List<Device> findByStatus(String status);
    
    @Query("SELECT d FROM Device d WHERE d.lastHeartbeat < :timeoutTime")
    List<Device> findDevicesWithExpiredHeartbeat(LocalDateTime timeoutTime);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'online'")
    Long countOnlineDevices();
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'offline'")
    Long countOfflineDevices();
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'warning'")
    Long countWarningDevices();
}