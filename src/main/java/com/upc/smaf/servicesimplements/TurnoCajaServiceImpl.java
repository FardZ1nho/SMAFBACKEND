package com.upc.smaf.servicesimplements;

import com.upc.smaf.entities.TurnoCaja;
import com.upc.smaf.entities.MovimientoCaja;
import com.upc.smaf.repositories.TurnoCajaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TurnoCajaServiceImpl {

    private final TurnoCajaRepository turnoRepository;

    @Transactional
    public TurnoCaja abrirCaja(BigDecimal saldoInicial, String responsable) {
        if (turnoRepository.findByEstado("ABIERTO").isPresent()) {
            throw new RuntimeException("Ya existe un turno de caja abierto.");
        }

        TurnoCaja turno = new TurnoCaja();
        turno.setEstado("ABIERTO");
        turno.setFechaApertura(LocalDateTime.now());
        turno.setSaldoInicial(saldoInicial);
        turno.setResponsable(responsable);

        return turnoRepository.save(turno);
    }

    @Transactional
    public TurnoCaja cerrarCaja(BigDecimal saldoFisicoContado) {
        TurnoCaja turno = turnoRepository.findByEstado("ABIERTO")
                .orElseThrow(() -> new RuntimeException("No hay caja abierta para cerrar."));

        // 1. Calculamos cuánto dinero DEBERÍA haber según el sistema
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;

        if (turno.getMovimientos() != null) {
            for (MovimientoCaja mov : turno.getMovimientos()) {
                if ("INGRESO".equals(mov.getTipo())) totalIngresos = totalIngresos.add(mov.getMonto());
                if ("EGRESO".equals(mov.getTipo())) totalEgresos = totalEgresos.add(mov.getMonto());
            }
        }

        BigDecimal saldoCalculado = turno.getSaldoInicial().add(totalIngresos).subtract(totalEgresos);

        // 2. Registramos el cierre
        turno.setEstado("CERRADO");
        turno.setFechaCierre(LocalDateTime.now());
        turno.setSaldoFinalCalculado(saldoCalculado);
        turno.setSaldoFinalFisico(saldoFisicoContado);

        // 3. Calculamos el descuadre (Positivo = Sobra plata, Negativo = Falta plata)
        turno.setDescuadre(saldoFisicoContado.subtract(saldoCalculado));

        return turnoRepository.save(turno);
    }
}