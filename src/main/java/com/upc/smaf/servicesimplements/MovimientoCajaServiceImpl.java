package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.MovimientoCajaRequestDTO;
import com.upc.smaf.dtos.response.MovimientoCajaResponseDTO;
import com.upc.smaf.entities.MovimientoCaja;
import com.upc.smaf.repositories.MovimientoCajaRepository;
import com.upc.smaf.serviceinterface.MovimientoCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoCajaServiceImpl implements MovimientoCajaService {

    private final MovimientoCajaRepository repository;

    @Override
    @Transactional
    public MovimientoCajaResponseDTO registrarMovimiento(MovimientoCajaRequestDTO request) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(request.getTipo());
        movimiento.setMonto(request.getMonto());
        movimiento.setMotivo(request.getMotivo());
        movimiento.setResponsable(request.getResponsable());

        // Si el frontend no manda fecha, usamos la actual
        movimiento.setFechaHora(request.getFechaHora() != null ? request.getFechaHora() : LocalDateTime.now());

        MovimientoCaja guardado = repository.save(movimiento);
        return mapToResponseDTO(guardado);
    }

    // ✅ NUEVO MÉTODO PARA ACTUALIZAR
    @Override
    @Transactional
    public MovimientoCajaResponseDTO actualizarMovimiento(Integer id, MovimientoCajaRequestDTO request) {
        MovimientoCaja movimiento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento de caja no encontrado con ID: " + id));

        movimiento.setTipo(request.getTipo());
        movimiento.setMonto(request.getMonto());
        movimiento.setMotivo(request.getMotivo());
        movimiento.setResponsable(request.getResponsable());

        // Solo actualizamos la fecha si el frontend nos manda una
        if (request.getFechaHora() != null) {
            movimiento.setFechaHora(request.getFechaHora());
        }

        MovimientoCaja guardado = repository.save(movimiento);
        return mapToResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoCajaResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper para convertir
    private MovimientoCajaResponseDTO mapToResponseDTO(MovimientoCaja entidad) {
        MovimientoCajaResponseDTO dto = new MovimientoCajaResponseDTO();
        dto.setId(entidad.getId());
        dto.setTipo(entidad.getTipo());
        dto.setMonto(entidad.getMonto());
        dto.setMotivo(entidad.getMotivo());
        dto.setResponsable(entidad.getResponsable());
        dto.setFechaHora(entidad.getFechaHora());
        return dto;
    }
}