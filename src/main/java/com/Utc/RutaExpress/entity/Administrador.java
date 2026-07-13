package com.Utc.RutaExpress.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "administradores")
public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(String nombreCompleto, String correo, String cedula, String telefono, String password) {
        super(nombreCompleto, correo, cedula, telefono, password, Rol.ADMINISTRADOR);
    }
}