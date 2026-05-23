package com.smarthome.user.service;

import com.smarthome.user.entity.User;
import com.smarthome.user.entity.UserPermission;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.repository.PermissionRepository;
import com.smarthome.user.repository.RoleRepository;
import com.smarthome.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RbacService rbacService;

    private UserRole testRole;
    private UserPermission testPermission;
    private User testUser;

    @BeforeEach
    void setUp() {
        testRole = buildRole(1L, "ROLE_ADMIN", "Admin role");
        testPermission = buildPermission(1L, "USER_READ", "Read user", "user", "read");
        testUser = buildUser(1L, "testuser", "test@example.com", testRole);
    }

    @Test
    void testCreateRole_Success() {
        when(roleRepository.existsByRoleName("ROLE_MANAGER")).thenReturn(false);
        when(roleRepository.save(any(UserRole.class))).thenAnswer(invocation -> {
            UserRole role = invocation.getArgument(0);
            role.setId(2L);
            return role;
        });

        UserRole result = rbacService.createRole("ROLE_MANAGER", "Manager role");

        assertNotNull(result);
        assertEquals("ROLE_MANAGER", result.getRoleName());
        assertEquals("Manager role", result.getDescription());
        verify(roleRepository).save(any(UserRole.class));
    }

    @Test
    void testCreateRole_Duplicate() {
        when(roleRepository.existsByRoleName("ROLE_ADMIN")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rbacService.createRole("ROLE_ADMIN", "Admin role"));
        assertTrue(exception.getMessage().contains("角色已存在"));
        verify(roleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void testGetAllRoles() {
        List<UserRole> roles = List.of(testRole,
                buildRole(2L, "ROLE_USER", "User role"));

        when(roleRepository.findAll()).thenReturn(roles);

        List<UserRole> result = rbacService.getAllRoles();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(UserRole.class))).thenReturn(testRole);

        UserRole result = rbacService.updateRole(1L, "ROLE_SUPER_ADMIN", "Super admin role");

        assertNotNull(result);
        assertEquals("ROLE_SUPER_ADMIN", testRole.getRoleName());
        assertEquals("Super admin role", testRole.getDescription());
        verify(roleRepository).save(testRole);
    }

    @Test
    void testDeleteRole() {
        doNothing().when(roleRepository).deleteById(1L);

        assertDoesNotThrow(() -> rbacService.deleteRole(1L));

        verify(roleRepository).deleteById(1L);
    }

    @Test
    void testCreatePermission_Success() {
        when(permissionRepository.existsByPermissionName("DEVICE_READ")).thenReturn(false);
        when(permissionRepository.save(any(UserPermission.class))).thenAnswer(invocation -> {
            UserPermission perm = invocation.getArgument(0);
            perm.setId(2L);
            return perm;
        });

        UserPermission result = rbacService.createPermission("DEVICE_READ", "Read device",
                "device", "read");

        assertNotNull(result);
        assertEquals("DEVICE_READ", result.getPermissionName());
        assertEquals("Read device", result.getDescription());
        assertEquals("device", result.getResource());
        assertEquals("read", result.getAction());
        verify(permissionRepository).save(any(UserPermission.class));
    }

    @Test
    void testAddPermissionToRole() {
        UserRole role = buildRole(1L, "ROLE_ADMIN", "Admin role");
        role.setPermissions(new ArrayList<>());
        UserPermission perm = buildPermission(2L, "DEVICE_WRITE", "Write device", "device", "write");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(2L)).thenReturn(Optional.of(perm));
        when(roleRepository.save(any(UserRole.class))).thenReturn(role);

        assertDoesNotThrow(() -> rbacService.addPermissionToRole(1L, 2L));

        assertTrue(role.getPermissions().contains(perm));
        verify(roleRepository).save(role);
    }

    @Test
    void testRemovePermissionFromRole() {
        UserRole role = buildRole(1L, "ROLE_ADMIN", "Admin role");
        UserPermission perm = buildPermission(2L, "DEVICE_WRITE", "Write device", "device", "write");
        List<UserPermission> permissions = new ArrayList<>();
        permissions.add(perm);
        role.setPermissions(permissions);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(2L)).thenReturn(Optional.of(perm));
        when(roleRepository.save(any(UserRole.class))).thenReturn(role);

        assertDoesNotThrow(() -> rbacService.removePermissionFromRole(1L, 2L));

        assertFalse(role.getPermissions().contains(perm));
        verify(roleRepository).save(role);
    }

    @Test
    void testAssignRoleToUser() {
        UserRole newRole = buildRole(2L, "ROLE_MANAGER", "Manager role");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> rbacService.assignRoleToUser(1L, 2L));

        assertEquals(newRole, testUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetUserPermissions() {
        UserPermission perm1 = buildPermission(1L, "USER_READ", "Read user", "user", "read");
        UserPermission perm2 = buildPermission(2L, "DEVICE_READ", "Read device", "device", "read");
        List<UserPermission> permissions = List.of(perm1, perm2);
        testRole.setPermissions(permissions);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Set<String> result = rbacService.getUserPermissions("testuser");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("user:read"));
        assertTrue(result.contains("device:read"));
    }

    @Test
    void testHasPermission() {
        UserPermission perm = buildPermission(1L, "USER_READ", "Read user", "user", "read");
        List<UserPermission> permissions = List.of(perm);
        testRole.setPermissions(permissions);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertTrue(rbacService.hasPermission("testuser", "user", "read"));
        assertFalse(rbacService.hasPermission("testuser", "user", "write"));
    }

    @Test
    void testHasRole() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertTrue(rbacService.hasRole("testuser", "ROLE_ADMIN"));
        assertFalse(rbacService.hasRole("testuser", "ROLE_USER"));
    }

    private UserRole buildRole(Long id, String roleName, String description) {
        UserRole role = new UserRole();
        role.setId(id);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setPermissions(List.of());
        return role;
    }

    private UserPermission buildPermission(Long id, String permissionName, String description,
                                           String resource, String action) {
        UserPermission permission = new UserPermission();
        permission.setId(id);
        permission.setPermissionName(permissionName);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        return permission;
    }

    private User buildUser(Long id, String username, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}