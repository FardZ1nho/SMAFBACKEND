package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.TareaCrmRequestDTO;
import com.upc.smaf.dtos.response.TareaCrmResponseDTO;
import com.upc.smaf.entities.Cotizacion;
import com.upc.smaf.entities.EstadoTareaCrm;
import com.upc.smaf.entities.TareaCrm;
import com.upc.smaf.repositories.CotizacionRepository;
import com.upc.smaf.repositories.TareaCrmRepository;
import com.upc.smaf.serviceinterface.TareaCrmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TareaCrmServiceImpl implements TareaCrmService {

    @Autowired
    private TareaCrmRepository tareaCrmRepository;

    @Autowired
    private CotizacionRepository cotizacionRepository; // Asegúrate de tener este repositorio

    @Override
    public TareaCrmResponseDTO crearTarea(TareaCrmRequestDTO requestDTO) {
        Cotizacion cotizacion = cotizacionRepository.findById(requestDTO.getCotizacionId())
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        TareaCrm tarea = new TareaCrm();
        tarea.setCotizacion(cotizacion);
        tarea.setTitulo(requestDTO.getTitulo());
        tarea.setDescripcion(requestDTO.getDescripcion());
        tarea.setFechaLimite(requestDTO.getFechaLimite());
        tarea.setTipo(requestDTO.getTipo());
        tarea.setEstado(EstadoTareaCrm.PENDIENTE); // Toda tarea nueva nace como pendiente

        TareaCrm tareaGuardada = tareaCrmRepository.save(tarea);
        return mapearADTO(tareaGuardada);
    }

    // Cambia la firma del método para que use Integer:
    @Override
    public List<TareaCrmResponseDTO> obtenerTareasPorCotizacion(Integer cotizacionId) {
        return tareaCrmRepository.findByCotizacionId(cotizacionId)
                .stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    @Override
    public List<TareaCrmResponseDTO> obtenerTareasPendientes() {
        return tareaCrmRepository.findByEstadoOrderByFechaLimiteAsc(EstadoTareaCrm.PENDIENTE)
                .stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    @Override
    public TareaCrmResponseDTO marcarComoCompletada(Long id) {
        TareaCrm tarea = tareaCrmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        tarea.setEstado(EstadoTareaCrm.COMPLETADA);
        return mapearADTO(tareaCrmRepository.save(tarea));
    }

    @Override
    public void eliminarTarea(Long id) {
        tareaCrmRepository.deleteById(id);
    }

    // Método auxiliar para convertir Entidad a DTO
    private TareaCrmResponseDTO mapearADTO(TareaCrm tarea) {
        TareaCrmResponseDTO dto = new TareaCrmResponseDTO();
        dto.setId(tarea.getId());
        dto.setCotizacionId(tarea.getCotizacion().getId());
        dto.setTitulo(tarea.getTitulo());
        dto.setDescripcion(tarea.getDescripcion());
        dto.setFechaLimite(tarea.getFechaLimite());
        dto.setEstado(tarea.getEstado());
        dto.setTipo(tarea.getTipo());
        return dto;
    }
}