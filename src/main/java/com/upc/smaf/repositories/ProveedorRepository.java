package com.upc.smaf.repositories;

import com.upc.smaf.entities.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    // Listar solo proveedores activos
    List<Proveedor> findByActivoTrue();

    // Buscar por nombre (case insensitive)
    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Proveedor> buscarPorNombre(@Param("nombre") String nombre);

    // Buscar por RUC
    Optional<Proveedor> findByRuc(String ruc);

    // Verificar si existe un RUC (útil para validaciones)
    boolean existsByRuc(String ruc);

    // Buscar proveedores activos por nombre
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Proveedor> buscarProveedoresActivosPorNombre(@Param("nombre") String nombre);
}