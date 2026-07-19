package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
}
