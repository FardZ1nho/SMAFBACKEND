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

    List<Proveedor> findByActivoTrue();

    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Proveedor> buscarPorNombre(@Param("nombre") String nombre);

    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    // MEJORA: Verifica si existe el RUC en CUALQUIER OTRO registro que no sea el actual (ID)
    boolean existsByRucAndIdNot(String ruc, Integer id);
}