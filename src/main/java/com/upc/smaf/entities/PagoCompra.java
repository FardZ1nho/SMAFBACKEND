package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_compras") // Tabla separada para evitar conflictos con ventas
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda; // PEN o USD

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago; // EFECTIVO, TRANSFERENCIA, YAPE...

    @Column(length = 100)
    private String referencia; // Nro de Operación

    // De qué cuenta salió el dinero (Opcional si es Efectivo de caja chica)
    @ManyToOne
    @JoinColumn(name = "cuenta_origen_id")
    private CuentaBancaria cuentaOrigen;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;
}