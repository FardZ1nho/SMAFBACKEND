package com.upc.smaf.servicesimplements;

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
        // 1. Normalización y Limpieza
        String identificacion = request.getRuc().trim().toUpperCase();
        String pais = request.getPais().trim().toUpperCase();

        // 2. Validaciones de Negocio según el País
        validarIdentificacionPorPais(identificacion, pais);

        // 3. Validar duplicidad
        if (proveedorRepository.existsByRuc(identificacion)) {
            throw new IllegalArgumentException("La identificación " + identificacion + " ya se encuentra registrada.");
        }

        Proveedor proveedor = new Proveedor();
        mapRequestToEntity(request, proveedor);

        // Forzamos valores de creación
        proveedor.setRuc(identificacion);
        proveedor.setPais(pais);
        proveedor.setActivo(true);

        Proveedor saved = proveedorRepository.save(proveedor);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerProveedor(Integer id) {
        Proveedor proveedor = findByIdInternal(id);
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
        Proveedor proveedor = findByIdInternal(id);

        String identificacion = request.getRuc().trim().toUpperCase();
        String pais = request.getPais().trim().toUpperCase();

        // Validaciones de formato
        validarIdentificacionPorPais(identificacion, pais);

        // Validar que el RUC no lo tenga OTRO proveedor
        if (proveedorRepository.existsByRucAndIdNot(identificacion, id)) {
            throw new IllegalArgumentException("La identificación " + identificacion + " ya pertenece a otro proveedor.");
        }

        mapRequestToEntity(request, proveedor);
        proveedor.setRuc(identificacion); // Asegurar normalización en update
        proveedor.setPais(pais);

        Proveedor updated = proveedorRepository.save(proveedor);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void desactivarProveedor(Integer id) {
        Proveedor proveedor = findByIdInternal(id);
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
        Proveedor proveedor = proveedorRepository.findByRuc(ruc.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con identificación: " + ruc));
        return mapToDTO(proveedor);
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private Proveedor findByIdInternal(Integer id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }

    private void validarIdentificacionPorPais(String iden, String pais) {
        if ("PERÚ".equals(pais) || "PERU".equals(pais)) {
            // RUC Peruano: 11 dígitos numéricos
            if (!iden.matches("\\d{11}")) {
                throw new IllegalArgumentException("El RUC peruano debe tener exactamente 11 dígitos numéricos.");
            }
        } else if ("CHINA".equals(pais)) {
            // USCC Chino: 18 caracteres alfanuméricos
            if (iden.length() != 18) {
                throw new IllegalArgumentException("El código USCC de China debe tener exactamente 18 caracteres.");
            }
        }
    }

    private void mapRequestToEntity(ProveedorRequestDTO request, Proveedor proveedor) {
        proveedor.setNombre(request.getNombre());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        if (request.getActivo() != null) {
            proveedor.setActivo(request.getActivo());
        }
    }

    private ProveedorResponseDTO mapToDTO(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setRuc(proveedor.getRuc());
        dto.setPais(proveedor.getPais()); // Nuevo campo
        dto.setContacto(proveedor.getContacto());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setDireccion(proveedor.getDireccion());
        dto.setActivo(proveedor.getActivo());
        dto.setFechaCreacion(proveedor.getFechaCreacion());
        dto.setFechaActualizacion(proveedor.getFechaActualizacion());
        return dto;
    }
}