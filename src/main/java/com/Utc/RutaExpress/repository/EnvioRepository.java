package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {

    List<Envio> findByEstadoAndRepartidorIsNull(EstadoEnvio estado);

    Optional<Envio> findByIdAndRepartidor(Long id, Repartidor repartidor);

    @Modifying
    @Query("UPDATE Envio e SET e.repartidor = :repartidor, e.fechaAsignacion = :fecha, e.fechaLimite = :fechaLimite " +
           "WHERE e.id = :id AND e.repartidor IS NULL AND e.estado = com.Utc.RutaExpress.entity.EstadoEnvio.PENDIENTE")
    int reclamar(@Param("id") Long id, @Param("repartidor") Repartidor repartidor,
            @Param("fecha") LocalDateTime fecha, @Param("fechaLimite") LocalDateTime fechaLimite);

    long countByRepartidorAndFechaAsignacionBetween(Repartidor repartidor, LocalDateTime inicio, LocalDateTime fin);

    List<Envio> findByRepartidorAndFechaAsignacionBetween(Repartidor repartidor, LocalDateTime inicio, LocalDateTime fin);

    List<Envio> findByEstadoNotInAndFechaLimiteBefore(List<EstadoEnvio> estadosExcluidos, LocalDateTime ahora);

    // Para el panel "Ganancias": entregas de un repartidor dentro de un rango de fechas,
    // filtrando por fechaEntrega (no fechaAsignacion) para contar solo cobros confirmados.
    List<Envio> findByRepartidorAndEstadoAndFechaEntregaBetween(
            Repartidor repartidor, EstadoEnvio estado, LocalDateTime inicio, LocalDateTime fin);

    // Ganancias cuando paga el REMITENTE (prepagado): se gana al recoger el paquete, no al
    // entregarlo, así que se filtra por fechaRecogido en vez de fechaEntrega.
    List<Envio> findByRepartidorAndPagadorAndFechaRecogidoBetween(
            Repartidor repartidor, String pagador, LocalDateTime inicio, LocalDateTime fin);

    // Ganancias cuando paga el DESTINATARIO (contra entrega): se gana al entregar, igual que
    // findByRepartidorAndEstadoAndFechaEntregaBetween pero acotado a ese pagador específico,
    // para no duplicar el conteo de los envíos que paga el remitente.
    List<Envio> findByRepartidorAndPagadorAndEstadoAndFechaEntregaBetween(
            Repartidor repartidor, String pagador, EstadoEnvio estado, LocalDateTime inicio, LocalDateTime fin);

    // Historial de "Ganancias": ultimas 10 entregas, mas reciente primero.
    List<Envio> findTop10ByRepartidorAndEstadoOrderByFechaEntregaDesc(Repartidor repartidor, EstadoEnvio estado);

    List<Envio> findByRemitenteId(Long remitenteId);
    List<Envio> findByDestinatarioId(Long destinatarioId);
}
