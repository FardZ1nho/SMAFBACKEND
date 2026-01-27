package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DashboardAlertaDTO {
    private Integer idImportacion;
    private String codigoImportacion; // "IMP-2026-01" o Serie-Numero
    private String proveedor;
    private LocalDate fechaEta;
    private Long diasRestantes; // Campo calculado
    private String estado;      // TRANSITO / ADUANAS
}