package com.smarthome.edge.repository;

import com.smarthome.edge.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {

    Optional<DeviceStatus> findByDeviceId(String deviceId);

    List<DeviceStatus> findByDeviceIdIn(List<String> deviceIds);

    List<DeviceStatus> findByStatus(String status);

    List<DeviceStatus> findByDeviceType(String deviceType);

    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.deviceId = :deviceId ORDER BY ds.lastUpdateTime DESC LIMIT 1")
    Optional<DeviceStatus> findLatestByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT ds FROM DeviceStatus ds ORDER BY ds.lastUpdateTime DESC")
    List<DeviceStatus> findAllOrderByLastUpdateTimeDesc();

    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.lastUpdateTime >= :since ORDER BY ds.lastUpdateTime DESC")
    List<DeviceStatus> findUpdatedAfter(@Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE DeviceStatus ds SET ds.status = :status, ds.lastUpdateTime = :updateTime WHERE ds.deviceId = :deviceId")
    int updateDeviceStatus(@Param("deviceId") String deviceId, @Param("status") String status, @Param("updateTime") LocalDateTime updateTime);

    @Modifying
    @Query("DELETE FROM DeviceStatus ds WHERE ds.deviceId = :deviceId")
    void deleteByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT COUNT(ds) FROM DeviceStatus ds WHERE ds.status = 'online'")
    long countOnlineDevices();

    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.deviceId IN :deviceIds ORDER BY ds.lastUpdateTime DESC")
    List<DeviceStatus> findByDeviceIdInOrderByLastUpdateTimeDesc(@Param("deviceIds") List<String> deviceIds);

    @Query(value = "SELECT ds.* FROM edge_device_status ds " +
            "INNER JOIN (SELECT device_id, MAX(last_update_time) AS max_time FROM edge_device_status GROUP BY device_id) latest " +
            "ON ds.device_id = latest.device_id AND ds.last_update_time = latest.max_time",
            nativeQuery = true)
    List<DeviceStatus> findLatestStatusForAllDevices();
}
