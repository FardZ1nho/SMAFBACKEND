package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.entities.MetodoPago; // Asegúrate de importar esto

import java.math.BigDecimal;
import java.util.List;

public interface CompraService {

    CompraResponseDTO registrarCompra(CompraRequestDTO request);
    CompraResponseDTO obtenerCompra(Integer id);
    List<CompraResponseDTO> listarTodas();
    List<CompraResponseDTO> listarPorProveedor(Integer proveedorId);
    List<CompraResponseDTO> buscarPorNumero(String numero);

    // ✅ NUEVO: Registrar un pago posterior (Amortización de deuda)
    CompraResponseDTO registrarAmortizacion(Integer compraId, BigDecimal monto, MetodoPago metodo, Integer cuentaOrigenId, String referencia);
}