package com.cicerobarros.omnilink_api_teste.service;

import com.cicerobarros.omnilink_api_teste.dto.request.VehicleRequest;
import com.cicerobarros.omnilink_api_teste.dto.response.VehicleResponse;
import com.cicerobarros.omnilink_api_teste.entity.Vehicle;
import com.cicerobarros.omnilink_api_teste.entity.enums.VehicleStatus;
import com.cicerobarros.omnilink_api_teste.exception.BusinessException;
import com.cicerobarros.omnilink_api_teste.exception.ResourceNotFoundException;
import com.cicerobarros.omnilink_api_teste.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService - Testes Unitários")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRequest validRequest;
    private Vehicle savedVehicle;

    @BeforeEach
    void setUp() {
        validRequest = new VehicleRequest(
                "Toyota", "Corolla", 2022, "ABC1D23",
                "Prata", new BigDecimal("95000.00"), VehicleStatus.DISPONIVEL
        );

        savedVehicle = Vehicle.builder()
                .id(1L)
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .plate("ABC1D23")
                .color("Prata")
                .price(new BigDecimal("95000.00"))
                .status(VehicleStatus.DISPONIVEL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    void shouldCreateVehicle() {
        when(vehicleRepository.existsByPlate("ABC1D23")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResponse response = vehicleService.create(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBrand()).isEqualTo("Toyota");
        assertThat(response.getPlate()).isEqualTo("ABC1D23");
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao criar veículo com placa duplicada")
    void shouldThrowWhenPlateAlreadyExists() {
        when(vehicleRepository.existsByPlate("ABC1D23")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ABC1D23");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar veículo por ID com sucesso")
    void shouldFindVehicleById() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(savedVehicle));

        VehicleResponse response = vehicleService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getModel()).isEqualTo("Corolla");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando veículo não existe")
    void shouldThrowWhenVehicleNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Deve listar veículos paginados")
    void shouldFindAllVehicles() {
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(savedVehicle));
        when(vehicleRepository.findAll(any(PageRequest.class))).thenReturn(vehiclePage);

        Page<VehicleResponse> result = vehicleService.findAll(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrand()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("Deve atualizar veículo com sucesso")
    void shouldUpdateVehicle() {
        VehicleRequest updateRequest = new VehicleRequest(
                "Toyota", "Corolla Cross", 2023, "ABC1D23",
                "Branco", new BigDecimal("120000.00"), VehicleStatus.RESERVADO
        );

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(savedVehicle));
        when(vehicleRepository.existsByPlateAndIdNot("ABC1D23", 1L)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResponse response = vehicleService.update(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Deve deletar veículo com sucesso")
    void shouldDeleteVehicle() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        assertThatCode(() -> vehicleService.delete(1L)).doesNotThrowAnyException();
        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao deletar veículo inexistente")
    void shouldThrowWhenDeletingNonExistentVehicle() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehicleRepository, never()).deleteById(any());
    }
}
