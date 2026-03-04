package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.MovimientoCajaRequestDTO;
import com.upc.smaf.dtos.response.MovimientoCajaResponseDTO;
import java.util.List;

public interface MovimientoCajaService {
    MovimientoCajaResponseDTO registrarMovimiento(MovimientoCajaRequestDTO request);
    List<MovimientoCajaResponseDTO> listarTodos();
    MovimientoCajaResponseDTO actualizarMovimiento(Integer id, MovimientoCajaRequestDTO request);
}