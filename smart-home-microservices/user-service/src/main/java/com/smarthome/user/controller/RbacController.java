package com.smarthome.user.controller;

import com.smarthome.user.entity.UserPermission;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.service.RbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user/rbac")
@RequiredArgsConstructor
public class RbacController {
    
    private final RbacService rbacService;
    
    /**
     * 创建角色
     */
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> request) {
        try {
            String roleName = request.get("roleName");
            String description = request.get("description");
            
            if (roleName == null || roleName.isEmpty()) {
                return new ResponseEntity<>("角色名称不能为空", HttpStatus.BAD_REQUEST);
            }
            
            UserRole role = rbacService.createRole(roleName, description);
            return new ResponseEntity<>(role, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("创建角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserRole>> getAllRoles() {
        List<UserRole> roles = rbacService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRole> getRole(@PathVariable String roleName) {
        Optional<UserRole> role = rbacService.getRole(roleName);
        return role.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long roleId, 
                                       @RequestBody Map<String, String> request) {
        try {
            String roleName = request.get("roleName");
            String description = request.get("description");
            
            UserRole role = rbacService.updateRole(roleId, roleName, description);
            return new ResponseEntity<>(role, HttpStatus.OK);
        } catch (Exception e) {
            log.error("更新角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        rbacService.deleteRole(roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 创建权限
     */
    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPermission(@RequestBody Map<String, String> request) {
        try {
            String permissionName = request.get("permissionName");
            String description = request.get("description");
            String resource = request.get("resource");
            String action = request.get("action");
            
            if (permissionName == null || permissionName.isEmpty()) {
                return new ResponseEntity<>("权限名称不能为空", HttpStatus.BAD_REQUEST);
            }
            
            UserPermission permission = rbacService.createPermission(permissionName, description, resource, action);
            return new ResponseEntity<>(permission, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("创建权限失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 获取所有权限
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserPermission>> getAllPermissions() {
        List<UserPermission> permissions = rbacService.getAllPermissions();
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }
    
    /**
     * 获取权限详情
     */
    @GetMapping("/permissions/{permissionName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPermission> getPermission(@PathVariable String permissionName) {
        Optional<UserPermission> permission = rbacService.getPermission(permissionName);
        return permission.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 更新权限
     */
    @PutMapping("/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePermission(@PathVariable Long permissionId,
                                             @RequestBody Map<String, String> request) {
        try {
            String permissionName = request.get("permissionName");
            String description = request.get("description");
            String resource = request.get("resource");
            String action = request.get("action");
            
            UserPermission permission = rbacService.updatePermission(permissionId, permissionName, description, resource, action);
            return new ResponseEntity<>(permission, HttpStatus.OK);
        } catch (Exception e) {
            log.error("更新权限失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 删除权限
     */
    @DeleteMapping("/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        rbacService.deletePermission(permissionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 为角色添加权限
     */
    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        try {
            rbacService.addPermissionToRole(roleId, permissionId);
            return new ResponseEntity<>("权限添加成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("添加权限到角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 从角色移除权限
     */
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        try {
            rbacService.removePermissionFromRole(roleId, permissionId);
            return new ResponseEntity<>("权限移除成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("从角色移除权限失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            rbacService.assignRoleToUser(userId, roleId);
            return new ResponseEntity<>("角色分配成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("为用户分配角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 检查用户权限
     */
    @GetMapping("/users/{username}/permissions/check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> checkPermission(@PathVariable String username,
                                                   @RequestParam String resource,
                                                   @RequestParam String action) {
        boolean hasPermission = rbacService.hasPermission(username, resource, action);
        return new ResponseEntity<>(hasPermission, HttpStatus.OK);
    }
    
    /**
     * 获取用户权限列表
     */
    @GetMapping("/users/{username}/permissions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserPermissions(@PathVariable String username) {
        return new ResponseEntity<>(rbacService.getUserPermissions(username), HttpStatus.OK);
    }
}