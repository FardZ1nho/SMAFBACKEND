package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.response.FinanzasDashboardResponseDTO;
import java.time.LocalDate;

public interface FinanzasService {
    // Recibe un rango de fechas. Si le mandas null, saca todo el historial o el mes actual.
    FinanzasDashboardResponseDTO obtenerDashboardFinanciero(LocalDate fechaInicio, LocalDate fechaFin);
}