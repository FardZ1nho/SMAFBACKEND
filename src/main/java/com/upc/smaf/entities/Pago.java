package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    // ✅ NUEVO: Moneda del pago (PEN/USD) - Soluciona el error setMoneda
    @Column(length = 3, nullable = false)
    private String moneda;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    // ✅ NUEVO: Referencia (Nro Operación, Yape ID, etc.) - Soluciona el error setReferencia
    @Column(length = 100)
    private String referencia;

    // Relación con Venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    // Cuenta destino
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id", nullable = true)
    private CuentaBancaria cuentaDestino;

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) fechaPago = LocalDateTime.now();
    }
}