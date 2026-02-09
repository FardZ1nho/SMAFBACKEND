package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Importante

@Data
public class ProductoResponseDTO {
    private Integer id;
    private String tipo;
    private String nombre;
    private String codigo;
    private String descripcion;
    private String codigoInternacional;
    private Integer idCategoria;
    private String nombreCategoria;

    private Integer stockActual;
    private Integer stockMinimo;
    private Integer stockPorLlegar;

    private BigDecimal precioChina;
    private BigDecimal costoTotal;
    private BigDecimal precioVenta;

    private String moneda;
    private String unidadMedida;

    private Boolean activo;
    private LocalDateTime fechaCreacion;

    private BigDecimal margenGanancia;
    private Double porcentajeMargen;
    private String estadoStock;
    private Boolean necesitaReorden;

    // ✅ NUEVO: Lista de componentes para mostrar en el detalle
    private List<ComponenteResponseDTO> componentes;

    // ✅ CLASE INTERNA para la respuesta
    @Data
    public static class ComponenteResponseDTO {
        private Integer idProducto;
        private String nombre;
        private Integer cantidad;
    }
}