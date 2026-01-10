package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClienteResponseDTO {

    private Integer id;
    private String tipoCliente;
    private String nombreCompleto;
    private String tipoDocumento;
    private String numeroDocumento;

    // Contacto
    private String telefono;
    private String email;

    // Dirección completa
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;
    private String direccionCompleta; // Concatenada para mostrar

    // Para empresas
    private String razonSocial;
    private String nombreContacto;

    // Información adicional
    private String notas;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}