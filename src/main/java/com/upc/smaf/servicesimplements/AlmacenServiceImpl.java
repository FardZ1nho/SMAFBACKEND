package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.AlmacenRequestDTO;
import com.upc.smaf.dtos.response.AlmacenResponseDTO;
import com.upc.smaf.entities.Almacen;
import com.upc.smaf.repositories.AlmacenRepository;
import com.upc.smaf.serviceinterface.AlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {

    private final AlmacenRepository almacenRepository;

    @Override
    @Transactional
    public AlmacenResponseDTO crearAlmacen(AlmacenRequestDTO request) {
        // Validar que el código no exista
        if (almacenRepository.existsByCodigo(request.getCodigo())) {
            throw new RuntimeException("Ya existe un almacén con el código: " + request.getCodigo());
        }

        Almacen almacen = new Almacen();
        almacen.setCodigo(request.getCodigo().trim().toUpperCase());
        almacen.setNombre(request.getNombre().trim());
        almacen.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        almacen.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Almacen guardado = almacenRepository.save(almacen);
        return convertirAResponseDTO(guardado);
    }

    @Override
    @Transactional
    public AlmacenResponseDTO actualizarAlmacen(Long id, AlmacenRequestDTO request) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + id));

        // Validar que el código no exista en otro almacén
        if (almacenRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
            throw new RuntimeException("Ya existe otro almacén con el código: " + request.getCodigo());
        }

        almacen.setCodigo(request.getCodigo().trim().toUpperCase());
        almacen.setNombre(request.getNombre().trim());
        almacen.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);

        if (request.getActivo() != null) {
            almacen.setActivo(request.getActivo());
        }

        Almacen actualizado = almacenRepository.save(almacen);
        return convertirAResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public AlmacenResponseDTO obtenerAlmacenPorId(Long id) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + id));
        return convertirAResponseDTO(almacen);
    }

    @Override
    @Transactional(readOnly = true)
    public AlmacenResponseDTO obtenerAlmacenPorCodigo(String codigo) {
        Almacen almacen = almacenRepository.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con código: " + codigo));
        return convertirAResponseDTO(almacen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlmacenResponseDTO> listarTodosLosAlmacenes() {
        return almacenRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlmacenResponseDTO> listarAlmacenesActivos() {
        return almacenRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarEstadoAlmacen(Long id, Boolean activo) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + id));
        almacen.setActivo(activo);
        almacenRepository.save(almacen);
    }

    @Override
    @Transactional
    public void eliminarAlmacen(Long id) {
        if (!almacenRepository.existsById(id)) {
            throw new RuntimeException("Almacén no encontrado con ID: " + id);
        }

        // TODO: Validar que no tenga productos asociados antes de eliminar
        // if (tieneProductosAsociados(id)) {
        //     throw new RuntimeException("No se puede eliminar el almacén porque tiene productos asociados");
        // }

        almacenRepository.deleteById(id);
    }

    // Método auxiliar para convertir entidad a DTO
    private AlmacenResponseDTO convertirAResponseDTO(Almacen almacen) {
        AlmacenResponseDTO dto = new AlmacenResponseDTO();
        dto.setId(almacen.getId());
        dto.setCodigo(almacen.getCodigo());
        dto.setNombre(almacen.getNombre());
        dto.setDireccion(almacen.getDireccion());
        dto.setActivo(almacen.getActivo());
        dto.setFechaCreacion(almacen.getFechaCreacion());
        return dto;
    }
}