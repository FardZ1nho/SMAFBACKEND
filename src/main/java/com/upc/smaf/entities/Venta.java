package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    // ... (Tu relación con Cliente si la tienes, o nombre_cliente) ...
    @Column(name = "nombre_cliente", length = 100)
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false)
    private TipoCliente tipoCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    // ✅ NUEVO: Columnas para Pago Mixto
    @Column(name = "pago_efectivo", precision = 10, scale = 2)
    private BigDecimal pagoEfectivo;

    @Column(name = "pago_transferencia", precision = 10, scale = 2)
    private BigDecimal pagoTransferencia;

    // ✅ NUEVO: Columnas para Moneda y TC (Faltaban en tu Entity)
    @Column(length = 3)
    private String moneda; // PEN, USD

    @Column(name = "tipo_cambio", precision = 10, scale = 4) // Scale 4 para mejor precisión en TC
    private BigDecimal tipoCambio;

    // ✅ NUEVO: Tipo de Comprobante (Factura, Boleta) y el Número (F001-123)
    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal igv;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(length = 500)
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}