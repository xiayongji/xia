package com.smarthome.user.repository;

import com.smarthome.user.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    @Query("SELECT o FROM OperationLog o WHERE o.user.username = :username ORDER BY o.timestamp DESC")
    List<OperationLog> findByUsernameOrderByTimestampDesc(@Param("username") String username);
    
    @Query("SELECT o FROM OperationLog o WHERE o.timestamp BETWEEN :startTime AND :endTime")
    List<OperationLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT o FROM OperationLog o WHERE o.resource = :resource ORDER BY o.timestamp DESC")
    List<OperationLog> findByResourceOrderByTimestampDesc(@Param("resource") String resource);
    
    @Query("SELECT o FROM OperationLog o WHERE o.success = :success ORDER BY o.timestamp DESC")
    List<OperationLog> findBySuccessOrderByTimestampDesc(@Param("success") boolean success);
    
    @Query("SELECT o.operation, COUNT(o) FROM OperationLog o WHERE o.timestamp >= :since GROUP BY o.operation")
    List<Object[]> countByOperation(@Param("since") LocalDateTime since);
}