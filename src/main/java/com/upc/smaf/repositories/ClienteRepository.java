package com.upc.smaf.repositories;

import com.upc.smaf.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Búsqueda por documento
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    Optional<Cliente> findByNumeroDocumentoAndActivoTrue(String numeroDocumento);
    boolean existsByNumeroDocumento(String numeroDocumento);

    // Búsqueda por nombre
    List<Cliente> findByNombreCompletoContainingIgnoreCase(String nombre);
    List<Cliente> findByNombreCompletoContainingIgnoreCaseAndActivoTrue(String nombre);

    // Filtros por estado
    List<Cliente> findByActivoTrue();
    List<Cliente> findByActivoFalse();

    // Filtros por tipo de cliente
    List<Cliente> findByTipoClienteAndActivoTrue(String tipoCliente);

    // Búsqueda por email
    Optional<Cliente> findByEmail(String email);

    // Búsqueda combinada
    @Query("SELECT c FROM Cliente c WHERE " +
            "(LOWER(c.nombreCompleto) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.numeroDocumento) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "c.activo = true")
    List<Cliente> buscarClientes(String termino);

    // ========== AGREGAR ESTE MÉTODO AL FINAL DE ClienteRepository ==========

    /**
     * Cuenta la cantidad de clientes activos
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.activo = true")
    Long contarClientesActivos();

    // Ordenamientos
    List<Cliente> findByActivoTrueOrderByNombreCompletoAsc();
    List<Cliente> findByActivoTrueOrderByFechaCreacionDesc();
}