package com.smarthome.edge.repository;

import com.smarthome.edge.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    Optional<Device> findByDeviceId(String deviceId);

    List<Device> findByStatus(String status);

    List<Device> findByDeviceType(String deviceType);

    List<Device> findByProtocol(String protocol);

    @Query("SELECT d FROM Device d WHERE d.status = 'online'")
    List<Device> findAllOnlineDevices();

    @Query("SELECT d FROM Device d ORDER BY d.updatedAt DESC")
    List<Device> findAllOrderByUpdateTimeDesc();

    boolean existsByDeviceId(String deviceId);
}
