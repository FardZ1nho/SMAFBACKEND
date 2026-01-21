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

    // ✅ RELACIÓN CLAVE: Esta importación pertenece a UNA compra específica
    @OneToOne
    @JoinColumn(name = "compra_id", nullable = false, unique = true)
    private Compra compra;

    // --- ESTADO Y SEGUIMIENTO ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoImportacion estado;

    @Column(name = "numero_dua", length = 50)
    private String numeroDua; // Declaración Única de Aduanas / DAM

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber; // BL o Guía Aérea

    @Column(name = "fecha_estimada_llegada")
    private LocalDate fechaEstimadaLlegada;

    @Column(name = "fecha_nacionalizacion")
    private LocalDate fechaNacionalizacion;

    // --- LOGÍSTICA Y TRANSPORTE (NUEVOS) ---
    @Column(name = "pais_origen")
    private String paisOrigen;

    @Column(name = "puerto_embarque")
    private String puertoEmbarque;

    @Column(name = "puerto_llegada")
    private String puertoLlegada;

    @Enumerated(EnumType.STRING)
    @Column(name = "incoterm")
    private Incoterm incoterm; // FOB, CIF, EXW...

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transporte")
    private TipoTransporte tipoTransporte; // MARITIMO, AEREO...

    @Column(name = "naviera_aerolinea")
    private String navieraAerolinea;

    @Column(name = "numero_contenedor")
    private String numeroContenedor;

    // --- COSTOS INTERNACIONALES ---
    @Column(name = "costo_flete", precision = 10, scale = 2)
    private BigDecimal costoFlete;

    @Column(name = "costo_seguro", precision = 10, scale = 2)
    private BigDecimal costoSeguro;

    // --- COSTOS NACIONALES ---
    @Column(name = "impuestos_aduanas", precision = 10, scale = 2)
    private BigDecimal impuestosAduanas; // Ad Valorem + IGV Importación

    @Column(name = "gastos_operativos", precision = 10, scale = 2)
    private BigDecimal gastosOperativos; // Agente de aduana, almacenaje, estiba

    @Column(name = "costo_transporte_local", precision = 10, scale = 2)
    private BigDecimal costoTransporteLocal; // Flete interno hasta tu almacén

    // --- AUDITORÍA ---
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();

        // Inicializamos valores en 0 para evitar nulls en cálculos matemáticos
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