package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.TareaCrmRequestDTO;
import com.upc.smaf.dtos.response.TareaCrmResponseDTO;

import java.util.List;

public interface TareaCrmService {
    TareaCrmResponseDTO crearTarea(TareaCrmRequestDTO requestDTO);
    List<TareaCrmResponseDTO> obtenerTareasPorCotizacion(Integer cotizacionId);
    List<TareaCrmResponseDTO> obtenerTareasPendientes();
    TareaCrmResponseDTO marcarComoCompletada(Long id);
    void eliminarTarea(Long id);
}