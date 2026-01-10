package com.upc.smaf.repositories;

import com.upc.smaf.entities.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    // Buscar por código
    Optional<Almacen> findByCodigo(String codigo);

    // Listar solo almacenes activos
    List<Almacen> findByActivoTrue();

    // Verificar si existe un código
    boolean existsByCodigo(String codigo);

    // Verificar si existe un código diferente al ID actual (para edición)
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}