package com.smarthome.user.controller;

import com.smarthome.user.entity.User;
import com.smarthome.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping({"/api/user/users", "/api/user"})
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String username = getCurrentUsername();
            log.debug("获取用户信息: {}", username);
            Optional<User> user = userService.getUserInfo(username);
            
            if (user.isPresent()) {
                User u = user.get();
                Map<String, Object> profile = new java.util.HashMap<>();
                profile.put("id", u.getId());
                profile.put("username", u.getUsername());
                profile.put("email", u.getEmail());
                profile.put("fullName", u.getFullName());
                profile.put("phone", u.getPhone());
                profile.put("role", u.getRole() != null ? u.getRole().getRoleName() : null);
                profile.put("enabled", u.isEnabled());
                profile.put("createdAt", u.getCreatedAt());
                profile.put("lastLogin", u.getLastLogin());
                
                userService.logOperation(username, "GET_PROFILE", "user", 
                    getClientIP(request), request.getHeader("User-Agent"), true, null);
                
                return new ResponseEntity<>(profile, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("用户不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(@RequestBody User userUpdate, 
                                          HttpServletRequest request) {
        try {
            String username = getCurrentUsername();
            User updatedUser = userService.updateUserInfo(username, userUpdate);
            
            // 记录操作日志
            userService.logOperation(username, "UPDATE_PROFILE", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, null);
            
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordRequest,
                                           HttpServletRequest request) {
        try {
            String username = getCurrentUsername();
            String oldPassword = passwordRequest.get("oldPassword");
            String newPassword = passwordRequest.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return new ResponseEntity<>("旧密码和新密码不能为空", HttpStatus.BAD_REQUEST);
            }
            
            userService.changePassword(username, oldPassword, newPassword);
            
            // 记录操作日志
            userService.logOperation(username, "CHANGE_PASSWORD", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, null);
            
            return new ResponseEntity<>("密码修改成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 获取用户设置
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSettings(HttpServletRequest request) {
        try {
            String username = getCurrentUsername();
            // 这里应该获取用户设置，简化实现
            Map<String, Object> settings = Map.of(
                "theme", "light",
                "language", "zh-CN",
                "notifications", true
            );
            
            // 记录操作日志
            userService.logOperation(username, "GET_SETTINGS", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, null);
            
            return new ResponseEntity<>(settings, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取用户设置失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 更新用户设置
     */
    @PutMapping("/settings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> settingsUpdate,
                                           HttpServletRequest request) {
        try {
            String username = getCurrentUsername();
            
            // 这里应该更新用户设置，简化实现
            
            // 记录操作日志
            userService.logOperation(username, "UPDATE_SETTINGS", "user", 
                getClientIP(request), request.getHeader("User-Agent"), true, null);
            
            return new ResponseEntity<>("设置更新成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("更新用户设置失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return new ResponseEntity<>(Map.of("status", "UP", "service", "user-service"), HttpStatus.OK);
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        throw new RuntimeException("无法获取当前用户");
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}