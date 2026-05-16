package com.smarthome.analytics.repository;

import com.smarthome.analytics.entity.LoadProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoadProfileRepository extends JpaRepository<LoadProfile, Long> {
    
    Optional<LoadProfile> findByDeviceId(String deviceId);
    
    Optional<LoadProfile> findByDeviceType(String deviceType);
    
    boolean existsByDeviceId(String deviceId);
}