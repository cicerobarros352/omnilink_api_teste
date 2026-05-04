package com.cicerobarros.omnilink_api_teste.dto.request;

import com.cicerobarros.omnilink_api_teste.validation.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String name;

    @NotBlank(message = "CPF é obrigatório")
    @ValidCpf
    private String cpf;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 100)
    private String email;

    @Pattern(
        regexp = "^(\\+?\\d{1,3}[\\s-]?)?(\\(?\\d{2}\\)?[\\s-]?)?(\\d{4,5}[\\s-]?\\d{4})$",
        message = "Telefone inválido"
    )
    private String phone;

    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String address;
}
