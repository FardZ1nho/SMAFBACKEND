package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.PrioridadTarea;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TareaRequestDTO {
    @NotNull
    private String titulo;
    private String descripcion;

    @NotNull
    private LocalDateTime fechaLimite;

    @NotNull
    private PrioridadTarea prioridad; // ALTA, MEDIA, BAJA

    @NotNull
    private Long usuarioAsignadoId; // A quién se la mandamos (ID de la tabla users)

    // ❌ ELIMINADO: private Long creadorId; -> Ya no se pide, se saca del token.
}