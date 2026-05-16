package com.smarthome.analytics.repository;

import com.smarthome.analytics.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    
    List<SensorData> findByDeviceIdOrderByTimestampDesc(String deviceId);
    
    @Query("SELECT s FROM SensorData s WHERE s.deviceId = :deviceId ORDER BY s.timestamp DESC LIMIT :limit")
    List<SensorData> findRecentByDeviceId(@Param("deviceId") String deviceId, @Param("limit") int limit);
    
    List<SensorData> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT s FROM SensorData s WHERE s.deviceId = :deviceId AND s.sensorType = :sensorType ORDER BY s.timestamp DESC LIMIT :limit")
    List<SensorData> findRecentByDeviceIdAndSensorType(@Param("deviceId") String deviceId, 
                                                       @Param("sensorType") String sensorType, 
                                                       @Param("limit") int limit);
}