package com.cicerobarros.omnilink_api_teste.service;

import com.cicerobarros.omnilink_api_teste.config.CacheConfig;
import com.cicerobarros.omnilink_api_teste.dto.request.VehicleRequest;
import com.cicerobarros.omnilink_api_teste.dto.response.VehicleResponse;
import com.cicerobarros.omnilink_api_teste.entity.Vehicle;
import com.cicerobarros.omnilink_api_teste.entity.enums.VehicleStatus;
import com.cicerobarros.omnilink_api_teste.exception.BusinessException;
import com.cicerobarros.omnilink_api_teste.exception.ResourceNotFoundException;
import com.cicerobarros.omnilink_api_teste.repository.VehicleRepository;
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
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    @CacheEvict(value = CacheConfig.VEHICLES_CACHE, allEntries = true)
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByPlate(request.getPlate())) {
            throw new BusinessException("Já existe um veículo com a placa: " + request.getPlate());
        }

        Vehicle vehicle = Vehicle.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .plate(request.getPlate().toUpperCase())
                .color(request.getColor())
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : VehicleStatus.DISPONIVEL)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("Veículo criado: id={}, placa={}", vehicle.getId(), vehicle.getPlate());
        return VehicleResponse.from(vehicle);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.VEHICLES_CACHE, key = "#id")
    public VehicleResponse findById(Long id) {
        log.debug("Buscando veículo por id: {}", id);
        return vehicleRepository.findById(id)
                .map(VehicleResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.VEHICLES_CACHE,
               key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<VehicleResponse> findAll(Pageable pageable) {
        log.debug("Listando veículos - página {}", pageable.getPageNumber());
        return vehicleRepository.findAll(pageable).map(VehicleResponse::from);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.VEHICLES_CACHE, allEntries = true)
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo", id));

        if (vehicleRepository.existsByPlateAndIdNot(request.getPlate(), id)) {
            throw new BusinessException("Já existe outro veículo com a placa: " + request.getPlate());
        }

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setPlate(request.getPlate().toUpperCase());
        vehicle.setColor(request.getColor());
        vehicle.setPrice(request.getPrice());
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }

        vehicle = vehicleRepository.save(vehicle);
        log.info("Veículo atualizado: id={}", vehicle.getId());
        return VehicleResponse.from(vehicle);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.VEHICLES_CACHE, allEntries = true)
    public void delete(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo", id));
        vehicleRepository.delete(vehicle);
        log.info("Veículo removido: id={}", id);
    }
}
