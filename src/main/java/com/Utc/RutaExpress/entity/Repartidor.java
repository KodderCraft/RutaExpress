package com.Utc.RutaExpress.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "repartidores")
public class Repartidor extends Usuario {

    private String placaVehiculo;
    private boolean disponible = true;

    public Repartidor() {
        super();
    }

    public Repartidor(String nombreCompleto, String correo, String cedula, String telefono, String password, String placaVehiculo) {
        super(nombreCompleto, correo, cedula, telefono, password, Rol.REPARTIDOR);
        this.placaVehiculo = placaVehiculo;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}