package com.upc.smaf.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}