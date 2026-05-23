package com.smarthome.user.controller;

import com.smarthome.user.entity.User;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        UserRole role = new UserRole();
        role.setId(1L);
        role.setRoleName("ROLE_USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhone("13800138000");
        testUser.setRole(role);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetProfile() throws Exception {
        when(userService.getUserInfo("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetProfile_UserNotFound() throws Exception {
        when(userService.getUserInfo("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateProfile() throws Exception {
        when(userService.updateUserInfo(anyString(), any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "updated@example.com",
                                    "fullName": "Updated Name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testChangePassword_Success() throws Exception {
        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "oldPassword": "OldPass1",
                                    "newPassword": "NewPass1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("密码修改成功"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testChangePassword_MissingFields() throws Exception {
        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "oldPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetSettings() throws Exception {
        mockMvc.perform(get("/api/user/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("light"))
                .andExpect(jsonPath("$.language").value("zh-CN"))
                .andExpect(jsonPath("$.notifications").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateSettings() throws Exception {
        mockMvc.perform(put("/api/user/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "theme": "dark",
                                    "language": "en-US"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("设置更新成功"));
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/user/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("user-service"));
    }
}