package com.Utc.RutaExpress.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Paquete;

@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, Long> {
    Optional<Paquete> findByEnvioId(Long envioId);
}

