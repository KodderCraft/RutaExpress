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

    void deleteByEnvio(Envio envio);
}