package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.Utc.RutaExpress.entity.Envio;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {

List<Envio> findByRemitenteId(Long remitenteId);
List<Envio> findByDestinatarioId(Long destinatarioId);

}