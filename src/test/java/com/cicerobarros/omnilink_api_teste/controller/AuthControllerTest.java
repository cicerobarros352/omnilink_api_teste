package com.cicerobarros.omnilink_api_teste.controller;

import com.cicerobarros.omnilink_api_teste.dto.request.LoginRequest;
import com.cicerobarros.omnilink_api_teste.dto.request.RegisterRequest;
import com.cicerobarros.omnilink_api_teste.dto.response.AuthResponse;
import com.cicerobarros.omnilink_api_teste.config.SecurityConfig;
import com.cicerobarros.omnilink_api_teste.security.JwtTokenProvider;
import com.cicerobarros.omnilink_api_teste.security.UserDetailsServiceImpl;
import com.cicerobarros.omnilink_api_teste.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController - Testes de Integração Web")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private final AuthResponse tokenResponse = AuthResponse.builder()
            .token("eyJhbGciOiJIUzI1NiJ9.test")
            .tokenType("Bearer")
            .username("testuser")
            .email("test@email.com")
            .role("USER")
            .build();

    @Test
    @DisplayName("POST /api/auth/register - deve registrar usuário e retornar 201")
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "senha123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register - deve retornar 400 para dados inválidos")
    void shouldReturn400ForInvalidRegister() throws Exception {
        RegisterRequest request = new RegisterRequest("ab", "email-invalido", "123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/login - deve autenticar e retornar token")
    void shouldLoginUser() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "senha123");
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/login - deve retornar 401 para credenciais inválidas")
    void shouldReturn401ForInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "senhaerrada");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
