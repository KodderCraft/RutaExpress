package com.Utc.RutaExpress.service;

import java.util.List;
import com.Utc.RutaExpress.entity.Tarifa;

public interface TarifaService {

    List<Tarifa> listarTodos();

    Tarifa buscarPorId(Long id);

    Tarifa guardar(Tarifa tarifa);

    Tarifa actualizar(Long id, Tarifa tarifa);

    void eliminar(Long id);

}