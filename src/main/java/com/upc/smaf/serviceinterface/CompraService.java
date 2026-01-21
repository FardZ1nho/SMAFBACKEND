package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;

import java.util.List;

public interface CompraService {

    // Registra la compra y, si es FACTURA_COMERCIAL, crea la Importación automáticamente
    CompraResponseDTO registrarCompra(CompraRequestDTO request);

    // Obtiene una compra por su ID
    CompraResponseDTO obtenerCompra(Integer id);

    // Lista todas las compras
    List<CompraResponseDTO> listarTodas();

    // Lista compras de un proveedor específico
    List<CompraResponseDTO> listarPorProveedor(Integer proveedorId);

    // Busca compras por número de documento (serie-numero)
    List<CompraResponseDTO> buscarPorNumero(String numero);
}