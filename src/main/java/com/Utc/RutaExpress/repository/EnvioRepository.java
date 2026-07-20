package com.Utc.RutaExpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    List<Envio> findByDestinatarioId(Long destinatarioId);
    List<Envio> findByRemitenteId(Long remitenteId);
    Optional<Envio> findByIdAndRepartidor(Long id, Repartidor repartidor);

    
    @Modifying
    @Query("UPDATE Envio e SET e.repartidor = :repartidor, e.fechaAsignacion = :fecha, e.fechaLimite = :fechaLimite " +
           "WHERE e.id = :id AND e.repartidor IS NULL AND e.estado = com.Utc.RutaExpress.entity.EstadoEnvio.PENDIENTE")
    int reclamar(@Param("id") Long id, @Param("repartidor") Repartidor repartidor,
            @Param("fecha") LocalDateTime fecha, @Param("fechaLimite") LocalDateTime fechaLimite);
    List<Envio> findByEstadoAndRepartidorIsNull(EstadoEnvio estado);


    long countByRepartidorAndFechaAsignacionBetween(Repartidor repartidor, LocalDateTime inicio, LocalDateTime fin);

    List<Envio> findByRepartidorAndPagadorAndFechaRecogidoBetween(
        Repartidor repartidor, String pagador, LocalDateTime inicio, LocalDateTime fin
    );

    // 2. Y DE PASO, AGREGA TAMBIÉN LA DEL DESTINATARIO (que la vas a necesitar abajo):
    List<Envio> findByRepartidorAndPagadorAndEstadoAndFechaEntregaBetween(
        Repartidor repartidor, String pagador, EstadoEnvio estado, LocalDateTime inicio, LocalDateTime fin
    );

    // findByRepartidorAndPagadorAndFechaRecogidoBetween

    List<Envio> findByRepartidorAndFechaAsignacionBetween(Repartidor repartidor, LocalDateTime inicio, LocalDateTime fin);

    List<Envio> findByEstadoNotInAndFechaLimiteBefore(List<EstadoEnvio> estadosExcluidos, LocalDateTime ahora);

    // Para el panel "Ganancias": entregas de un repartidor dentro de un rango de fechas,
    // filtrando por fechaEntrega (no fechaAsignacion) para contar solo cobros confirmados.
    List<Envio> findByRepartidorAndEstadoAndFechaEntregaBetween(
            Repartidor repartidor, EstadoEnvio estado, LocalDateTime inicio, LocalDateTime fin);

    // Historial de "Ganancias": ultimas 10 entregas, mas reciente primero.
    List<Envio> findTop10ByRepartidorAndEstadoOrderByFechaEntregaDesc(Repartidor repartidor, EstadoEnvio estado);

}
