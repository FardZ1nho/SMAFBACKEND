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

    // ✅ Incluimos la compra completa para mostrar Proveedor, Fecha Compra, etc.
    private CompraResponseDTO compra;

    // --- Seguimiento ---
    private EstadoImportacion estado;
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

    // --- Costos ---
    private BigDecimal costoFlete;
    private BigDecimal costoSeguro;
    private BigDecimal impuestosAduanas;
    private BigDecimal gastosOperativos;
    private BigDecimal costoTransporteLocal; // Nuevo

    private LocalDateTime fechaCreacion;
}