package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.MovimientoCajaRequestDTO;
import com.upc.smaf.dtos.response.MovimientoCajaResponseDTO;
import com.upc.smaf.entities.MovimientoCaja;
import com.upc.smaf.entities.TurnoCaja;
import com.upc.smaf.entities.CuentaBancaria;
import com.upc.smaf.repositories.MovimientoCajaRepository;
import com.upc.smaf.repositories.TurnoCajaRepository;
import com.upc.smaf.repositories.CuentaBancariaRepository;
import com.upc.smaf.serviceinterface.MovimientoCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MovimientoCajaServiceImpl implements MovimientoCajaService {

    private final MovimientoCajaRepository repository;
    private final TurnoCajaRepository turnoRepository;
    private final CuentaBancariaRepository cuentaRepository; // Para depósitos

    @Override
    @Transactional
    public MovimientoCajaResponseDTO registrarMovimiento(MovimientoCajaRequestDTO request) {

        // 1. VALIDACIÓN CRÍTICA: ¿Hay una caja abierta?
        TurnoCaja turnoActivo = turnoRepository.findByEstado("ABIERTO")
                .orElseThrow(() -> new RuntimeException("No se puede registrar. ¡Debe ABRIR LA CAJA primero!"));

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(request.getTipo());
        movimiento.setMonto(request.getMonto());
        movimiento.setMotivo(request.getMotivo());
        // Asignamos la categoría (Si viene vacía, le ponemos OTROS)
        movimiento.setCategoria(request.getCategoria() != null ? request.getCategoria() : "OTROS");
        movimiento.setResponsable(request.getResponsable());
        movimiento.setFechaHora(request.getFechaHora() != null ? request.getFechaHora() : LocalDateTime.now());

        // 2. VINCULAMOS AL TURNO ACTUAL
        movimiento.setTurnoCaja(turnoActivo);

        MovimientoCaja guardado = repository.save(movimiento);
        return mapToResponseDTO(guardado);
    }

    // ✅ NUEVA FUNCIÓN: TRANSFERIR AL BANCO
    @Transactional
    public MovimientoCajaResponseDTO depositarABanco(BigDecimal monto, Integer cuentaId, String responsable) {
        TurnoCaja turnoActivo = turnoRepository.findByEstado("ABIERTO")
                .orElseThrow(() -> new RuntimeException("Caja cerrada. Abra la caja para retirar fondos."));

        CuentaBancaria cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));

        // 1. Sale la plata de la caja física (EGRESO)
        MovimientoCaja egreso = new MovimientoCaja();
        egreso.setTipo("EGRESO");
        egreso.setCategoria("TRANSFERENCIA_BANCO");
        egreso.setMonto(monto);
        egreso.setMotivo("Depósito en cuenta: " + cuenta.getBanco() + " - " + cuenta.getNumero());
        egreso.setResponsable(responsable);
        egreso.setTurnoCaja(turnoActivo);
        egreso.setFechaHora(LocalDateTime.now());
        repository.save(egreso);

        // 2. (Opcional) Aquí sumarías el saldo a la Entidad CuentaBancaria si manejas saldos en tiempo real en esa tabla
        // cuenta.setSaldoActual(cuenta.getSaldoActual().add(monto));
        // cuentaRepository.save(cuenta);

        return mapToResponseDTO(egreso);
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Integer id) {
        // Idealmente, solo se pueden eliminar movimientos del turno actual
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public MovimientoCajaResponseDTO actualizarMovimiento(Integer id, MovimientoCajaRequestDTO request) {
        MovimientoCaja movimiento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento de caja no encontrado"));

        movimiento.setTipo(request.getTipo());
        movimiento.setMonto(request.getMonto());
        movimiento.setMotivo(request.getMotivo());
        movimiento.setCategoria(request.getCategoria() != null ? request.getCategoria() : movimiento.getCategoria());
        movimiento.setResponsable(request.getResponsable());
        if (request.getFechaHora() != null) movimiento.setFechaHora(request.getFechaHora());

        MovimientoCaja guardado = repository.save(movimiento);
        return mapToResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoCajaResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private MovimientoCajaResponseDTO mapToResponseDTO(MovimientoCaja entidad) {
        MovimientoCajaResponseDTO dto = new MovimientoCajaResponseDTO();
        dto.setId(entidad.getId());
        dto.setTipo(entidad.getTipo());
        dto.setMonto(entidad.getMonto());
        dto.setMotivo(entidad.getMotivo());
        dto.setCategoria(entidad.getCategoria());
        dto.setResponsable(entidad.getResponsable());
        dto.setFechaHora(entidad.getFechaHora());
        if(entidad.getTurnoCaja() != null) dto.setTurnoCajaId(entidad.getTurnoCaja().getId());
        return dto;
    }
}