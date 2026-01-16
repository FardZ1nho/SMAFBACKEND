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

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    // Relación con la Venta (Muchos pagos pertenecen a una venta)
    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    // ✅ NUEVO: Cuenta donde entró el dinero de ESTA amortización
    // (Ej: Cliente viene 15 días después y paga por Yape)
    @ManyToOne
    @JoinColumn(name = "cuenta_destino_id", nullable = true)
    private CuentaBancaria cuentaDestino;

    @PrePersist
    protected void onCreate() {
        fechaPago = LocalDateTime.now();
    }
}