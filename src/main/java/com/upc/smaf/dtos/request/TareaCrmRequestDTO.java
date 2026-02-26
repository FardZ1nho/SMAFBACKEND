package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.TipoTareaCrm;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TareaCrmRequestDTO {
    private Integer cotizacionId;    private String titulo;
    private String descripcion;
    private LocalDateTime fechaLimite;
    private TipoTareaCrm tipo;
}