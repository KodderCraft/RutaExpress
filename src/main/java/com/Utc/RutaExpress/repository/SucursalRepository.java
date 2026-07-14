package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Sucursal;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

}