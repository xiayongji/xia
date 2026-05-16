package com.smarthome.device.repository;

import com.smarthome.device.entity.DeviceShadow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceShadowRepository extends JpaRepository<DeviceShadow, Long> {
    Optional<DeviceShadow> findByDeviceId(String deviceId);
    void deleteByDeviceId(String deviceId);
}