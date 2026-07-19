package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Incidencia;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    boolean existsByEnvio(Envio envio);

    List<Incidencia> findAllByOrderByIdDesc();

    // Usado antes de borrar un envio ENTREGADO (EnvioService.eliminarEntregado): la FK
    // incidencias.envio_id impide borrar el envio mientras tenga una incidencia asociada.
    void deleteByEnvio(Envio envio);
}