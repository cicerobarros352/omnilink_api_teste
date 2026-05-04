package com.cicerobarros.omnilink_api_teste.dto.request;

import com.cicerobarros.omnilink_api_teste.entity.enums.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    private String brand;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String model;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 2027, message = "Ano não pode ser superior a 2027")
    private Integer year;

    /**
     * Suporta o formato antigo (ABC1234) e o padrão Mercosul (ABC1D23).
     * Regex: 3 letras maiúsculas + 1 dígito + 1 letra ou dígito + 2 dígitos
     */
    @NotBlank(message = "Placa é obrigatória")
    @Pattern(
        regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$",
        message = "Placa inválida. Use o formato antigo (ABC1234) ou Mercosul (ABC1D23)"
    )
    private String plate;

    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    private String color;

    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser positivo")
    private BigDecimal price;

    private VehicleStatus status;
}
