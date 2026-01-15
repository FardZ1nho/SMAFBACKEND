package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas_credito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 4)
    private String serie; // Ej: NC01

    @Column(nullable = false, length = 10)
    private String numero; // Ej: 00000025

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta ventaOriginal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoNota motivo;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(length = 3)
    private String moneda;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @PrePersist
    protected void onCreate() {
        fechaEmision = LocalDateTime.now();
    }
}