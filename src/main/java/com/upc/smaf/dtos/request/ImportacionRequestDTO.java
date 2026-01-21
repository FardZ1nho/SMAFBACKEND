package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.EstadoImportacion;
import com.upc.smaf.entities.Incoterm;
import com.upc.smaf.entities.TipoTransporte;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ImportacionRequestDTO {

    // --- Seguimiento ---
    private String numeroDua;
    private String trackingNumber;
    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaNacionalizacion;

    // --- Logística (NUEVOS) ---
    private String paisOrigen;
    private String puertoEmbarque;
    private String puertoLlegada;
    private Incoterm incoterm;
    private TipoTransporte tipoTransporte;
    private String navieraAerolinea;
    private String numeroContenedor;

    // --- Costos (Si vienen nulos, el servicio los pondrá en 0) ---
    private BigDecimal costoFlete;
    private BigDecimal costoSeguro;
    private BigDecimal impuestosAduanas;
    private BigDecimal gastosOperativos;
    private BigDecimal costoTransporteLocal; // Nuevo

    // --- Estado ---
    private EstadoImportacion estado;
}