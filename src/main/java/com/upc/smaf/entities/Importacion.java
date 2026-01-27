package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "importaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Importacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "compra_id", nullable = false, unique = true)
    private Compra compra;

    // --- ESTADO Y SEGUIMIENTO GENERAL ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoImportacion estado;

    @Column(name = "numero_dua", length = 50)
    private String numeroDua;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    // --- FECHAS CRÍTICAS (CUT-OFFS & TRAVESÍA) ---
    @Column(name = "fecha_cutoff_documental")
    private LocalDateTime fechaCutOffDocumental; // Deadline docs

    @Column(name = "fecha_cutoff_fisico")
    private LocalDate fechaCutOffFisico; // Deadline puerto (Stacking)

    @Column(name = "fecha_salida_estimada")
    private LocalDate fechaSalidaEstimada; // ETD

    @Column(name = "fecha_estimada_llegada")
    private LocalDate fechaEstimadaLlegada; // ETA

    @Column(name = "fecha_llegada_real")
    private LocalDate fechaLlegadaReal; // ATA (Llegada Real)

    // --- CIERRE EN DESTINO (ADUANAS) ---
    @Column(name = "fecha_levante_autorizado")
    private LocalDateTime fechaLevanteAutorizado; // Momento exacto de liberación

    @Column(name = "fecha_nacionalizacion")
    private LocalDate fechaNacionalizacion; // Fecha contable

    // --- LOGÍSTICA Y TRANSPORTE ---
    @Column(name = "pais_origen")
    private String paisOrigen;

    @Column(name = "puerto_embarque")
    private String puertoEmbarque;

    @Column(name = "puerto_llegada")
    private String puertoLlegada;

    @Enumerated(EnumType.STRING)
    @Column(name = "incoterm")
    private Incoterm incoterm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transporte")
    private TipoTransporte tipoTransporte;

    @Column(name = "naviera_aerolinea")
    private String navieraAerolinea;

    @Column(name = "numero_viaje")
    private String numeroViaje; // Vessel / Voyage

    @Column(name = "numero_contenedor")
    private String numeroContenedor;

    // --- PENALIDADES Y DEVOLUCIÓN ---
    @Column(name = "dias_libres")
    private Integer diasLibres; // Free days

    @Column(name = "fecha_limite_devolucion")
    private LocalDate fechaLimiteDevolucion; // Deadline devolución vacío

    // --- COSTOS ---
    @Column(name = "costo_flete", precision = 10, scale = 2)
    private BigDecimal costoFlete;

    @Column(name = "costo_seguro", precision = 10, scale = 2)
    private BigDecimal costoSeguro;

    @Column(name = "impuestos_aduanas", precision = 10, scale = 2)
    private BigDecimal impuestosAduanas;

    @Column(name = "gastos_operativos", precision = 10, scale = 2)
    private BigDecimal gastosOperativos;

    @Column(name = "costo_transporte_local", precision = 10, scale = 2)
    private BigDecimal costoTransporteLocal;

    // --- AUDITORÍA ---
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (costoFlete == null) costoFlete = BigDecimal.ZERO;
        if (costoSeguro == null) costoSeguro = BigDecimal.ZERO;
        if (impuestosAduanas == null) impuestosAduanas = BigDecimal.ZERO;
        if (gastosOperativos == null) gastosOperativos = BigDecimal.ZERO;
        if (costoTransporteLocal == null) costoTransporteLocal = BigDecimal.ZERO;
        if (estado == null) estado = EstadoImportacion.ORDENADO;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}