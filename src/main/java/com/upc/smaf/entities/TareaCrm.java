package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_tareas") // Ajustado al español
public class TareaCrm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    private String titulo;
    private String descripcion;

    private LocalDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    private EstadoTareaCrm estado; // ¡Aquí estaba el error! Cambiado a EstadoTareaCrm

    @Enumerated(EnumType.STRING)
    private TipoTareaCrm tipo;
}