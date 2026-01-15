package com.upc.smaf.dtos.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CompraRequestDTO {
    private String tipoComprobante;
    private String serie;
    private String numero;
    private LocalDate fecEmision;
    private Integer proveedorId;
    private String moneda;
    private Double tipoCambio;
    private String observaciones;
    private List<DetalleRequestDTO> detalles;

    @Data
    public static class DetalleRequestDTO {
        private Integer productoId;
        private Integer almacenId;
        private Integer cantidad;
        private Double precioUnitario;
    }
}