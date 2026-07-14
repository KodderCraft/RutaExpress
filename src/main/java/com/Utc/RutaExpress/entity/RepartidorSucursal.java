package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;

// Tabla repartidor_sucursal: que sucursales cubre cada repartidor (relacion muchos a muchos)
@Entity
@Table(name = "repartidor_sucursal", uniqueConstraints = @UniqueConstraint(columnNames = {"repartidorId", "sucursalId"}))
public class RepartidorSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long repartidorId;

    @Column(nullable = false)
    private Long sucursalId;

    // Zona/barrio dentro de la sucursal que cubre este repartidor
    private String zona;

    public RepartidorSucursal() {
    }

    public RepartidorSucursal(Long repartidorId, Long sucursalId, String zona) {
        this.repartidorId = repartidorId;
        this.sucursalId = sucursalId;
        this.zona = zona;
    }

    public Long getId() {
        return id;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}
