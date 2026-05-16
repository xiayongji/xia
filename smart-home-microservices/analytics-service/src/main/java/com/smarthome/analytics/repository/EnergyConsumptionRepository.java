package com.smarthome.analytics.repository;

import com.smarthome.analytics.entity.EnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, Long> {
    
    List<EnergyConsumption> findByDeviceIdOrderByTimestampDesc(String deviceId);
    
    @Query("SELECT e FROM EnergyConsumption e WHERE e.deviceId = :deviceId AND e.timestamp BETWEEN :startTime AND :endTime")
    List<EnergyConsumption> findByDeviceIdAndTimeRange(@Param("deviceId") String deviceId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT e.deviceId, SUM(e.energy) FROM EnergyConsumption e WHERE e.timestamp BETWEEN :startTime AND :endTime GROUP BY e.deviceId ORDER BY SUM(e.energy) DESC")
    List<Object[]> sumEnergyByDevice(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT SUM(e.energy) FROM EnergyConsumption e WHERE e.timestamp BETWEEN :startTime AND :endTime")
    Double sumTotalEnergy(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT AVG(e.power) FROM EnergyConsumption e WHERE e.deviceId = :deviceId AND e.timestamp BETWEEN :startTime AND :endTime")
    Double avgPowerByDevice(@Param("deviceId") String deviceId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT e.deviceType, SUM(e.energy) FROM EnergyConsumption e WHERE e.timestamp BETWEEN :startTime AND :endTime GROUP BY e.deviceType")
    List<Object[]> sumEnergyByDeviceType(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT e FROM EnergyConsumption e WHERE e.timestamp >= :since ORDER BY e.timestamp DESC")
    List<EnergyConsumption> findRecentConsumption(@Param("since") LocalDateTime since);
}