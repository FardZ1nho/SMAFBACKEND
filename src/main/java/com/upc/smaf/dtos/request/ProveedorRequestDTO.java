package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorRequestDTO {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200)
    private String nombre;

    @NotBlank(message = "El número de identificación (RUC/USCC) es obligatorio")
    @Size(min = 8, max = 20, message = "La identificación debe tener entre 8 y 20 caracteres")
    // Quitamos el Pattern de solo números para permitir letras (China USCC)
    private String ruc;

    @NotBlank(message = "El país es obligatorio")
    private String pais;

    @Size(max = 100)
    private String contacto;

    @Size(max = 15)
    @Pattern(regexp = "^[0-9+ ]+$", message = "Formato de teléfono inválido")
    private String telefono;

    @Email
    private String email;

    @Size(max = 250)
    private String direccion;

    private Boolean activo = true;
}