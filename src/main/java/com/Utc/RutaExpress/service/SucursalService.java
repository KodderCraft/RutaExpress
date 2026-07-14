package com.Utc.RutaExpress.service;

import java.util.List;
import com.Utc.RutaExpress.entity.Sucursal;

public interface SucursalService {

    List<Sucursal> listarTodos();

    Sucursal buscarPorId(Long id);

    Sucursal guardar(Sucursal sucursal);

    Sucursal actualizar(Long id, Sucursal sucursal);

    void eliminar(Long id);

}