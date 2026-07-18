package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
