package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.TareaRequestDTO;
import com.upc.smaf.dtos.response.TareaResponseDTO;
import com.upc.smaf.entities.EstadoTarea;
import java.util.List;

public interface TareaService {
    TareaResponseDTO crearTarea(TareaRequestDTO request, String usernameCreador);
    List<TareaResponseDTO> listarTareas(String usernameSolicitante);
    TareaResponseDTO cambiarEstado(Integer tareaId, EstadoTarea nuevoEstado, String usernameSolicitante);
}