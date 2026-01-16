package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProveedorResponseDTO {

    private Integer id;
    private String nombre;
    private String ruc;
    private String pais; // Agregado
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}