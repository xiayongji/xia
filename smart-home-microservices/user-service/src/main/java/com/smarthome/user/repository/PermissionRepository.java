package com.smarthome.user.repository;

import com.smarthome.user.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<UserPermission, Long> {
    Optional<UserPermission> findByPermissionName(String permissionName);
    boolean existsByPermissionName(String permissionName);
    List<UserPermission> findByResource(String resource);
    List<UserPermission> findByAction(String action);
}