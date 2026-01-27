package com.upc.smaf.repositories;

import com.upc.smaf.entities.Tarea;
import com.upc.smaf.entities.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Integer> {
    List<Tarea> findByUsuarioAsignadoId(Long usuarioId); // Busca por el ID de Users
    List<Tarea> findByEstado(EstadoTarea estado);
}