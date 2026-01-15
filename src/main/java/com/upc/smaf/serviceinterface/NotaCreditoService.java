package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.NotaCreditoRequestDTO;
import com.upc.smaf.dtos.response.NotaCreditoResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface NotaCreditoService {
    NotaCreditoResponseDTO emitirNotaCredito(NotaCreditoRequestDTO request);
    List<NotaCreditoResponseDTO> listarTodas();
    List<NotaCreditoResponseDTO> listarPorVenta(Integer ventaId);
    BigDecimal obtenerTotalDevoluciones();
}