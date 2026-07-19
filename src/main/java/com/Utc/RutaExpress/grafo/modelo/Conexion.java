package com.Utc.RutaExpress.grafo.modelo;

public class Conexion {

    private Nodo nodo;
    private double distancia;

    public Conexion(Nodo nodo, double distancia) {
        this.nodo = nodo;
        this.distancia = distancia;
    }

    public Nodo getNodo() {
        return nodo;
    }

    public double getDistancia() {
        return distancia;
    }

}