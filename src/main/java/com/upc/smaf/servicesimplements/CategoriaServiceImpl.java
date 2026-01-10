package com.upc.smaf.serviceimplements;

import com.upc.smaf.dtos.request.CategoriaRequestDTO;
import com.upc.smaf.dtos.response.CategoriaResponseDTO;
import com.upc.smaf.entities.Categoria;
import com.upc.smaf.repositories.CategoriaRepository;
import com.upc.smaf.serviceinterface.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO categoriaRequestDTO) {
        // Validar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombre(categoriaRequestDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + categoriaRequestDTO.getNombre());
        }

        Categoria categoria = new Categoria(
                categoriaRequestDTO.getNombre(),
                categoriaRequestDTO.getDescripcion()
        );

        Categoria categoriaSaved = categoriaRepository.save(categoria);
        return convertirAResponseDTO(categoriaSaved);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Integer id, CategoriaRequestDTO categoriaRequestDTO) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        // Validar que no exista otra categoría con el mismo nombre
        if (!categoria.getNombre().equals(categoriaRequestDTO.getNombre())
                && categoriaRepository.existsByNombre(categoriaRequestDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + categoriaRequestDTO.getNombre());
        }

        categoria.setNombre(categoriaRequestDTO.getNombre());
        categoria.setDescripcion(categoriaRequestDTO.getDescripcion());

        if (categoriaRequestDTO.getActivo() != null) {
            categoria.setActivo(categoriaRequestDTO.getActivo());
        }

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return convertirAResponseDTO(categoriaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        return convertirAResponseDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarActivas() {
        return categoriaRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con id: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activarDesactivar(Integer id, Boolean activo) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoria.setActivo(activo);
        categoriaRepository.save(categoria);
    }

    private CategoriaResponseDTO convertirAResponseDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo(),
                categoria.getFechaCreacion()
        );
    }
}