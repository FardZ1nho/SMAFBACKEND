package com.upc.smaf.serviceimpl;

import com.upc.smaf.dtos.request.ProveedorRequestDTO;
import com.upc.smaf.dtos.response.ProveedorResponseDTO;
import com.upc.smaf.entities.Proveedor;
import com.upc.smaf.repositories.ProveedorRepository;
import com.upc.smaf.serviceinterface.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional
    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO request) {
        // Validar RUC único (si se proporciona)
        if (request.getRuc() != null && !request.getRuc().isEmpty()) {
            if (proveedorRepository.existsByRuc(request.getRuc())) {
                throw new RuntimeException("Ya existe un proveedor con el RUC: " + request.getRuc());
            }
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.getNombre());
        proveedor.setRuc(request.getRuc());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Proveedor saved = proveedorRepository.save(proveedor);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerProveedor(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        return mapToDTO(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarProveedores() {
        return proveedorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarProveedoresActivos() {
        return proveedorRepository.findByActivoTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProveedorResponseDTO actualizarProveedor(Integer id, ProveedorRequestDTO request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        // Validar RUC único si se está cambiando
        if (request.getRuc() != null && !request.getRuc().equals(proveedor.getRuc())) {
            if (proveedorRepository.existsByRuc(request.getRuc())) {
                throw new RuntimeException("Ya existe un proveedor con el RUC: " + request.getRuc());
            }
        }

        proveedor.setNombre(request.getNombre());
        proveedor.setRuc(request.getRuc());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        if (request.getActivo() != null) {
            proveedor.setActivo(request.getActivo());
        }

        Proveedor updated = proveedorRepository.save(proveedor);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void desactivarProveedor(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> buscarPorNombre(String nombre) {
        return proveedorRepository.buscarPorNombre(nombre).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorRuc(String ruc) {
        Proveedor proveedor = proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con RUC: " + ruc));
        return mapToDTO(proveedor);
    }

    // Método auxiliar para mapear entidad a DTO
    private ProveedorResponseDTO mapToDTO(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setRuc(proveedor.getRuc());
        dto.setContacto(proveedor.getContacto());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setDireccion(proveedor.getDireccion());
        dto.setActivo(proveedor.getActivo());
        dto.setFechaCreacion(proveedor.getFechaCreacion());
        return dto;
    }
}