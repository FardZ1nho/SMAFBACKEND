package com.upc.smaf.dtos.request;

import lombok.Data;

@Data
public class AjusteRequestDTO {
    private Integer productoId;
    private Long almacenId;
    private Integer cantidad; // Puede ser negativo o positivo
    private String motivo;
    private String usuarioResponsable; // ✅ Campo para auditoría
}