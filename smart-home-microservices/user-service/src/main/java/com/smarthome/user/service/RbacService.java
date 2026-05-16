package com.smarthome.user.service;

import com.smarthome.user.entity.UserPermission;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.entity.User;
import com.smarthome.user.repository.PermissionRepository;
import com.smarthome.user.repository.RoleRepository;
import com.smarthome.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    /**
     * 创建角色
     */
    @Transactional
    public UserRole createRole(String roleName, String description) {
        log.info("创建角色: {}", roleName);
        
        if (roleRepository.existsByRoleName(roleName)) {
            throw new RuntimeException("角色已存在: " + roleName);
        }
        
        UserRole role = new UserRole();
        role.setRoleName(roleName);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }

    /**
     * 获取角色
     */
    public Optional<UserRole> getRole(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    /**
     * 获取所有角色
     */
    public List<UserRole> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * 更新角色
     */
    @Transactional
    public UserRole updateRole(Long roleId, String roleName, String description) {
        UserRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
        
        role.setRoleName(roleName);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
        log.info("角色删除成功: {}", roleId);
    }

    /**
     * 创建权限
     */
    @Transactional
    public UserPermission createPermission(String permissionName, String description, 
                                          String resource, String action) {
        log.info("创建权限: {}", permissionName);
        
        if (permissionRepository.existsByPermissionName(permissionName)) {
            throw new RuntimeException("权限已存在: " + permissionName);
        }
        
        UserPermission permission = new UserPermission();
        permission.setPermissionName(permissionName);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        
        return permissionRepository.save(permission);
    }

    /**
     * 获取权限
     */
    public Optional<UserPermission> getPermission(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName);
    }

    /**
     * 获取所有权限
     */
    public List<UserPermission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * 更新权限
     */
    @Transactional
    public UserPermission updatePermission(Long permissionId, String permissionName, 
                                          String description, String resource, String action) {
        UserPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("权限不存在: " + permissionId));
        
        permission.setPermissionName(permissionName);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        
        return permissionRepository.save(permission);
    }

    /**
     * 删除权限
     */
    @Transactional
    public void deletePermission(Long permissionId) {
        permissionRepository.deleteById(permissionId);
        log.info("权限删除成功: {}", permissionId);
    }

    /**
     * 为角色添加权限
     */
    @Transactional
    public void addPermissionToRole(Long roleId, Long permissionId) {
        UserRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
        
        UserPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("权限不存在: " + permissionId));
        
        if (!role.getPermissions().contains(permission)) {
            role.getPermissions().add(permission);
            roleRepository.save(role);
            log.info("权限 {} 添加到角色 {}", permission.getPermissionName(), role.getRoleName());
        }
    }

    /**
     * 从角色移除权限
     */
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        UserRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
        
        UserPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("权限不存在: " + permissionId));
        
        role.getPermissions().remove(permission);
        roleRepository.save(role);
        log.info("从角色 {} 移除权限 {}", role.getRoleName(), permission.getPermissionName());
    }

    /**
     * 为用户分配角色
     */
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        UserRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
        
        user.setRole(role);
        userRepository.save(user);
        log.info("用户 {} 分配角色 {}", user.getUsername(), role.getRoleName());
    }

    /**
     * 获取用户的所有权限
     */
    public Set<String> getUserPermissions(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (user.getRole() != null && user.getRole().getPermissions() != null) {
                        return user.getRole().getPermissions().stream()
                                .map(p -> p.getResource() + ":" + p.getAction())
                                .collect(Collectors.<String>toSet());
                    }
                    return java.util.Set.<String>of();
                })
                .orElse(java.util.Set.<String>of());
    }

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(String username, String resource, String action) {
        Set<String> permissions = getUserPermissions(username);
        return permissions.contains(resource + ":" + action);
    }

    /**
     * 获取用户角色
     */
    public Optional<String> getUserRoleName(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole() != null ? user.getRole().getRoleName() : null);
    }

    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(String username, String roleName) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole() != null && roleName.equals(user.getRole().getRoleName()))
                .orElse(false);
    }
}