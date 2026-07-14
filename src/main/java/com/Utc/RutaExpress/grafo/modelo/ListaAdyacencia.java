package com.Utc.RutaExpress.grafo.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaAdyacencia {

    private Map<Nodo, List<Arista>> lista;

    public ListaAdyacencia() {
        lista = new HashMap<>();
    }

    public void agregarNodo(Nodo nodo) {

        lista.putIfAbsent(nodo, new ArrayList<>());

    }

    public void agregarArista(Arista arista) {

        lista.get(arista.getOrigen()).add(arista);

    }

    public List<Arista> obtenerAristas(Nodo nodo) {

        return lista.get(nodo);

    }

    public Map<Nodo, List<Arista>> getLista() {

        return lista;

    }

}