package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ImportacionRequestDTO {
    // Datos Básicos
    private String estado;
    private String tipoTransporte;
    private LocalDate fechaEstimadaLlegada;

    // Aduanas (NUEVOS)
    private String numeroDua;
    private String trackingNumber;
    private String agenteAduanas;
    private String canal;

    // Costos (NUEVOS)
    private BigDecimal totalFleteInternacional;
    private BigDecimal totalSeguro;
    private BigDecimal totalGastosAduana;
    private BigDecimal totalGastosAlmacen;
    private BigDecimal totalTransporteLocal;
    private BigDecimal otrosGastosGlobales;
}