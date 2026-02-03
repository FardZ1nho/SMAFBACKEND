package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DashboardAlertaDTO {
    private Integer idImportacion;
    private String codigoImportacion;

    // ✅ ESTOS CAMPOS FALTABAN:
    private LocalDate fechaLlegada;
    private String estado;
    private String proveedores;
}