package com.Utc.RutaExpress.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    public Cliente() {
        super();
    }

    public Cliente(String nombreCompleto, String correo, String cedula, String telefono, String password) {
        super(nombreCompleto, correo, cedula, telefono, password, Rol.CLIENTE);
    }
}