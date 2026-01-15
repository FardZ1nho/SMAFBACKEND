package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorRequestDTO {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 caracteres")
    @Pattern(regexp = "\\d+", message = "El RUC debe contener solo números")
    private String ruc;

    @Size(max = 100, message = "El contacto no puede exceder 100 caracteres")
    private String contacto;

    // CORREGIDO: Ahora acepta espacios (ej: 999 444 555)
    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    @Pattern(regexp = "^[0-9+ ]+$", message = "El teléfono solo puede contener números, espacios y el signo +")
    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 250, message = "La dirección no puede exceder 250 caracteres")
    private String direccion;

    private Boolean activo = true;
}