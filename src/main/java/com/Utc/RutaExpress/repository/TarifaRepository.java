package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Tarifa;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

}