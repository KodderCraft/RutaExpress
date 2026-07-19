package com.Utc.RutaExpress.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Direccion;
import com.Utc.RutaExpress.grafo.algoritmo.Dijkstra;
import com.Utc.RutaExpress.grafo.modelo.Conexion;
import com.Utc.RutaExpress.grafo.modelo.Grafo;
import com.Utc.RutaExpress.grafo.modelo.Nodo;
import com.Utc.RutaExpress.grafo.util.Haversine;
import com.Utc.RutaExpress.repository.DireccionRepository;

@Service
public class GrafoService {

    private final DireccionRepository direccionRepository;

    public GrafoService(DireccionRepository direccionRepository) {
        this.direccionRepository = direccionRepository;
    }

    private Grafo construirGrafo() {
    Grafo grafo = new Grafo();
    Map<Long, Nodo> nodos = new HashMap<>();
    List<Direccion> direcciones = direccionRepository.findAll();

    for (Direccion d : direcciones) {
        Nodo nodo = new Nodo(
                d.getId(),
                d.getDireccionTexto(),
                d.getLatitud(),
                d.getLongitud());

        grafo.agregarNodo(nodo);
        nodos.put(d.getId(), nodo);
    }
    
    List<Nodo> listaNodos = List.copyOf(nodos.values());
    int MAX_VECINOS = 8;

    for (Nodo origen : listaNodos) {
        List<Conexion> conexiones = new ArrayList<>();

        for (Nodo destino : listaNodos) {
            if (origen.equals(destino)) {
                continue;
            }

            double distancia = Haversine.calcular(
                    origen.getLatitud(),
                    origen.getLongitud(),
                    destino.getLatitud(),
                    destino.getLongitud());

            conexiones.add(new Conexion(destino, distancia));
        }

        conexiones.sort(Comparator.comparingDouble((Conexion c) -> c.getDistancia()));

        for (int i = 0; i < Math.min(MAX_VECINOS, conexiones.size()); i++) {
            Conexion conexion = conexiones.get(i);
            grafo.agregarArista(
                    origen,
                    conexion.getNodo(),
                    (int) Math.round(conexion.getDistancia()));
        }
    } 
    return grafo;
}

    public List<Nodo> calcularRuta(Long origenId, List<Long> paradas, Long destinoId) {

    Grafo grafo = construirGrafo();

    Map<Long, Nodo> mapaNodos = new HashMap<>();

    for (Nodo nodo : grafo.getListaAdyacencia().getLista().keySet()) {
        mapaNodos.put(nodo.getId(), nodo);
    }

    List<Long> recorrido = new ArrayList<>();

    recorrido.add(origenId);

    if (paradas != null && !paradas.isEmpty()) {
        recorrido.addAll(paradas);
    }

    recorrido.add(destinoId);

    List<Nodo> caminoCompleto = new ArrayList<>();

    for (int i = 0; i < recorrido.size() - 1; i++) {

        Nodo origen = mapaNodos.get(recorrido.get(i));

        Nodo destino = mapaNodos.get(recorrido.get(i + 1));

        if (origen == null || destino == null) {
            throw new RuntimeException("Ciudad no encontrada.");
        }

        Dijkstra dijkstra = new Dijkstra();

        dijkstra.calcular(grafo, origen);

        List<Nodo> tramo = dijkstra.obtenerCamino(destino);

        if (tramo.isEmpty()) {
            continue;
        }

        // Evita repetir la ciudad donde termina un tramo
        // y comienza el siguiente.
        if (!caminoCompleto.isEmpty()) {
            tramo.remove(0);
        }

        caminoCompleto.addAll(tramo);

    }

    return caminoCompleto;
}

    public List<Direccion> obtenerCiudades() {

        return direccionRepository.findAll();

    }
}