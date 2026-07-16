package com.Utc.RutaExpress.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.service.EnvioService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvioServiceImpl implements EnvioService {

    private final EnvioRepository envioRepository;

    public EnvioServiceImpl(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    @Override
    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }

    @Override
    public Envio buscarPorId(Long id) {
        return envioRepository.findById(id).orElse(null);
    }

    @Override
    public Envio guardar(Envio envio) {
        return envioRepository.save(envio);
    }

    @Override
    public Envio actualizar(Long id, Envio envio) {
        envio.setId(id);
        return envioRepository.save(envio);
    }

    @Override
    public void eliminar(Long id) {
        envioRepository.deleteById(id);
    }

    @Override
    public List<Envio> listarDisponibles() {
        return envioRepository.findByEstadoAndRepartidorIsNull(EstadoEnvio.PENDIENTE);
    }

    @Override
    @Transactional
    public boolean reclamar(Long envioId, Repartidor repartidor) {
        int filas = envioRepository.reclamar(envioId, repartidor, LocalDateTime.now());
        return filas == 1;
    }

    @Override
    public long contarReclamadosHoy(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return envioRepository.countByRepartidorAndFechaAsignacionBetween(repartidor, inicio, fin);
    }

    @Override
    public List<Envio> listarAsignadosHoy(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return envioRepository.findByRepartidorAndFechaAsignacionBetween(repartidor, inicio, fin);
    }
}
