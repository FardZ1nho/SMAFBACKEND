package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompraDetalleResponseDTO {

    // Identificadores
    private Integer id; // ID del detalle (útil si necesitas editarlo luego)

    // Producto
    private Integer productoId;
    private String nombreProducto;
    private String codigoProducto; // SKU

    // Almacén (Puede ser null si la compra fue de un SERVICIO)
    private Integer almacenId;
    private String nombreAlmacen;

    // Valores Numéricos
    private Integer cantidad;

    // Usamos BigDecimal para coincidir con la Entidad y no perder centavos
    private BigDecimal precioUnitario;

    // Campo calculado (cantidad * precio) útil para mostrar en la tabla del frontend
    private BigDecimal importeTotal;
}