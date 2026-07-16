package com.Utc.RutaExpress.service;

import java.util.List;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Repartidor;

public interface EnvioService {

    List<Envio> listarTodos();

    Envio buscarPorId(Long id);

    Envio guardar(Envio envio);

    Envio actualizar(Long id, Envio envio);

    void eliminar(Long id);

    List<Envio> listarDisponibles();

    boolean reclamar(Long envioId, Repartidor repartidor);

    long contarReclamadosHoy(Repartidor repartidor);

    List<Envio> listarAsignadosHoy(Repartidor repartidor);

}
