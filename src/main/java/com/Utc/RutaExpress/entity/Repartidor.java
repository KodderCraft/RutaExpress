package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;

// Tabla repartidores: datos extra que solo aplican a un usuario con rol REPARTIDOR
@Entity
@Table(name = "repartidores")
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true asegura que un mismo usuario no tenga mas de una fila de repartidor
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    private String vehiculoTipo;

    private String placa;

    // Zona/barrio que cubre este repartidor
    private String zona;

    @Column(nullable = false)
    private boolean disponible = true;

    public Repartidor() {
    }

    public Repartidor(Usuario usuario, String vehiculoTipo, String placa, String zona) {
        this.usuario = usuario;
        this.vehiculoTipo = vehiculoTipo;
        this.placa = placa;
        this.zona = zona;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getVehiculoTipo() {
        return vehiculoTipo;
    }

    public void setVehiculoTipo(String vehiculoTipo) {
        this.vehiculoTipo = vehiculoTipo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
