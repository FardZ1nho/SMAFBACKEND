package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProveedorResponseDTO {

    private Integer id;
    private String nombre;
    private String ruc;
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;

    // Fechas de auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion; // Nuevo campo útil
}