package com.Utc.RutaExpress.service;

import java.util.List;
import com.Utc.RutaExpress.entity.Pago;

public interface PagoService {

    List<Pago> listarTodos();

    Pago buscarPorId(Long id);

    Pago guardar(Pago pago);

    Pago actualizar(Long id, Pago pago);

    void eliminar(Long id);

}