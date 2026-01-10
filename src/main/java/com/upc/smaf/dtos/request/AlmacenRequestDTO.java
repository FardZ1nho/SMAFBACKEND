package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede tener más de 20 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @Size(max = 200, message = "La dirección no puede tener más de 200 caracteres")
    private String direccion;

    private Boolean activo;
}