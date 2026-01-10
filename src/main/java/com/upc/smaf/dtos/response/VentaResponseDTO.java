package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.entities.TipoCliente;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {

    private Integer id;
    private String codigo;
    private LocalDateTime fechaVenta;
    private String nombreCliente;
    private TipoCliente tipoCliente;
    private MetodoPago metodoPago;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String notas;
    private EstadoVenta estado;
    private List<DetalleVentaResponseDTO> detalles;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}