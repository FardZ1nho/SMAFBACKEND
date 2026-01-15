package com.upc.smaf.repositories;

import com.upc.smaf.entities.CompraDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Integer> {

    // Obtener todos los productos de una compra específica
    List<CompraDetalle> findByCompraId(Integer compraId);

    // Buscar ingresos a un almacén específico a través de compras
    List<CompraDetalle> findByAlmacenId(Integer almacenId);
}