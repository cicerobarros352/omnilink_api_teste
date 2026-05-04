package com.cicerobarros.omnilink_api_teste.service;

import com.cicerobarros.omnilink_api_teste.dto.request.CustomerRequest;
import com.cicerobarros.omnilink_api_teste.dto.response.CustomerResponse;
import com.cicerobarros.omnilink_api_teste.entity.Customer;
import com.cicerobarros.omnilink_api_teste.exception.BusinessException;
import com.cicerobarros.omnilink_api_teste.exception.ResourceNotFoundException;
import com.cicerobarros.omnilink_api_teste.repository.CustomerRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService - Testes Unitários")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        // CPF válido para testes: 529.982.247-25
        validRequest = new CustomerRequest(
                "João Silva", "52998224725",
                "joao.silva@email.com", "(11) 99999-9999", "Rua Teste, 123"
        );

        savedCustomer = Customer.builder()
                .id(1L)
                .name("João Silva")
                .cpf("52998224725")
                .email("joao.silva@email.com")
                .phone("(11) 99999-9999")
                .address("Rua Teste, 123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void shouldCreateCustomer() {
        when(customerRepository.existsByCpf("52998224725")).thenReturn(false);
        when(customerRepository.existsByEmail("joao.silva@email.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.create(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("João Silva");
        assertThat(response.getCpf()).isEqualTo("52998224725");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao criar cliente com CPF duplicado")
    void shouldThrowWhenCpfAlreadyExists() {
        when(customerRepository.existsByCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao criar cliente com email duplicado")
    void shouldThrowWhenEmailAlreadyExists() {
        when(customerRepository.existsByCpf("52998224725")).thenReturn(false);
        when(customerRepository.existsByEmail("joao.silva@email.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void shouldFindCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));

        CustomerResponse response = customerService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cliente não existe")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Deve listar clientes paginados")
    void shouldFindAllCustomers() {
        Page<Customer> customerPage = new PageImpl<>(List.of(savedCustomer));
        when(customerRepository.findAll(any(PageRequest.class))).thenReturn(customerPage);

        Page<CustomerResponse> result = customerService.findAll(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void shouldUpdateCustomer() {
        CustomerRequest updateRequest = new CustomerRequest(
                "João da Silva", "52998224725",
                "joao.dasilva@email.com", "(11) 88888-8888", "Av. Atualizada, 456"
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByCpfAndIdNot("52998224725", 1L)).thenReturn(false);
        when(customerRepository.existsByEmailAndIdNot("joao.dasilva@email.com", 1L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.update(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void shouldDeleteCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertThatCode(() -> customerService.delete(1L)).doesNotThrowAnyException();
        verify(customerRepository, times(1)).deleteById(1L);
    }
}
