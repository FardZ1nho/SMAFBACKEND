package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteRequestDTO {

    @NotBlank(message = "El tipo de cliente es obligatorio")
    private String tipoCliente; // PERSONA, EMPRESA

    @NotBlank(message = "El nombre es obligatorio")
    private String nombreCompleto;

    private String tipoDocumento; // DNI, RUC, PASAPORTE, CARNET_EXTRANJERIA
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