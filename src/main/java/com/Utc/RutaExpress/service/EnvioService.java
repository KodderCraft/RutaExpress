package com.Utc.RutaExpress.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Paquete;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.entity.Usuario;

public interface EnvioService {

    List<Envio> listarTodos();

    Envio buscarPorId(Long id);

    Envio guardar(Envio envio);

    Envio actualizar(Long id, Envio envio);

    void eliminar(Long id);

    void eliminarEnvio(Long id);

    Envio registrarEnvio(Usuario remitente, RegistroEnvioDTO dto);

    RegistroEnvioDTO obtenerDetalleEnvio(Long id);

    Paquete buscarPaquetePorEnvioId(Long envioId);

    List<Envio> listarPorCliente(Long clienteId);

    List<Envio> listarPorDestinatario(Long destinatarioId);

    Envio actualizarEnvio(Long id, RegistroEnvioDTO dto);

    Map<String, BigDecimal> obtenerTarifasPorKm();

    List<Envio> listarDisponibles();

    boolean reclamar(Long envioId, Repartidor repartidor);

    long contarReclamadosHoy(Repartidor repartidor);

    List<Envio> listarAsignadosHoy(Repartidor repartidor);

    BigDecimal calcularGanadoHoy(Repartidor repartidor);

    Optional<Envio> buscarGestionable(Long envioId, Repartidor repartidor);

    boolean marcarEntregado(Long envioId, Repartidor repartidor);

    Optional<Envio> marcarNoEntregado(Long envioId, Repartidor repartidor);

    boolean reintentarEntrega(Long envioId, Repartidor repartidor);

    boolean avanzarEstado(Long envioId, Repartidor repartidor);

    Optional<Envio> eliminarEntregado(Long envioId, Repartidor repartidor);

    List<Envio> listarEntregadosSemana(Repartidor repartidor);

    List<Envio> listarGananciasSemana(Repartidor repartidor);
    BigDecimal calcularGanadoSemana(Repartidor repartidor);

    List<Envio> listarEntregasRecientes(Repartidor repartidor);

    List<Envio> listarVencidosNoResueltos();
}
