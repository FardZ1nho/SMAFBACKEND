package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;

import java.util.List;

public interface CompraService {

    /**
     * Registra una nueva compra completa (Cabecera + Detalles).
     * Esta operación debe ser transaccional y actualizar el stock de productos.
     */
    CompraResponseDTO registrarCompra(CompraRequestDTO request);

    /**
     * Obtiene una compra específica por su ID, incluyendo todos sus detalles.
     */
    CompraResponseDTO obtenerCompra(Integer id);

    /**
     * Lista todas las compras registradas en el sistema.
     */
    List<CompraResponseDTO> listarTodas();

    /**
     * Lista compras filtradas por un proveedor específico.
     */
    List<CompraResponseDTO> listarPorProveedor(Integer proveedorId);

    /**
     * Busca compras por el número de comprobante (Serie-Número).
     */
    List<CompraResponseDTO> buscarPorNumero(String numero);
}