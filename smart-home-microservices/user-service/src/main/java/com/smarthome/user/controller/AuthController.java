package com.smarthome.user.controller;

import com.smarthome.user.entity.User;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, 
                                     @RequestParam(defaultValue = "USER") String roleName) {
        try {
            UserRole userRole = new UserRole();
            userRole.setRoleName(roleName);
            userRole.setDescription("Default user role");
            userRole.setPermissions(List.of());
            
            User registeredUser = userService.registerUser(user, userRole);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, 
                                  HttpServletRequest request) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            if (username == null || password == null) {
                return new ResponseEntity<>("用户名和密码不能为空", HttpStatus.BAD_REQUEST);
            }
            
            Map<String, Object> result = userService.login(username, password);
            
            // 记录登录日志
            userService.logOperation(username, "LOGIN", "auth", 
                getClientIP(request), request.getHeader("User-Agent"), true, null);
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshRequest) {
        try {
            String refreshToken = refreshRequest.get("refreshToken");
            
            if (refreshToken == null) {
                return new ResponseEntity<>("刷新令牌不能为空", HttpStatus.BAD_REQUEST);
            }
            
            Map<String, Object> result = userService.refreshToken(refreshToken);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String username = getCurrentUsername();
            
            // 使令牌失效
            // 这里应该实现令牌失效逻辑
            
            // 记录登出日志
            userService.logOperation(username, "LOGOUT", "auth", 
                getClientIP(request), request.getHeader("User-Agent"), true, null);
            
            return new ResponseEntity<>("登出成功", HttpStatus.OK);
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private String getCurrentUsername() {
        // 从安全上下文中获取当前用户名
        return "current_user"; // 简化实现
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}