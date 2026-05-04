package com.cicerobarros.omnilink_api_teste.controller;

import com.cicerobarros.omnilink_api_teste.config.SecurityConfig;
import com.cicerobarros.omnilink_api_teste.dto.request.VehicleRequest;
import com.cicerobarros.omnilink_api_teste.dto.response.VehicleResponse;
import com.cicerobarros.omnilink_api_teste.entity.enums.VehicleStatus;
import com.cicerobarros.omnilink_api_teste.security.JwtTokenProvider;
import com.cicerobarros.omnilink_api_teste.security.UserDetailsServiceImpl;
import com.cicerobarros.omnilink_api_teste.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@Import(SecurityConfig.class)
@DisplayName("VehicleController - Testes de Integração Web")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private VehicleResponse vehicleResponse;
    private VehicleRequest validRequest;

    @BeforeEach
    void setUp() {
        vehicleResponse = VehicleResponse.builder()
                .id(1L)
                .brand("Honda")
                .model("Civic")
                .year(2023)
                .plate("XYZ9W87")
                .color("Preto")
                .price(new BigDecimal("110000.00"))
                .status(VehicleStatus.DISPONIVEL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new VehicleRequest(
                "Honda", "Civic", 2023, "XYZ9W87",
                "Preto", new BigDecimal("110000.00"), VehicleStatus.DISPONIVEL
        );
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/vehicles/{id} - deve retornar 200 com veículo")
    void shouldReturnVehicleById() throws Exception {
        when(vehicleService.findById(1L)).thenReturn(vehicleResponse);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Honda"))
                .andExpect(jsonPath("$.model").value("Civic"))
                .andExpect(jsonPath("$.plate").value("XYZ9W87"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/vehicles - deve retornar página de veículos")
    void shouldReturnVehiclePage() throws Exception {
        when(vehicleService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(vehicleResponse)));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].brand").value("Honda"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/vehicles - deve criar veículo e retornar 201")
    void shouldCreateVehicle() throws Exception {
        when(vehicleService.create(any(VehicleRequest.class))).thenReturn(vehicleResponse);

        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("XYZ9W87"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/vehicles - deve retornar 400 para placa inválida")
    void shouldReturn400ForInvalidPlate() throws Exception {
        VehicleRequest invalidRequest = new VehicleRequest(
                "Honda", "Civic", 2023, "INVALIDA",
                "Preto", new BigDecimal("110000.00"), null
        );

        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/vehicles/{id} - deve atualizar e retornar 200")
    void shouldUpdateVehicle() throws Exception {
        when(vehicleService.update(eq(1L), any(VehicleRequest.class))).thenReturn(vehicleResponse);

        mockMvc.perform(put("/api/vehicles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/vehicles/{id} - ADMIN deve retornar 204")
    void shouldDeleteVehicleAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/vehicles/{id} - USER deve retornar 403")
    void shouldReturn403WhenUserTriesToDelete() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - sem autenticação deve retornar 401")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isUnauthorized());
    }
}
