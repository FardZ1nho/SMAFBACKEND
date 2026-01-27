package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TareaResponseDTO {
    private Integer id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaLimite;
    private String prioridad;
    private String estado;

    // ✅ CORREGIDO: IDs como Long
    private Long usuarioAsignadoId;
    private String usernameAsignado; // Usamos username porque Users no tiene 'nombre'

    private String usernameCreador;
    private LocalDateTime fechaCreacion;

    private boolean vencida;
}