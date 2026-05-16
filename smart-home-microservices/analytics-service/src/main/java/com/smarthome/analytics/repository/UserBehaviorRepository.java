package com.smarthome.analytics.repository;

import com.smarthome.analytics.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    
    List<UserBehavior> findByUserIdOrderByTimestampDesc(String userId);
    
    @Query("SELECT b FROM UserBehavior b WHERE b.userId = :userId AND b.timestamp BETWEEN :startTime AND :endTime")
    List<UserBehavior> findByUserIdAndTimeRange(@Param("userId") String userId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT b.behaviorType, COUNT(b) FROM UserBehavior b WHERE b.userId = :userId AND b.timestamp >= :since GROUP BY b.behaviorType ORDER BY COUNT(b) DESC")
    List<Object[]> countByBehaviorType(@Param("userId") String userId,
                                       @Param("since") LocalDateTime since);
    
    @Query("SELECT b.deviceId, COUNT(b) FROM UserBehavior b WHERE b.userId = :userId AND b.timestamp >= :since GROUP BY b.deviceId ORDER BY COUNT(b) DESC")
    List<Object[]> countByDevice(@Param("userId") String userId,
                                 @Param("since") LocalDateTime since);
    
    @Query("SELECT b.category, COUNT(b) FROM UserBehavior b WHERE b.timestamp >= :since GROUP BY b.category ORDER BY COUNT(b) DESC")
    List<Object[]> countByCategory(@Param("since") LocalDateTime since);
    
    @Query("SELECT b.action, COUNT(b) FROM UserBehavior b WHERE b.userId = :userId AND b.behaviorType = :behaviorType AND b.timestamp >= :since GROUP BY b.action ORDER BY COUNT(b) DESC")
    List<Object[]> countByAction(@Param("userId") String userId,
                                 @Param("behaviorType") String behaviorType,
                                 @Param("since") LocalDateTime since);
}