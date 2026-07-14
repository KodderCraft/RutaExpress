package com.Utc.RutaExpress.service;

import java.util.List;

import com.Utc.RutaExpress.entity.Envio;

public interface EnvioService {

    List<Envio> listarTodos();

    Envio buscarPorId(Long id);

    Envio guardar(Envio envio);

    Envio actualizar(Long id, Envio envio);

    void eliminar(Long id);

}