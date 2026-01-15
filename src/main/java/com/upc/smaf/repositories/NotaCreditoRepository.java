package com.upc.smaf.repositories;

import com.upc.smaf.entities.NotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Integer> {

    // Buscar notas de una venta específica
    List<NotaCredito> findByVentaOriginalId(Integer ventaId);

    // Sumar TODO el dinero devuelto (Para tu Dashboard)
    @Query("SELECT COALESCE(SUM(nc.montoTotal), 0) FROM NotaCredito nc")
    BigDecimal sumarTotalDevoluciones();
}