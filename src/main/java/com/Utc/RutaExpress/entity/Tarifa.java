package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tarifas")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoServicio;

    private BigDecimal precioBase;

    private BigDecimal precioPorKg;

    private String zona;

    public Tarifa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public BigDecimal getPrecioPorKg() { return precioPorKg; }
    public void setPrecioPorKg(BigDecimal precioPorKg) { this.precioPorKg = precioPorKg; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}
