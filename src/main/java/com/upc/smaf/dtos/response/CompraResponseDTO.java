package com.upc.smaf.dtos.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Integer id;
    private String tipoComprobante;
    private String serie;
    private String numero;
    private LocalDate fecEmision;
    private String nombreProveedor; // Para mostrar en la tabla de compras
    private String rucProveedor;
    private String moneda;
    private Double tipoCambio;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private Double total; // <--- AGREGA ESTO

    private List<CompraDetalleResponseDTO> detalles;
}