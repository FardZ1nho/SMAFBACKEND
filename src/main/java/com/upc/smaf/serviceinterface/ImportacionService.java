package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ImportacionRequestDTO;
import com.upc.smaf.dtos.response.ImportacionResponseDTO;
import com.upc.smaf.entities.EstadoImportacion;
import java.util.List;

public interface ImportacionService {
    List<ImportacionResponseDTO> listarTodas();
    List<ImportacionResponseDTO> listarPorEstado(EstadoImportacion estado);
    ImportacionResponseDTO obtenerPorId(Integer id);

    // Método para actualizar costos, fechas y estado
    ImportacionResponseDTO actualizarImportacion(Integer id, ImportacionRequestDTO request);
}