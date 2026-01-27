package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.EstadoImportacion;
import com.upc.smaf.entities.Incoterm;
import com.upc.smaf.entities.TipoTransporte;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ImportacionResponseDTO {
    private Integer id;
    private CompraResponseDTO compra;

    // --- Seguimiento ---
    private EstadoImportacion estado;
    private String numeroDua;
    private String trackingNumber;

    // --- Fechas Críticas ---
    private LocalDateTime fechaCutOffDocumental;
    private LocalDate fechaCutOffFisico;
    private LocalDate fechaSalidaEstimada; // ETD
    private LocalDate fechaEstimadaLlegada; // ETA
    private LocalDate fechaLlegadaReal; // ATA

    // --- Cierre Aduanas ---
    private LocalDateTime fechaLevanteAutorizado;
    private LocalDate fechaNacionalizacion;

    // --- Logística ---
    private String paisOrigen;
    private String puertoEmbarque;
    private String puertoLlegada;
    private Incoterm incoterm;
    private TipoTransporte tipoTransporte;
    private String navieraAerolinea;
    private String numeroViaje;
    private String numeroContenedor;

    // --- Penalidades ---
    private Integer diasLibres;
    private LocalDate fechaLimiteDevolucion;

    // --- Costos ---
    private BigDecimal costoFlete;
    private BigDecimal costoSeguro;
    private BigDecimal impuestosAduanas;
    private BigDecimal gastosOperativos;
    private BigDecimal costoTransporteLocal;

    private LocalDateTime fechaCreacion;
}