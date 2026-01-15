package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // <--- IMPORTANTE: Activa la auditoría de fechas
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 11, unique = true)
    private String ruc;

    @Column(length = 100)
    private String contacto;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 250)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

    // Se llena solo cuando creas el registro
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Se llena/actualiza solo cuando editas el registro (ESTO TE FALTABA)
    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Mantenemos esto solo para asegurar que 'activo' sea true por defecto
    @PrePersist
    protected void onPrePersist() {
        if (this.activo == null) {
            this.activo = true;
        }
        // ESTO ES LO QUE FALTABA: Asegurar que la fecha nunca sea null
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}