package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CompraRequestDTO {

    // ✅ NUEVO: BIEN o SERVICIO
    @NotNull(message = "El tipo de compra es obligatorio")
    private String tipoCompra;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    private String serie;
    private String numero;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision; // Ajusté nombre a camelCase estándar

    private LocalDate fechaVencimiento; // Opcional

    @NotNull(message = "El proveedor es obligatorio")
    private Integer proveedorId;

    private String moneda;
    private BigDecimal tipoCambio;
    private String observaciones;

    // ✅ NUEVO: IMPUESTOS DEL EXCEL
    // Se envían desde el front si aplican (si no, pueden ser null o 0)
    private BigDecimal percepcion;         // Solo Bienes
    private BigDecimal detraccionPorcentaje; // Solo Servicios
    private BigDecimal detraccionMonto;      // Solo Servicios
    private BigDecimal retencion;          // Ambos

    // Los totales suelen calcularse en Backend, pero si el front los manda para validar:
    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    private List<DetalleRequestDTO> detalles;

    @Data
    public static class DetalleRequestDTO {
        private Integer productoId;

        // Opcional: Si es SERVICIO, esto vendrá null
        private Integer almacenId;

        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}