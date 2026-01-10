package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @Column(name = "tipo_cliente", length = 20, nullable = false)
    private String tipoCliente; // PERSONA, EMPRESA

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento; // DNI, RUC, PASAPORTE, CARNET_EXTRANJERIA

    @Column(name = "numero_documento", unique = true, length = 20)
    private String numeroDocumento;

    // ⭐⭐⭐ AGREGAR ESTOS DOS CAMPOS ⭐⭐⭐
    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    // Contacto
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    // Dirección
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "departamento", length = 100)
    private String departamento;

    // Para empresas
    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "nombre_contacto", length = 200)
    private String nombreContacto;

    // Información adicional
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Cliente(String tipoCliente, String nombreCompleto, String numeroDocumento) {
        this.tipoCliente = tipoCliente;
        this.nombreCompleto = nombreCompleto;
        this.numeroDocumento = numeroDocumento;
    }
}