package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.entities.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {

    // ========== CRUD BÁSICO ==========
    VentaResponseDTO crearVenta(VentaRequestDTO request);
    VentaResponseDTO obtenerVenta(Integer id);
    VentaResponseDTO actualizarVenta(Integer id, VentaRequestDTO request);
    void eliminarVenta(Integer id);

    // ========== LISTAR ==========
    List<VentaResponseDTO> listarTodas();
    List<VentaResponseDTO> listarPorEstado(EstadoVenta estado);
    List<VentaResponseDTO> listarPorFecha(LocalDateTime inicio, LocalDateTime fin);
    List<VentaResponseDTO> listarBorradores();
    List<VentaResponseDTO> listarCompletadas();

    // ========== BUSCAR ==========
    VentaResponseDTO buscarPorCodigo(String codigo);
    List<VentaResponseDTO> buscarPorCliente(String nombreCliente);

    // ========== ACCIONES ESPECIALES ==========
    VentaResponseDTO guardarBorrador(VentaRequestDTO request);
    VentaResponseDTO completarVenta(Integer id);
    VentaResponseDTO convertirBorradorAVenta(Integer id);
    void cancelarVenta(Integer id);

    // ✅ ACTUALIZADO: Agregamos "Integer cuentaId" al final
    VentaResponseDTO registrarAmortizacion(Integer ventaId, BigDecimal monto, MetodoPago metodo, Integer cuentaId);

    // ========== ESTADÍSTICAS ==========
    Long contarVentasPorEstado(EstadoVenta estado);
}