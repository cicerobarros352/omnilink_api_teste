package com.cicerobarros.omnilink_api_teste.service;

import com.cicerobarros.omnilink_api_teste.config.CacheConfig;
import com.cicerobarros.omnilink_api_teste.dto.request.CustomerRequest;
import com.cicerobarros.omnilink_api_teste.dto.response.CustomerResponse;
import com.cicerobarros.omnilink_api_teste.entity.Customer;
import com.cicerobarros.omnilink_api_teste.exception.BusinessException;
import com.cicerobarros.omnilink_api_teste.exception.ResourceNotFoundException;
import com.cicerobarros.omnilink_api_teste.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    @CacheEvict(value = CacheConfig.CUSTOMERS_CACHE, allEntries = true)
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("Já existe um cliente com o CPF: " + request.getCpf());
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Já existe um cliente com o email: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .cpf(sanitizeCpf(request.getCpf()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        customer = customerRepository.save(customer);
        log.info("Cliente criado: id={}, cpf={}", customer.getId(), customer.getCpf());
        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CUSTOMERS_CACHE, key = "#id")
    public CustomerResponse findById(Long id) {
        log.debug("Buscando cliente por id: {}", id);
        return customerRepository.findById(id)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CUSTOMERS_CACHE,
               key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<CustomerResponse> findAll(Pageable pageable) {
        log.debug("Listando clientes - página {}", pageable.getPageNumber());
        return customerRepository.findAll(pageable).map(CustomerResponse::from);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CUSTOMERS_CACHE, allEntries = true)
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));

        if (customerRepository.existsByCpfAndIdNot(request.getCpf(), id)) {
            throw new BusinessException("Já existe outro cliente com o CPF: " + request.getCpf());
        }
        if (customerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException("Já existe outro cliente com o email: " + request.getEmail());
        }

        customer.setName(request.getName());
        customer.setCpf(sanitizeCpf(request.getCpf()));
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        customer = customerRepository.save(customer);
        log.info("Cliente atualizado: id={}", customer.getId());
        return CustomerResponse.from(customer);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CUSTOMERS_CACHE, allEntries = true)
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        customerRepository.delete(customer);
        log.info("Cliente removido: id={}", id);
    }

    private static String sanitizeCpf(String cpf) {
        return cpf.replaceAll("[^0-9]", "");
    }
}
