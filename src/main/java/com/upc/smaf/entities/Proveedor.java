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
@EntityListeners(AuditingEntityListener.class)
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String nombre;

    // Aumentamos a 20 para cubrir los 18 de China y posibles nuevos formatos
    @Column(length = 20, unique = true)
    private String ruc;

    // Agregamos el país para lógica de negocio (PERÚ, CHINA, etc.)
    @Column(length = 50, nullable = false)
    private String pais;

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

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onPrePersist() {
        if (this.activo == null) this.activo = true;
        if (this.fechaCreacion == null) this.fechaCreacion = LocalDateTime.now();
        // Normalizar a mayúsculas para evitar problemas de búsqueda
        if (this.ruc != null) this.ruc = this.ruc.toUpperCase().trim();
    }
}