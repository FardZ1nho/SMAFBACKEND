package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.CategoriaRequestDTO;
import com.upc.smaf.dtos.response.CategoriaResponseDTO;
import com.upc.smaf.serviceinterface.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        CategoriaResponseDTO response = categoriaService.crear(categoriaRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        CategoriaResponseDTO response = categoriaService.actualizar(id, categoriaRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        CategoriaResponseDTO response = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        List<CategoriaResponseDTO> categorias = categoriaService.listarTodas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarActivas() {
        List<CategoriaResponseDTO> categorias = categoriaService.listarActivas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<CategoriaResponseDTO> categorias = categoriaService.buscarPorNombre(nombre);
        return ResponseEntity.ok(categorias);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> activarDesactivar(
            @PathVariable Integer id,
            @RequestParam Boolean activo) {
        categoriaService.activarDesactivar(id, activo);
        return ResponseEntity.ok().build();
    }
}