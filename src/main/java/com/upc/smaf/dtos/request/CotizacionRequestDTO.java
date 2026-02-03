package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CotizacionRequestDTO {
    private Integer idCliente;
    private LocalDate fechaVencimiento;
    private String moneda;
    private BigDecimal tipoCambio;
    private String observaciones;

    // Totales calculados en el front (validaremos en back también)
    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    private List<DetalleCotizacionDTO> detalles;

    @Data
    public static class DetalleCotizacionDTO {
        private Integer idProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}