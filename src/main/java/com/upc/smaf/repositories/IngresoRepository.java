package com.upc.smaf.repositories;

import com.upc.smaf.entities.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {

    // Para mostrar los ingresos más recientes primero en tu tabla
    List<Ingreso> findAllByOrderByFechaDesc();
}