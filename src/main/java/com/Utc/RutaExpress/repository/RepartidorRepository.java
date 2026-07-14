package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Repartidor;

@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

}