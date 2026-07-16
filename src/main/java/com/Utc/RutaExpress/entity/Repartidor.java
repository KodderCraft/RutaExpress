package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "repartidores")
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private Boolean disponible;

    private String placa;

    private String vehiculoTipo;

    private String zona;

    public Repartidor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getVehiculoTipo() { return vehiculoTipo; }
    public void setVehiculoTipo(String vehiculoTipo) { this.vehiculoTipo = vehiculoTipo; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}
