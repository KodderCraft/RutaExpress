package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Tabla tarifas: precios base por tipo de servicio, usados para calcular el costo de un envio
@Entity
@Table(name = "tarifas")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoServicio tipoServicio;

    // Costo fijo que se cobra sin importar el peso
    @Column(nullable = false)
    private BigDecimal costoBase;

    // Costo que se suma por cada kilo adicional del envio
    @Column(nullable = false)
    private BigDecimal costoKgAdicional;

    public Tarifa() {
    }

    public Tarifa(TipoServicio tipoServicio, BigDecimal costoBase, BigDecimal costoKgAdicional) {
        this.tipoServicio = tipoServicio;
        this.costoBase = costoBase;
        this.costoKgAdicional = costoKgAdicional;
    }

    public Long getId() {
        return id;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public void setCostoBase(BigDecimal costoBase) {
        this.costoBase = costoBase;
    }

    public BigDecimal getCostoKgAdicional() {
        return costoKgAdicional;
    }

    public void setCostoKgAdicional(BigDecimal costoKgAdicional) {
        this.costoKgAdicional = costoKgAdicional;
    }
}
