package com.Utc.RutaExpress.grafo.modelo;

import java.util.Objects;

public class Nodo {

    private Long id;

    private String nombre;

    private double latitud;

    private double longitud;

    public Nodo(Long id, String nombre, double latitud, double longitud) {

        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;

    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof Nodo))
            return false;

        Nodo otro = (Nodo) obj;

        return Objects.equals(id, otro.id);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}