package com.upc.smaf.servicesimplements;

import com.upc.smaf.entities.CuentaBancaria;
import com.upc.smaf.repositories.CuentaBancariaRepository;
import com.upc.smaf.serviceinterface.CuentaBancariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaBancariaServiceImpl implements CuentaBancariaService {

    private final CuentaBancariaRepository cuentaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CuentaBancaria> listarTodas() {
        return cuentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaBancaria> listarActivas() {
        return cuentaRepository.findByActivaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaBancaria obtenerPorId(Integer id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    @Override
    @Transactional
    public CuentaBancaria guardar(CuentaBancaria cuenta) {
        // Podrías validar que no se repita el número si quisieras
        return cuentaRepository.save(cuenta);
    }

    @Override
    @Transactional
    public CuentaBancaria actualizar(Integer id, CuentaBancaria cuentaRequest) {
        CuentaBancaria cuentaDb = obtenerPorId(id);

        cuentaDb.setNombre(cuentaRequest.getNombre());
        cuentaDb.setBanco(cuentaRequest.getBanco());
        cuentaDb.setNumero(cuentaRequest.getNumero());
        cuentaDb.setMoneda(cuentaRequest.getMoneda());
        cuentaDb.setTipo(cuentaRequest.getTipo());
        cuentaDb.setTitular(cuentaRequest.getTitular());
        cuentaDb.setActiva(cuentaRequest.isActiva());

        return cuentaRepository.save(cuentaDb);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // OJO: Si ya tiene pagos asociados, esto dará error de FK en BD.
        // En ese caso, mejor usar desactivar().
        cuentaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        CuentaBancaria cuenta = obtenerPorId(id);
        cuenta.setActiva(false);
        cuentaRepository.save(cuenta);
    }
}