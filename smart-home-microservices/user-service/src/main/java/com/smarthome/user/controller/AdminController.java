package com.smarthome.user.controller;

import com.smarthome.user.entity.OperationLog;
import com.smarthome.user.entity.User;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.repository.OperationLogRepository;
import com.smarthome.user.service.RbacService;
import com.smarthome.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员控制器 - 提供完整的用户权限管理功能
 * 参考美的、小米之家等智能家居大厂的管理员体系
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final RbacService rbacService;
    private final OperationLogRepository operationLogRepository;
    
    /**
     * 获取系统统计信息
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", allUsers.size());
            stats.put("activeUsers", allUsers.stream().filter(User::isEnabled).count());
            stats.put("inactiveUsers", allUsers.stream().filter(u -> !u.isEnabled()).count());
            stats.put("totalRoles", rbacService.getAllRoles().size());
            stats.put("totalPermissions", rbacService.getAllPermissions().size());
            stats.put("recentLogs", operationLogRepository.countByTimestampAfter(java.time.LocalDateTime.now().minusDays(1)));
            
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            Page<User> userPage = userService.getUsersPaginated(PageRequest.of(page, size, sort));
            
            List<Map<String, Object>> userList = userPage.getContent().stream()
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", userList);
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                return new ResponseEntity<>(convertUserToDetailMap(userOpt.get()), HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "用户不存在"));
            }
        } catch (Exception e) {
            log.error("获取用户详情失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 创建新用户
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData, HttpServletRequest request) {
        try {
            String username = (String) userData.get("username");
            String password = (String) userData.get("password");
            String email = (String) userData.get("email");
            String phone = (String) userData.get("phone");
            String fullName = (String) userData.get("fullName");
            Long roleId = userData.get("roleId") != null ? ((Number) userData.get("roleId")).longValue() : null;
            
            if (username == null || password == null) {
                return new ResponseEntity<>("用户名和密码不能为空", HttpStatus.BAD_REQUEST);
            }
            
            User user = userService.createUser(username, password, email, phone, fullName, roleId);
            
            userService.logOperation(getCurrentUsername(request), "CREATE_USER", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Created user: " + username);
            
            return new ResponseEntity<>(convertUserToMap(user), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            userService.logOperation(getCurrentUsername(request), "CREATE_USER", "user", 
                getClientIP(request), request.getHeader("User-Agent"), false, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, 
                                     @RequestBody Map<String, Object> userData,
                                     HttpServletRequest request) {
        try {
            User user = userService.updateUserByAdmin(userId, userData);
            
            userService.logOperation(getCurrentUsername(request), "UPDATE_USER", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Updated user ID: " + userId);
            
            return new ResponseEntity<>(convertUserToMap(user), HttpStatus.OK);
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage());
            userService.logOperation(getCurrentUsername(request), "UPDATE_USER", "user", 
                getClientIP(request), request.getHeader("User-Agent"), false, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        try {
            userService.deleteUser(userId);
            
            userService.logOperation(getCurrentUsername(request), "DELETE_USER", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Deleted user ID: " + userId);
            
            return new ResponseEntity<>("用户删除成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            userService.logOperation(getCurrentUsername(request), "DELETE_USER", "user", 
                getClientIP(request), request.getHeader("User-Agent"), false, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 启用/禁用用户
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, 
                                            @RequestParam boolean enabled,
                                            HttpServletRequest request) {
        try {
            User user = userService.toggleUserStatus(userId, enabled);
            
            String action = enabled ? "ENABLE_USER" : "DISABLE_USER";
            userService.logOperation(getCurrentUsername(request), action, "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, 
                (enabled ? "Enabled" : "Disabled") + " user ID: " + userId);
            
            return new ResponseEntity<>(convertUserToMap(user), HttpStatus.OK);
        } catch (Exception e) {
            log.error("切换用户状态失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long userId,
                                              @RequestBody Map<String, String> passwordData,
                                              HttpServletRequest request) {
        try {
            String newPassword = passwordData.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return new ResponseEntity<>("新密码不能为空", HttpStatus.BAD_REQUEST);
            }
            
            userService.resetUserPassword(userId, newPassword);
            
            userService.logOperation(getCurrentUsername(request), "RESET_PASSWORD", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Reset password for user ID: " + userId);
            
            return new ResponseEntity<>("密码重置成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/users/{userId}/assign-role")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId,
                                              @RequestBody Map<String, Long> roleData,
                                              HttpServletRequest request) {
        try {
            Long roleId = roleData.get("roleId");
            if (roleId == null) {
                return new ResponseEntity<>("角色ID不能为空", HttpStatus.BAD_REQUEST);
            }
            
            rbacService.assignRoleToUser(userId, roleId);
            
            userService.logOperation(getCurrentUsername(request), "ASSIGN_ROLE", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, 
                "Assigned role ID " + roleId + " to user ID: " + userId);
            
            return new ResponseEntity<>("角色分配成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("分配角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 获取所有角色列表
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<UserRole> roles = rbacService.getAllRoles();
            List<Map<String, Object>> roleList = roles.stream()
                .map(this::convertRoleToMap)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(roleList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取角色列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 创建新角色
     */
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> roleData,
                                       HttpServletRequest request) {
        try {
            String roleName = roleData.get("roleName");
            String description = roleData.get("description");
            
            if (roleName == null || roleName.isEmpty()) {
                return new ResponseEntity<>("角色名称不能为空", HttpStatus.BAD_REQUEST);
            }
            
            UserRole role = rbacService.createRole(roleName, description);
            
            userService.logOperation(getCurrentUsername(request), "CREATE_ROLE", "role", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Created role: " + roleName);
            
            return new ResponseEntity<>(convertRoleToMap(role), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("创建角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<?> updateRole(@PathVariable Long roleId,
                                       @RequestBody Map<String, String> roleData,
                                       HttpServletRequest request) {
        try {
            String roleName = roleData.get("roleName");
            String description = roleData.get("description");
            
            UserRole role = rbacService.updateRole(roleId, roleName, description);
            
            userService.logOperation(getCurrentUsername(request), "UPDATE_ROLE", "role", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Updated role ID: " + roleId);
            
            return new ResponseEntity<>(convertRoleToMap(role), HttpStatus.OK);
        } catch (Exception e) {
            log.error("更新角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable Long roleId, HttpServletRequest request) {
        try {
            rbacService.deleteRole(roleId);
            
            userService.logOperation(getCurrentUsername(request), "DELETE_ROLE", "role", 
                getClientIP(request), request.getHeader("User-Agent"), true, "Deleted role ID: " + roleId);
            
            return new ResponseEntity<>("角色删除成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("删除角色失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 获取操作日志（分页）
     */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getOperationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username) {
        try {
            Page<OperationLog> logPage = operationLogRepository.findAll(
                PageRequest.of(page, size, Sort.by("timestamp").descending())
            );
            
            List<Map<String, Object>> logs = logPage.getContent().stream()
                .map(this::convertLogToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("currentPage", logPage.getNumber());
            response.put("totalItems", logPage.getTotalElements());
            response.put("totalPages", logPage.getTotalPages());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取操作日志失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取用户操作日志
     */
    @GetMapping("/logs/user/{username}")
    public ResponseEntity<?> getUserLogs(@PathVariable String username) {
        try {
            List<OperationLog> logs = operationLogRepository.findByUsernameOrderByTimestampDesc(username);
            List<Map<String, Object>> logList = logs.stream()
                .map(this::convertLogToMap)
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(logList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取用户日志失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "admin-service");
        health.put("version", "1.0.0");
        
        return new ResponseEntity<>(health, HttpStatus.OK);
    }
    
    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("systemName", "智能家居管理系统");
        config.put("version", "1.0.0");
        config.put("maxUsers", 1000);
        config.put("sessionTimeout", 3600);
        config.put("passwordMinLength", 6);
        config.put("allowRegistration", false);
        
        return new ResponseEntity<>(config, HttpStatus.OK);
    }
    
    /**
     * 辅助方法：转换用户为Map
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("fullName", user.getFullName());
        map.put("enabled", user.isEnabled());
        map.put("createdAt", user.getCreatedAt());
        map.put("lastLogin", user.getLastLogin());
        if (user.getRole() != null) {
            map.put("roleId", user.getRole().getId());
            map.put("roleName", user.getRole().getRoleName());
        }
        return map;
    }
    
    /**
     * 辅助方法：转换用户为详细Map
     */
    private Map<String, Object> convertUserToDetailMap(User user) {
        Map<String, Object> map = convertUserToMap(user);
        map.put("permissions", rbacService.getUserPermissions(user.getUsername()));
        if (user.getRole() != null) {
            map.put("rolePermissions", user.getRole().getPermissions());
        }
        return map;
    }
    
    /**
     * 辅助方法：转换角色为Map
     */
    private Map<String, Object> convertRoleToMap(UserRole role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("roleName", role.getRoleName());
        map.put("description", role.getDescription());
        map.put("permissions", role.getPermissions());
        map.put("permissionCount", role.getPermissions() != null ? role.getPermissions().size() : 0);
        return map;
    }
    
    /**
     * 辅助方法：转换日志为Map
     */
    private Map<String, Object> convertLogToMap(OperationLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("username", log.getUsername());
        map.put("operation", log.getOperation());
        map.put("resource", log.getResource());
        map.put("ipAddress", log.getIpAddress());
        map.put("userAgent", log.getUserAgent());
        map.put("success", log.isSuccess());
        map.put("details", log.getRequestData() != null ? log.getRequestData() : log.getErrorMessage());
        map.put("timestamp", log.getTimestamp());
        return map;
    }
    
    /**
     * 辅助方法：获取当前用户名
     */
    private String getCurrentUsername(HttpServletRequest request) {
        return request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "admin";
    }
    
    /**
     * 辅助方法：获取客户端IP
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
