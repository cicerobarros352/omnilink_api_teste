package com.cicerobarros.omnilink_api_teste.repository;

import com.cicerobarros.omnilink_api_teste.entity.Vehicle;
import com.cicerobarros.omnilink_api_teste.entity.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByPlate(String plate);

    boolean existsByPlateAndIdNot(String plate, Long id);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByBrandContainingIgnoreCase(String brand, Pageable pageable);
}
