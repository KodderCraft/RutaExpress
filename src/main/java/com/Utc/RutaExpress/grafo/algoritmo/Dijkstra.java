package com.Utc.RutaExpress.grafo.algoritmo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.Utc.RutaExpress.grafo.modelo.Arista;
import com.Utc.RutaExpress.grafo.modelo.Grafo;
import com.Utc.RutaExpress.grafo.modelo.Nodo;

public class Dijkstra {

    private Map<Nodo, Integer> distancias;
    private Map<Nodo, Nodo> anteriores;

    public Dijkstra() {
        distancias = new HashMap<>();
        anteriores = new HashMap<>();
    }

    public void calcular(Grafo grafo, Nodo origen) {

        PriorityQueue<Nodo> cola =
                new PriorityQueue<>(Comparator.comparingInt(distancias::get));

        for (Nodo nodo : grafo.getListaAdyacencia().getLista().keySet()) {

            distancias.put(nodo, Integer.MAX_VALUE);
            anteriores.put(nodo, null);

        }

        distancias.put(origen, 0);

        cola.add(origen);

        while (!cola.isEmpty()) {

            Nodo actual = cola.poll();

            for (Arista arista : grafo.obtenerAristas(actual)) {

                Nodo vecino = arista.getDestino();

                int nuevaDistancia =
                        distancias.get(actual)
                        + arista.getDistancia();

                if (nuevaDistancia < distancias.get(vecino)) {

                    distancias.put(vecino, nuevaDistancia);

                    anteriores.put(vecino, actual);

                    cola.add(vecino);

                }

            }

        }

    }

    public int obtenerDistancia(Nodo nodo) {

        return distancias.get(nodo);

    }

    public Map<Nodo, Nodo> getAnteriores() {

        return anteriores;

    }

}