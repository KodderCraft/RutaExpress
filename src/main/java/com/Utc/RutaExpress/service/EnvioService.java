package com.Utc.RutaExpress.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.repository.EnvioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EnvioService {

    private static final Map<EstadoEnvio, EstadoEnvio> SIGUIENTE_ESTADO = Map.of(
            EstadoEnvio.PENDIENTE, EstadoEnvio.RECOGIDO,
            EstadoEnvio.RECOGIDO, EstadoEnvio.EN_CAMINO);

    private final EnvioRepository envioRepository;

    @Value("${app.repartidor.plazo-entrega-horas}")
    private int plazoEntregaHoras;

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
        LocalDateTime ahora = LocalDateTime.now();
        int filas = envioRepository.reclamar(envioId, repartidor, ahora, ahora.plusHours(plazoEntregaHoras));
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

    public Optional<Envio> buscarGestionable(Long envioId, Repartidor repartidor) {
        return envioRepository.findByIdAndRepartidor(envioId, repartidor);
    }

    @Transactional
    public boolean marcarEntregado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return false;
        }
        envio.setEstado(EstadoEnvio.ENTREGADO);
        envio.setFechaEntrega(LocalDateTime.now());
        envioRepository.save(envio);
        return true;
    }

    @Transactional
    public boolean avanzarEstado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return false;
        }
        EstadoEnvio siguiente = SIGUIENTE_ESTADO.get(envio.getEstado());
        if (siguiente == null) {
            return false;
        }
        envio.setEstado(siguiente);
        envioRepository.save(envio);
        return true;
    }

    public List<Envio> listarVencidosNoResueltos() {
        return envioRepository.findByEstadoNotInAndFechaLimiteBefore(
                List.of(EstadoEnvio.ENTREGADO, EstadoEnvio.CANCELADO), LocalDateTime.now());
    }
}
