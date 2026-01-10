package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorRequestDTO {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 11, message = "El RUC debe tener máximo 11 caracteres")
    private String ruc;

    @Size(max = 100, message = "El contacto no puede exceder 100 caracteres")
    private String contacto;

    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    private String telefono;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 250, message = "La dirección no puede exceder 250 caracteres")
    private String direccion;

    private Boolean activo = true;
}