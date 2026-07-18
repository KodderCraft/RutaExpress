package com.Utc.RutaExpress.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.repository.EnvioRepository;

import java.math.BigDecimal;
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
    private final IncidenciaService incidenciaService;

    @Value("${app.repartidor.plazo-entrega-horas}")
    private int plazoEntregaHoras;

    @Value("${app.repartidor.max-intentos-entrega}")
    private int maxIntentosEntrega;

    public EnvioService(EnvioRepository envioRepository, IncidenciaService incidenciaService) {
        this.envioRepository = envioRepository;
        this.incidenciaService = incidenciaService;
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

    public BigDecimal calcularGanadoHoy(Repartidor repartidor) {
        return listarAsignadosHoy(repartidor).stream()
                .filter(envio -> envio.getEstado() != EstadoEnvio.CANCELADO)
                .map(Envio::getCostoTotal)
                .filter(costo -> costo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
    public Optional<Envio> marcarNoEntregado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return Optional.empty();
        }

        int intentos = (envio.getIntentosEntrega() == null ? 0 : envio.getIntentosEntrega()) + 1;
        envio.setIntentosEntrega(intentos);

        if (intentos >= maxIntentosEntrega) {
            envio.setEstado(EstadoEnvio.DEVUELTO);
        } else {
            envio.setEstado(EstadoEnvio.NO_ENTREGADO);
        }

        return Optional.of(envioRepository.save(envio));
    }

    @Transactional
    public boolean reintentarEntrega(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())
                || envio.getEstado() != EstadoEnvio.NO_ENTREGADO) {
            return false;
        }
        envio.setEstado(EstadoEnvio.EN_CAMINO);
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

    @Transactional
    public Optional<Envio> eliminarEntregado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return Optional.empty();
        }

        if (envio.getEstado() == EstadoEnvio.ENTREGADO) {
            incidenciaService.eliminarPorEnvio(envio);
            envioRepository.delete(envio);
            return Optional.of(envio);
        }

        if (envio.getEstado() == EstadoEnvio.DEVUELTO) {
            envio.setEstado(EstadoEnvio.PENDIENTE);
            envio.setRepartidor(null);
            envio.setFechaAsignacion(null);
            envio.setFechaLimite(null);
            envio.setIntentosEntrega(0);
            return Optional.of(envioRepository.save(envio));
        }

        return Optional.empty();
    }

    public List<Envio> listarVencidosNoResueltos() {
        return envioRepository.findByEstadoNotInAndFechaLimiteBefore(
                List.of(EstadoEnvio.ENTREGADO, EstadoEnvio.CANCELADO, EstadoEnvio.DEVUELTO),
                LocalDateTime.now());
    }
}
