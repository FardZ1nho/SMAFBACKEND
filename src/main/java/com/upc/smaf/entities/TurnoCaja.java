package com.upc.smaf.entities;

import com.fasterxml.jackson.annotation.JsonIgnore; // ✅ IMPORTACIÓN AGREGADA
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "turnos_caja")
@Data
public class TurnoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String estado; // "ABIERTO" o "CERRADO"

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoInicial;

    @Column(precision = 12, scale = 2)
    private BigDecimal saldoFinalCalculado; // Lo que dice el sistema

    @Column(precision = 12, scale = 2)
    private BigDecimal saldoFinalFisico; // Los billetes reales que contó el cajero

    @Column(precision = 12, scale = 2)
    private BigDecimal descuadre; // Diferencia (Físico - Calculado)

    @Column(nullable = false)
    private String responsable;

    @OneToMany(mappedBy = "turnoCaja", cascade = CascadeType.ALL)
    @JsonIgnore // ✅ ESTO EVITA EL BUCLE INFINITO (Error 500 / StackOverflow)
    private List<MovimientoCaja> movimientos;
}