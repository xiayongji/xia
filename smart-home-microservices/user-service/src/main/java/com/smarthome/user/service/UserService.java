package com.smarthome.user.service;

import com.smarthome.user.entity.*;
import com.smarthome.user.repository.OperationLogRepository;
import com.smarthome.user.repository.RoleRepository;
import com.smarthome.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OperationLogRepository operationLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * 用户注册
     */
    @Transactional
    public User registerUser(User user, UserRole defaultRole) {
        log.info("用户注册: {}", user.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }
        
        // 密码强度验证
        validatePasswordStrength(user.getPassword());
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存角色
        if (defaultRole != null) {
            UserRole savedRole = roleRepository.save(defaultRole);
            user.setRole(savedRole);
        }
        
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // 创建用户默认设置
        createDefaultUserSettings(savedUser);
        
        log.info("用户注册成功: {}", user.getUsername());
        return savedUser;
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        log.info("用户登录: {}", username);
        
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            log.error("用户不存在: {}", username);
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = userOptional.get();
        
        if (!user.isEnabled()) {
            log.error("用户已被禁用: {}", username);
            throw new RuntimeException("用户已被禁用");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("密码验证失败: {}", username);
            throw new RuntimeException("用户名或密码错误");
        }
        
        String jwt = jwtService.generateJwtToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", jwt);
        result.put("token", jwt);
        result.put("refreshToken", refreshToken);
        result.put("tokenType", "Bearer");
        result.put("username", username);
        result.put("role", user.getRole() != null ? user.getRole().getRoleName() : "ROLE_USER");
        
        log.info("用户登录成功: {}", username);
        return result;
    }
    
    /**
     * 刷新令牌
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtService.validateJwtToken(refreshToken)) {
            throw new RuntimeException("刷新令牌无效");
        }
        
        String username = jwtService.getUserNameFromJwtToken(refreshToken);
        
        String newJwt = jwtService.generateJwtToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newJwt);
        result.put("refreshToken", newRefreshToken);
        result.put("tokenType", "Bearer");
        
        log.info("令牌刷新成功: {}", username);
        return result;
    }
    
    /**
     * 获取用户信息
     */
    public Optional<User> getUserInfo(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUserInfo(String username, User userUpdate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        if (userUpdate.getEmail() != null && !userUpdate.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userUpdate.getEmail())) {
                throw new RuntimeException("邮箱已存在: " + userUpdate.getEmail());
            }
            user.setEmail(userUpdate.getEmail());
        }
        
        if (userUpdate.getPhone() != null) {
            user.setPhone(userUpdate.getPhone());
        }
        
        if (userUpdate.getFullName() != null) {
            user.setFullName(userUpdate.getFullName());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 验证新密码强度
        validatePasswordStrength(newPassword);
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("密码修改成功: {}", username);
    }
    
    /**
     * 密码强度验证
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new RuntimeException("密码长度至少8位");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("密码必须包含大写字母");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("密码必须包含小写字母");
        }
        
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("密码必须包含数字");
        }
    }
    
    /**
     * 创建默认用户设置
     */
    private void createDefaultUserSettings(User user) {
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setTheme("light");
        settings.setLanguage("zh-CN");
        settings.setTimezone("Asia/Shanghai");
        settings.setNotificationsEnabled(true);
        settings.setEmailNotifications(true);
        settings.setPushNotifications(true);
        
        // 这里应该保存到数据库，简化实现
        log.info("创建用户默认设置: {}", user.getUsername());
    }
    
    /**
     * 记录操作日志
     */
    @Transactional
    public void logOperation(String username, String operation, String resource, 
                           String ipAddress, String userAgent, boolean success, String errorMessage) {
        OperationLog logEntry = new OperationLog();
        logEntry.setUsername(username);
        logEntry.setOperation(operation);
        logEntry.setResource(resource);
        logEntry.setIpAddress(ipAddress);
        logEntry.setUserAgent(userAgent);
        logEntry.setSuccess(success);
        logEntry.setErrorMessage(errorMessage);
        
        operationLogRepository.save(logEntry);
        log.info("操作日志: {} - {} - {}", username, operation, resource);
    }
    
    /**
     * 获取用户操作日志
     */
    public List<OperationLog> getUserOperationLogs(String username) {
        return operationLogRepository.findByUsernameOrderByTimestampDesc(username);
    }
    
    /**
     * 获取时间范围内的操作日志
     */
    public List<OperationLog> getOperationLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.findByTimeRange(startTime, endTime);
    }
    
    /**
     * 获取操作统计
     */
    public List<Object[]> getOperationStatistics(LocalDateTime since) {
        return operationLogRepository.countByOperation(since);
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 创建用户（管理员）
     */
    @Transactional
    public User createUser(String username, String password, String email, String phone, String fullName, Long roleId) {
        log.info("管理员创建用户: {}", username);
        
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在: " + username);
        }
        
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在: " + email);
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 会在保存前加密
        user.setEmail(email);
        user.setPhone(phone);
        user.setFullName(fullName);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        
        if (roleId != null) {
            UserRole role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
            user.setRole(role);
        } else {
            // 默认分配ROLE_USER角色
            roleRepository.findByRoleName("ROLE_USER").ifPresent(user::setRole);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 更新用户（管理员）
     */
    @Transactional
    public User updateUserByAdmin(Long userId, Map<String, Object> userData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        if (userData.containsKey("email")) {
            String email = (String) userData.get("email");
            if (email != null && !email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("邮箱已存在: " + email);
            }
            user.setEmail(email);
        }
        
        if (userData.containsKey("phone")) {
            user.setPhone((String) userData.get("phone"));
        }
        
        if (userData.containsKey("fullName")) {
            user.setFullName((String) userData.get("fullName"));
        }
        
        if (userData.containsKey("enabled")) {
            user.setEnabled((Boolean) userData.get("enabled"));
        }
        
        if (userData.containsKey("roleId")) {
            Long roleId = ((Number) userData.get("roleId")).longValue();
            UserRole role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
            user.setRole(role);
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("删除用户: {}", userId);
    }
    
    /**
     * 切换用户状态
     */
    @Transactional
    public User toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * 重置用户密码
     */
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 验证密码强度
        validatePasswordStrength(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("重置用户密码: {}", user.getUsername());
    }
    
    /**
     * 分页查询用户
     */
    public org.springframework.data.domain.Page<User> getUsersPaginated(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}