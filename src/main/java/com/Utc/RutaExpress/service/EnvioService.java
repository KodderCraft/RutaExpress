package com.Utc.RutaExpress.service;

import java.util.List;

import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Paquete;
import com.Utc.RutaExpress.entity.Usuario;
public interface EnvioService {

    List<Envio> listarTodos();

    Envio buscarPorId(Long id);

    Envio guardar(Envio envio);

    Envio actualizar(Long id, Envio envio);

    void eliminarEnvio(Long id);

    Envio registrarEnvio(Usuario remitente, RegistroEnvioDTO dto);
    RegistroEnvioDTO obtenerDetalleEnvio(Long id);
    Paquete buscarPaquetePorEnvioId(Long envioId);
    
    List<Envio> listarPorCliente(Long clienteId);
    
    List<Envio> listarPorDestinatario(Long destinatarioId);
    
    Envio actualizarEnvio(Long id, RegistroEnvioDTO dto);

}