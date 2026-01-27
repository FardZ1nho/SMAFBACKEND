package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compra", nullable = false, length = 20)
    private TipoCompra tipoCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    private TipoComprobante tipoComprobante;

    // ✅ NUEVO: TIPO DE PAGO (CONTADO / CREDITO)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "fec_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fec_vencimiento")
    private LocalDate fechaVencimiento;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(length = 10)
    private String moneda = "PEN";

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    // --- TOTALES ---
    @Column(name = "sub_total", precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // --- SALDOS Y DEUDA (Similar a Ventas) ---
    @Column(name = "monto_pagado_inicial", precision = 12, scale = 2)
    private BigDecimal montoPagadoInicial = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente", precision = 12, scale = 2)
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    // --- IMPUESTOS ESPECÍFICOS ---
    @Column(precision = 12, scale = 2)
    private BigDecimal percepcion = BigDecimal.ZERO;

    @Column(name = "detraccion_porcentaje", precision = 5, scale = 2)
    private BigDecimal detraccionPorcentaje = BigDecimal.ZERO;

    @Column(name = "detraccion_monto", precision = 12, scale = 2)
    private BigDecimal detraccionMonto = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal retencion = BigDecimal.ZERO;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado = EstadoCompra.REGISTRADA; // Crea este Enum o usa String
    @Column(name = "cod_importacion", length = 50)
    private String codImportacion;
    // --- RELACIONES ---
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalle> detalles = new ArrayList<>();

    // ✅ LISTA DE PAGOS (Cascade ALL y EAGER para que carguen siempre)
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PagoCompra> pagos = new ArrayList<>();

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.subTotal == null) this.subTotal = BigDecimal.ZERO;
        if (this.igv == null) this.igv = BigDecimal.ZERO;
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (this.saldoPendiente == null) this.saldoPendiente = BigDecimal.ZERO;
    }

    // Helper para relación bidireccional
    public void agregarPago(PagoCompra pago) {
        pagos.add(pago);
        pago.setCompra(this);
    }
}