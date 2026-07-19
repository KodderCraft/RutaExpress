package com.Utc.RutaExpress.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Paquete;
import com.Utc.RutaExpress.entity.Usuario;
public interface EnvioService {
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.repository.EnvioRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // Suma proyectada de "lo que se espera cobrar hoy": incluye envios en curso, no solo
    // los ya ENTREGADO (el cobro es contra entrega, asi que esto es una proyeccion, no
    // dinero confirmado). Solo se excluye CANCELADO; DEVUELTO SI cuenta hasta que el
    // repartidor lo elimine explicitamente (ver eliminarEntregado).
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

    // Registra un intento de entrega fallido. Devuelve Optional<Envio> (en vez de boolean)
    // para que el controller pueda leer el estado resultante y armar el mensaje correcto
    // (reintento vs. devolucion) sin tener que volver a consultar la base.
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
            // Se queda asignado al mismo repartidor (a diferencia de CANCELADO/PENDIENTE),
            // asi no desaparece de "Ruta de hoy" y puede reintentarse con reintentarEntrega.
            envio.setEstado(EstadoEnvio.NO_ENTREGADO);
        }

        return Optional.of(envioRepository.save(envio));
    }

    // Vuelve un NO_ENTREGADO a EN_CAMINO para que el repartidor pueda intentar entregarlo
    // de nuevo, sin pasar por "Disponibles" ni perder el conteo de intentos ya usados.
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

    // "Eliminar" hace cosas distintas segun el estado, por eso retorna el envio resultante:
    // - ENTREGADO: se borra la fila de verdad (primero su Incidencia si tenia una, porque
    //   la FK incidencias.envio_id impide borrar el envio mientras exista esa referencia).
    // - DEVUELTO: no se borra, se LIBERA — vuelve a PENDIENTE sin repartidor y con los
    //   intentos reiniciados, para que reaparezca en "Disponibles" y otro repartidor lo
    //   intente desde cero.
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

    // Panel "Ganancias": a diferencia de calcularGanadoHoy (que proyecta lo asignado hoy),
    // esto usa fechaEntrega -> solo cuenta dinero de envios que de verdad llegaron a
    // ENTREGADO (cobro confirmado), dentro de la semana actual (lunes a domingo).
    public List<Envio> listarEntregadosSemana(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime fin = inicio.plusDays(7);
        return envioRepository.findByRepartidorAndEstadoAndFechaEntregaBetween(
                repartidor, EstadoEnvio.ENTREGADO, inicio, fin);
    }

    public BigDecimal calcularGanadoSemana(Repartidor repartidor) {
        return listarEntregadosSemana(repartidor).stream()
                .map(Envio::getCostoTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void eliminarEnvio(Long id);

    Envio registrarEnvio(Usuario remitente, RegistroEnvioDTO dto);
    RegistroEnvioDTO obtenerDetalleEnvio(Long id);
    Paquete buscarPaquetePorEnvioId(Long envioId);
    
    List<Envio> listarPorCliente(Long clienteId);
    
    List<Envio> listarPorDestinatario(Long destinatarioId);
    
    Envio actualizarEnvio(Long id, RegistroEnvioDTO dto);
    // Historial de "Ganancias": las ultimas 10 entregas completadas, mas reciente primero.
    public List<Envio> listarEntregasRecientes(Repartidor repartidor) {
        return envioRepository.findTop10ByRepartidorAndEstadoOrderByFechaEntregaDesc(
                repartidor, EstadoEnvio.ENTREGADO);
    }

    public List<Envio> listarVencidosNoResueltos() {
        return envioRepository.findByEstadoNotInAndFechaLimiteBefore(
                List.of(EstadoEnvio.ENTREGADO, EstadoEnvio.CANCELADO, EstadoEnvio.DEVUELTO),
                LocalDateTime.now());
    }
}
