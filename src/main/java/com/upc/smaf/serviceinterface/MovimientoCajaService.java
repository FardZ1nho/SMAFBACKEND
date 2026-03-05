package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.MovimientoCajaRequestDTO;
import com.upc.smaf.dtos.response.MovimientoCajaResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoCajaService {
    MovimientoCajaResponseDTO registrarMovimiento(MovimientoCajaRequestDTO request);
    List<MovimientoCajaResponseDTO> listarTodos();
    MovimientoCajaResponseDTO actualizarMovimiento(Integer id, MovimientoCajaRequestDTO request);

    // ✅ CORREGIDO: Ahora usa Integer para que coincida con tu BD
    void eliminarMovimiento(Integer id);
    MovimientoCajaResponseDTO depositarABanco(BigDecimal monto, Integer cuentaId, String responsable);
}