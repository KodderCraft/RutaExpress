package com.Utc.RutaExpress.repository;
import com.Utc.RutaExpress.entity.Cliente;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findBynombre(String nombre);
}
