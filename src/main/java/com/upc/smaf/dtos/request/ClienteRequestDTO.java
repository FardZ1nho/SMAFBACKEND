package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern; // 👈 Importante
import lombok.Data;

@Data
public class ClienteRequestDTO {

    @NotBlank(message = "El tipo de cliente es obligatorio")
    private String tipoCliente; // PERSONA, EMPRESA

    // Nota: Para empresas el nombre a veces va null, validarlo condicionalmente es complejo en DTO simple.
    // Lo dejaremos opcional aquí y el servicio o el frontend se encargan, o usas grupos de validación.
    private String nombreCompleto;

    private String tipoDocumento; // DNI, RUC, PASAPORTE, CARNET_EXTRANJERIA

    // ✅ AQUÍ ESTÁ LA MAGIA:
    // Esta expresión acepta:
    // 1. \d{8}  -> Exactamente 8 dígitos (DNI)
    // 2. |      -> Ó
    // 3. (10|15|17|20)\d{9} -> Empieza con 10, 15, 17 o 20 seguido de 9 dígitos (RUC)
    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "^(\\d{8}|(10|15|17|20)\\d{9})$", message = "El documento debe ser DNI (8 dígitos) o RUC válido (empieza con 10, 15, 17 o 20)")
    private String numeroDocumento;

    // Contacto
    private String telefono;

    @Email(message = "Email inválido")
    private String email;

    // Dirección
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;

    // Para empresas
    private String razonSocial;
    private String nombreContacto;

    // Notas
    private String notas;
}