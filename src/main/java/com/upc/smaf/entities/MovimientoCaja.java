package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_caja_chica")
@Data
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String tipo; // "INGRESO" o "EGRESO"

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private String motivo;

    // ✅ NUEVO: Para poder clasificar en el gráfico (Combustible, Pasajes, etc.)
    @Column(name = "categoria", nullable = false, columnDefinition = "varchar(255) default 'OTROS'")
    private String categoria;

    @Column(nullable = false)
    private String responsable;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    // ✅ NUEVO: Enlace al turno de caja activo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_caja_id")
    private TurnoCaja turnoCaja;

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }
}