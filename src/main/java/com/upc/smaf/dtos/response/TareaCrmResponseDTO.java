package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.EstadoTareaCrm;
import com.upc.smaf.entities.TipoTareaCrm;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TareaCrmResponseDTO {
    private Long id;
    private Integer cotizacionId;    private String titulo;
    private String descripcion;
    private LocalDateTime fechaLimite;
    private EstadoTareaCrm estado;
    private TipoTareaCrm tipo;
}