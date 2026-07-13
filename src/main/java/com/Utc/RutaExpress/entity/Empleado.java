package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "empleados")
public class Empleado extends Usuario {

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    public Empleado() {
        super();
    }

    public Empleado(String nombreCompleto, String correo, String cedula, String telefono, String password, Sucursal sucursal) {
        super(nombreCompleto, correo, cedula, telefono, password, Rol.EMPLEADO);
        this.sucursal = sucursal;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}