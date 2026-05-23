package com.smarthome.user.service;

import com.smarthome.user.entity.OperationLog;
import com.smarthome.user.entity.User;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.repository.OperationLogRepository;
import com.smarthome.user.repository.RoleRepository;
import com.smarthome.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OperationLogRepository operationLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRole testRole;

    @BeforeEach
    void setUp() {
        testUser = buildUser(1L, "testuser", "encodedPassword", "test@example.com",
                "13800138000", "Test User", true);
        testRole = buildRole(1L, "ROLE_USER", "Default user role");
    }

    @Test
    void testRegisterUser_Success() {
        User newUser = buildUser(null, "newuser", "StrongPass1", "new@example.com",
                null, null, null);
        UserRole defaultRole = buildRole(null, "ROLE_USER", "Default user role");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encodedStrongPass1");
        when(roleRepository.save(defaultRole)).thenReturn(defaultRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.registerUser(newUser, defaultRole);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("encodedStrongPass1", result.getPassword());
        assertTrue(result.isEnabled());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("StrongPass1");
        verify(roleRepository).save(defaultRole);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        User newUser = buildUser(null, "existinguser", "StrongPass1", "unique@example.com",
                null, null, null);

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser, null));
        assertTrue(exception.getMessage().contains("用户名已存在"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        User newUser = buildUser(null, "newuser", "StrongPass1", "existing@example.com",
                null, null, null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser, null));
        assertTrue(exception.getMessage().contains("邮箱已存在"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_WeakPassword_TooShort() {
        User newUser = buildUser(null, "newuser", "Ab1", "new@example.com",
                null, null, null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser, null));
        assertTrue(exception.getMessage().contains("密码长度至少8位"));
    }

    @Test
    void testRegisterUser_WeakPassword_NoUpperCase() {
        User newUser = buildUser(null, "newuser", "abcdefg1", "new@example.com",
                null, null, null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser, null));
        assertTrue(exception.getMessage().contains("密码必须包含大写字母"));
    }

    @Test
    void testRegisterUser_WeakPassword_NoLowerCase() {
        User newUser = buildUser(null, "newuser", "ABCDEFG1", "new@example.com",
                null, null, null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser, null));
        assertTrue(exception.getMessage().contains("密码必须包含小写字母"));
    }

    @Test
    void testRegisterUser_WeakPassword_NoDigit() {
        User newUser = buildUser(null, "newuser", "Abcdefgh", "new@example.com",
                null, null, null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser, null));
        assertTrue(exception.getMessage().contains("密码必须包含数字"));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("StrongPass1", "encodedPassword")).thenReturn(true);
        when(jwtService.generateJwtToken("testuser")).thenReturn("jwt-token-123");
        when(jwtService.generateRefreshToken("testuser")).thenReturn("refresh-token-456");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Map<String, Object> result = userService.login("testuser", "StrongPass1");

        assertNotNull(result);
        assertEquals("jwt-token-123", result.get("accessToken"));
        assertEquals("jwt-token-123", result.get("token"));
        assertEquals("refresh-token-456", result.get("refreshToken"));
        assertEquals("Bearer", result.get("tokenType"));
        assertEquals("testuser", result.get("username"));
        assertEquals("ROLE_USER", result.get("role"));
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.login("nonexistent", "password"));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLogin_DisabledUser() {
        User disabledUser = buildUser(1L, "disableduser", "encodedPassword", "disabled@example.com",
                null, null, false);

        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.login("disableduser", "password"));
        assertEquals("用户已被禁用", exception.getMessage());
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.login("testuser", "WrongPassword"));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testRefreshToken_Success() {
        when(jwtService.validateJwtToken("refresh-token-456")).thenReturn(true);
        when(jwtService.getUserNameFromJwtToken("refresh-token-456")).thenReturn("testuser");
        when(jwtService.generateJwtToken("testuser")).thenReturn("new-jwt-token");
        when(jwtService.generateRefreshToken("testuser")).thenReturn("new-refresh-token");

        Map<String, Object> result = userService.refreshToken("refresh-token-456");

        assertNotNull(result);
        assertEquals("new-jwt-token", result.get("accessToken"));
        assertEquals("new-refresh-token", result.get("refreshToken"));
        assertEquals("Bearer", result.get("tokenType"));
    }

    @Test
    void testRefreshToken_Invalid() {
        when(jwtService.validateJwtToken("invalid-token")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.refreshToken("invalid-token"));
        assertEquals("刷新令牌无效", exception.getMessage());
    }

    @Test
    void testGetUserInfo() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserInfo("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testUpdateUserInfo_Success() {
        User updateRequest = buildUser(null, null, null, "newemail@example.com",
                "13900139000", "Updated Name", null);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUserInfo("testuser", updateRequest);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserInfo_DuplicateEmail() {
        User updateRequest = buildUser(null, null, null, "existing@example.com",
                null, null, null);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUserInfo("testuser", updateRequest));
        assertTrue(exception.getMessage().contains("邮箱已存在"));
    }

    @Test
    void testChangePassword_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass1", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1")).thenReturn("encodedNewPass1");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.changePassword("testuser", "OldPass1", "NewPass1"));

        verify(passwordEncoder).encode("NewPass1");
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongOldPass1", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword("testuser", "WrongOldPass1", "NewPass1"));
        assertEquals("旧密码错误", exception.getMessage());
    }

    @Test
    void testChangePassword_WeakNewPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass1", "encodedPassword")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword("testuser", "OldPass1", "weak"));
        assertTrue(exception.getMessage().contains("密码长度至少8位"));
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(99L));
        assertTrue(exception.getMessage().contains("用户不存在"));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testToggleUserStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.toggleUserStatus(1L, false);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void testResetUserPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPass1")).thenReturn("encodedNewPass1");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.resetUserPassword(1L, "NewPass1"));

        verify(passwordEncoder).encode("NewPass1");
        verify(userRepository).save(testUser);
        assertEquals("encodedNewPass1", testUser.getPassword());
    }

    @Test
    void testCreateUser_Success() {
        UserRole role = buildRole(2L, "ROLE_ADMIN", "Admin role");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = userService.createUser("newuser", "StrongPass1", "new@example.com",
                "13800138000", "New User", 2L);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(role, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser("existinguser", "StrongPass1", "email@example.com",
                        null, null, null));
        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    void testUpdateUserByAdmin() {
        Map<String, Object> userData = Map.of(
                "email", "updated@example.com",
                "fullName", "Updated Name"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUserByAdmin(1L, userData);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(testUser, buildUser(2L, "user2", "pass", "user2@example.com",
                null, "User Two", true));

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testLogOperation() {
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> {
            OperationLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        assertDoesNotThrow(() -> userService.logOperation("testuser", "TEST_OP", "user",
                "127.0.0.1", "Mozilla/5.0", true, null));

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogRepository).save(captor.capture());
        OperationLog savedLog = captor.getValue();
        assertEquals("testuser", savedLog.getUsername());
        assertEquals("TEST_OP", savedLog.getOperation());
        assertEquals("user", savedLog.getResource());
        assertEquals("127.0.0.1", savedLog.getIpAddress());
        assertEquals("Mozilla/5.0", savedLog.getUserAgent());
        assertTrue(savedLog.isSuccess());
        assertNull(savedLog.getErrorMessage());
    }

    private User buildUser(Long id, String username, String password, String email,
                           String phone, String fullName, Boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setFullName(fullName);
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private UserRole buildRole(Long id, String roleName, String description) {
        UserRole role = new UserRole();
        role.setId(id);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setPermissions(List.of());
        return role;
    }
}