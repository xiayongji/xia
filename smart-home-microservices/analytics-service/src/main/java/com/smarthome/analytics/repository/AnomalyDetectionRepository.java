package com.smarthome.analytics.repository;

import com.smarthome.analytics.entity.AnomalyDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetection, Long> {
    
    List<AnomalyDetection> findByDeviceIdOrderByDetectedAtDesc(String deviceId);
    
    List<AnomalyDetection> findByStatusOrderByDetectedAtDesc(String status);
    
    @Query("SELECT a FROM AnomalyDetection a WHERE a.detectedAt BETWEEN :startTime AND :endTime")
    List<AnomalyDetection> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a.deviceId, COUNT(a) FROM AnomalyDetection a WHERE a.status = 'pending' GROUP BY a.deviceId ORDER BY COUNT(a) DESC")
    List<Object[]> countPendingAnomaliesByDevice();
    
    @Query("SELECT a.anomalyType, COUNT(a) FROM AnomalyDetection a WHERE a.detectedAt >= :since GROUP BY a.anomalyType ORDER BY COUNT(a) DESC")
    List<Object[]> countByAnomalyType(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AnomalyDetection a WHERE a.status = 'pending' ORDER BY a.detectedAt ASC")
    List<AnomalyDetection> findPendingAnomalies();
    
    @Query("SELECT a FROM AnomalyDetection a WHERE a.confidence >= :threshold ORDER BY a.detectedAt DESC")
    List<AnomalyDetection> findHighConfidenceAnomalies(@Param("threshold") Double threshold);
}