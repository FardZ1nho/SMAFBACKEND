package com.upc.smaf.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadTarea prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTarea estado;

    @ManyToOne
    @JoinColumn(name = "usuario_asignado_id", nullable = false)
    private Users usuarioAsignado;

    @ManyToOne
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Users creadoPor;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // ⭐⭐⭐ NUEVOS CAMPOS PARA EL CRM ⭐⭐⭐

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnore // Evita problemas de serialización al listar tareas
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    @JsonIgnore
    private Cotizacion cotizacion;

    // Ej: "LLAMADA", "WHATSAPP", "CORREO"
    @Column(name = "tipo_accion", length = 50)
    private String tipoAccion;

    @PrePersist
    void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoTarea.PENDIENTE;
    }
}