package com.Utc.RutaExpress.grafo.modelo;

public class Arista {

    private final Nodo origen;
    private final Nodo destino;
    private final int distancia;

    public Arista(Nodo origen, Nodo destino, int distancia) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
    }

    public Nodo getOrigen() {
        return origen;
    }

    public Nodo getDestino() {
        return destino;
    }

    public int getDistancia() {
        return distancia;
    }

    @Override
    public String toString() {
        return origen + " -> " + destino + " (" + distancia + " km)";
    }
}