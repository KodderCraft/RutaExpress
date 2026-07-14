package com.Utc.RutaExpress.grafo.modelo;

import java.util.List;

public class Grafo {

    private ListaAdyacencia listaAdyacencia;

    public Grafo() {
        this.listaAdyacencia = new ListaAdyacencia();
    }

    public void agregarNodo(Nodo nodo) {
        listaAdyacencia.agregarNodo(nodo);
    }

    public void agregarArista(Nodo origen, Nodo destino, int distancia) {

        Arista arista = new Arista(origen, destino, distancia);

        listaAdyacencia.agregarArista(arista);
    }

    public List<Arista> obtenerAristas(Nodo nodo) {
        return listaAdyacencia.obtenerAristas(nodo);
    }

    public ListaAdyacencia getListaAdyacencia() {
        return listaAdyacencia;
    }

}