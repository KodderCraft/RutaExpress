package com.Utc.RutaExpress.service;

import java.util.List;
import com.Utc.RutaExpress.entity.Incidencia;

public interface IncidenciaService {

    List<Incidencia> listarTodos();

    Incidencia buscarPorId(Long id);

    Incidencia guardar(Incidencia incidencia);

    Incidencia actualizar(Long id, Incidencia incidencia);

    void eliminar(Long id);

}