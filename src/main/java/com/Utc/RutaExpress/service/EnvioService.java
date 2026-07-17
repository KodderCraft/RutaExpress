package com.Utc.RutaExpress.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.repository.EnvioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;

    public EnvioService(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }

    public Envio buscarPorId(Long id) {
        return envioRepository.findById(id).orElse(null);
    }

    public Envio guardar(Envio envio) {
        return envioRepository.save(envio);
    }

    public Envio actualizar(Long id, Envio envio) {
        envio.setId(id);
        return envioRepository.save(envio);
    }

    public void eliminar(Long id) {
        envioRepository.deleteById(id);
    }

    public List<Envio> listarDisponibles() {
        return envioRepository.findByEstadoAndRepartidorIsNull(EstadoEnvio.PENDIENTE);
    }

    @Transactional
    public boolean reclamar(Long envioId, Repartidor repartidor) {
        int filas = envioRepository.reclamar(envioId, repartidor, LocalDateTime.now());
        return filas == 1;
    }

    public long contarReclamadosHoy(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return envioRepository.countByRepartidorAndFechaAsignacionBetween(repartidor, inicio, fin);
    }

    public List<Envio> listarAsignadosHoy(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return envioRepository.findByRepartidorAndFechaAsignacionBetween(repartidor, inicio, fin);
    }
}
