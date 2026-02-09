package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ✅ CAMPO FALTANTE QUE CAUSA EL ERROR
    // Al ponerle "= true", nos aseguramos de que nunca sea nulo al crear
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // --- DATOS DEL DOCUMENTO ---
    @Column(nullable = false, length = 20)
    private String serie;

    @Column(nullable = false, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compra")
    private TipoCompra tipoCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante")
    private TipoComprobante tipoComprobante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago")
    private TipoPago tipoPago;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // --- MONTOS ---
    @Column(length = 3)
    private String moneda;

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    @Column(name = "sub_total")
    private BigDecimal subTotal;

    @Column(name = "fob")
    private BigDecimal fob;

    @Column(name = "igv")
    private BigDecimal igv;

    @Column(name = "total")
    private BigDecimal total;

    // --- SALDOS Y ESTADO ---
    @Column(name = "monto_pagado_inicial")
    private BigDecimal montoPagadoInicial;

    @Column(name = "saldo_pendiente")
    private BigDecimal saldoPendiente;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado;

    // --- IMPUESTOS LOCALES ---
    private BigDecimal percepcion;
    private BigDecimal detraccionPorcentaje;
    private BigDecimal detraccionMonto;
    private BigDecimal retencion;

    // --- IMPORTACIÓN ---
    @Column(name = "cod_importacion")
    private String codImportacion;

    @Column(name = "peso_neto_kg")
    private BigDecimal pesoNetoKg;

    @Column(name = "cbm")
    private BigDecimal cbm;

    // ==========================================
    // 🚢 PRORRATEO DETALLADO (Nuevos campos)
    // ==========================================
    private BigDecimal proFlete;
    private BigDecimal proAlmacenaje;
    private BigDecimal proTransporte;
    private BigDecimal proPersonalDescarga;
    private BigDecimal proMontacarga;

    private BigDecimal proDesconsolidacion;

    private BigDecimal proVistosBuenos;
    private BigDecimal proTransmision;
    private BigDecimal proComisionAgencia;
    private BigDecimal proVobo;
    private BigDecimal proGastosOperativos;
    private BigDecimal proResguardo;

    private BigDecimal proAdv;
    private BigDecimal proIgv;
    private BigDecimal proIpm;
    private BigDecimal proPercepcion;

    private BigDecimal proOtros1;
    private BigDecimal proOtros2;
    private BigDecimal proOtros3;
    private BigDecimal proOtros4;

    // Campos antiguos (puedes dejarlos o borrarlos si ya no los usas en BD)
    private BigDecimal proCargaDescarga;
    private BigDecimal proGastosAduaneros;
    private BigDecimal proSeguroResguardo;
    private BigDecimal proImpuestos;
    private BigDecimal proOtrosGastos;

    @Column(name = "costo_total_importacion")
    private BigDecimal costoTotalImportacion;

    // --- RELACIONES ---
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "importacion_id")
    private Importacion importacion;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompraDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PagoCompra> pagos = new ArrayList<>();

    // Helpers
    public void agregarDetalle(CompraDetalle detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
    }
    public void agregarPago(PagoCompra pago) {
        pagos.add(pago);
        pago.setCompra(this);
    }
}