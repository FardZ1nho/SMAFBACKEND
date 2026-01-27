package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.TareaRequestDTO;
import com.upc.smaf.dtos.response.TareaResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.IUserRepository;
import com.upc.smaf.repositories.TareaRepository;
import com.upc.smaf.serviceinterface.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final IUserRepository usersRepository;

    @Override
    @Transactional
    public TareaResponseDTO crearTarea(TareaRequestDTO request, String usernameCreador) {
        // 1. Obtener al Creador (Admin) desde el token
        Users creador = usersRepository.findByUsername(usernameCreador);
        if (creador == null) throw new RuntimeException("Usuario creador no encontrado en base de datos");

        // 2. Obtener al Asignado (Trabajador) desde el DTO
        Users asignado = usersRepository.findById(request.getUsuarioAsignadoId())
                .orElseThrow(() -> new RuntimeException("Usuario asignado no encontrado"));

        // 3. Crear
        Tarea tarea = new Tarea();
        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setFechaLimite(request.getFechaLimite());
        tarea.setPrioridad(request.getPrioridad());
        tarea.setEstado(EstadoTarea.PENDIENTE);
        tarea.setUsuarioAsignado(asignado);
        tarea.setCreadoPor(creador); // Se asigna automáticamente al logueado

        return mapToDTO(tareaRepository.save(tarea));
    }

    @Override
    public List<TareaResponseDTO> listarTareas(String usernameSolicitante) {
        Users usuario = usersRepository.findByUsername(usernameSolicitante);
        if (usuario == null) throw new RuntimeException("Usuario no encontrado");

        // LÓGICA DE VISIBILIDAD:
        // Verificamos si tiene rol ADMIN iterando su lista de roles
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(rol -> rol.getRol().equals("ADMIN")); // Ajusta "rol" al nombre de tu campo en la entidad Role

        List<Tarea> tareas;
        if (esAdmin) {
            tareas = tareaRepository.findAll(); // Admin ve todo
        } else {
            tareas = tareaRepository.findByUsuarioAsignadoId(usuario.getId()); // Trabajador ve solo suyas
        }

        return tareas.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TareaResponseDTO cambiarEstado(Integer tareaId, EstadoTarea nuevoEstado, String usernameSolicitante) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Opcional: Validar que la tarea pertenezca al usuario que intenta completarla
        // if (!tarea.getUsuarioAsignado().getUsername().equals(usernameSolicitante)) { ... }

        tarea.setEstado(nuevoEstado);
        return mapToDTO(tareaRepository.save(tarea));
    }

    private TareaResponseDTO mapToDTO(Tarea t) {
        TareaResponseDTO d = new TareaResponseDTO();
        d.setId(t.getId());
        d.setTitulo(t.getTitulo());
        d.setDescripcion(t.getDescripcion());
        d.setFechaLimite(t.getFechaLimite());
        d.setPrioridad(t.getPrioridad().name());
        d.setEstado(t.getEstado().name());
        d.setFechaCreacion(t.getFechaCreacion());

        if (t.getUsuarioAsignado() != null) {
            d.setUsuarioAsignadoId(t.getUsuarioAsignado().getId());
            d.setUsernameAsignado(t.getUsuarioAsignado().getUsername());
        }
        if (t.getCreadoPor() != null) {
            d.setUsernameCreador(t.getCreadoPor().getUsername());
        }

        d.setVencida(t.getEstado() == EstadoTarea.PENDIENTE && LocalDateTime.now().isAfter(t.getFechaLimite()));
        return d;
    }
}