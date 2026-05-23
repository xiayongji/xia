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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
    }

    @Test
    void testRegister_Success() throws Exception {
        when(userService.registerUser(any(User.class), any(UserRole.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "StrongPass1",
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).registerUser(any(User.class), any(UserRole.class));
    }

    @Test
    void testRegister_MissingFields() throws Exception {
        mockMvc.perform(post("/api/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": ""
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void testLogin_Success() throws Exception {
        Map<String, Object> loginResult = Map.of(
                "accessToken", "jwt-token-123",
                "token", "jwt-token-123",
                "refreshToken", "refresh-token-456",
                "tokenType", "Bearer",
                "username", "testuser",
                "role", "ROLE_USER"
        );

        when(userService.login("testuser", "StrongPass1")).thenReturn(loginResult);

        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "StrongPass1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void testLogin_MissingCredentials() throws Exception {
        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        Map<String, Object> refreshResult = Map.of(
                "accessToken", "new-jwt-token",
                "refreshToken", "new-refresh-token",
                "tokenType", "Bearer"
        );

        when(userService.refreshToken("refresh-token-456")).thenReturn(refreshResult);

        mockMvc.perform(post("/api/user/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "refresh-token-456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testRefreshToken_MissingToken() throws Exception {
        mockMvc.perform(post("/api/user/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}