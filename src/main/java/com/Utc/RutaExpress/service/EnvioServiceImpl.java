package com.Utc.RutaExpress.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.entity.Direccion;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Paquete;
import com.Utc.RutaExpress.entity.Rol;
import com.Utc.RutaExpress.entity.Tarifa;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.repository.DireccionRepository;
import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.repository.PaqueteRepository;
import com.Utc.RutaExpress.repository.TarifaRepository;
import com.Utc.RutaExpress.repository.UsuarioRepository;

@Service
public class EnvioServiceImpl implements EnvioService {

    private final EnvioRepository envioRepository;
    private final DireccionRepository direccionRepository;
    private final PaqueteRepository paqueteRepository;
    private final TarifaRepository tarifaRepository;
    private final UsuarioRepository usuarioRepository;

    public EnvioServiceImpl(EnvioRepository envioRepository,
                            DireccionRepository direccionRepository,
                            PaqueteRepository paqueteRepository,
                            TarifaRepository tarifaRepository,UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.direccionRepository = direccionRepository;
        this.paqueteRepository = paqueteRepository;
        this.tarifaRepository = tarifaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }

    @Override
    public Envio buscarPorId(Long id) {
        return envioRepository.findById(id).orElse(null);
    }

    @Override
    public Envio guardar(Envio envio) {
        return envioRepository.save(envio);
    }

    @Override
    public Envio actualizar(Long id, Envio envio) {
        Envio existente = buscarPorId(id);
        if (existente == null) {
            return null;
        }
        envio.setId(id);
        return envioRepository.save(envio);
    }

    @Override
    public void eliminarEnvio(Long id) {
Envio envio = envioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("El envío no existe."));

            // 1. Validamos que no tenga un repartidor real asignado
            if (envio.getRepartidor() != null) {
                throw new IllegalArgumentException("No se puede eliminar un envío que ya tiene un repartidor asignado.");
            }

            // 2. Buscamos el paquete y lo borramos primero
            paqueteRepository.findByEnvioId(id).ifPresent(paquete -> {
                paquete.setEnvio(null); // Rompemos la relación en memoria
                paqueteRepository.saveAndFlush(paquete); // Actualizamos la BD de inmediato
                paqueteRepository.delete(paquete); // Borramos el paquete
            });

            // 3. Ahora sí, borramos el envío (las direcciones y usuarios no darán problema)
            envioRepository.flush();
            envioRepository.delete(envio);
    }

    @Override
    public Envio registrarEnvio(Usuario remitente, RegistroEnvioDTO dto) {
        validarDatosObligatorios(remitente, dto);

        Direccion recogida = crearDireccion(dto.getDireccionRecogida(), dto.getLatitudRecogida(), dto.getLongitudRecogida());
        System.out.println("Direccion de recogida creada: " + recogida.getDireccionTexto() + " (Lat: " + recogida.getLatitud() + ", Lon: " + recogida.getLongitud() + ")");
        Direccion entrega = crearDireccion(dto.getDireccionEntrega(), dto.getLatitudEntrega(), dto.getLongitudEntrega());
        System.out.println("Direccion de entrega creada: " + entrega.getDireccionTexto() + " (Lat: " + entrega.getLatitud() + ", Lon: " + entrega.getLongitud() + ")");

        Usuario destinatario = obtenerOCrearDestinatario(dto.getNombreDestinatario(), dto.getTelefonoDestinatario());
        Tarifa tarifa = obtenerTarifaPorTipo(dto.getTipoServicio());

        Envio envio = new Envio();
        envio.setCodigoGuia(generarCodigoGuia());
        envio.setRemitente(remitente);
        envio.setDestinatario(destinatario);
        envio.setDireccionRecogida(recogida);
        envio.setDireccionEntrega(entrega);
        envio.setTarifa(tarifa);
        envio.setTipoServicio(dto.getTipoServicio());
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setCostoTotal(dto.getCostoTotal() != null ? dto.getCostoTotal() : (tarifa != null ? tarifa.getPrecioBase() : BigDecimal.ZERO));
        envio.setDistanciaKm(dto.getDistanciaKm() != null ? dto.getDistanciaKm() : 0.0);
        envio.setTiempoEstimadoMin(dto.getTiempoEstimadoMin() != null ? dto.getTiempoEstimadoMin() : 30);
        envio.setFechaRegistro(LocalDateTime.now());

        Envio guardado = envioRepository.save(envio);

        Paquete paquete = new Paquete();
        paquete.setEnvio(guardado);
        paquete.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion() : "");
        paquete.setPeso(dto.getPeso() != null ? dto.getPeso() : 0.0);
        paquete.setAlto(dto.getAlto() != null ? dto.getAlto() : 0.0);
        paquete.setAncho(dto.getAncho() != null ? dto.getAncho() : 0.0);
        paquete.setLargo(dto.getLargo() != null ? dto.getLargo() : 0.0);
        paquete.setFragil(Boolean.TRUE.equals(dto.getFragil()));
        paquete.setTipo(!esVacio(dto.getTipo()) ? dto.getTipo() : "otro");
        paquete.setValorDeclarado(dto.getValorDeclarado() != null ? dto.getValorDeclarado() : BigDecimal.ZERO);
        paqueteRepository.save(paquete);

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Envio> listarPorCliente(Long clienteId) {
        return envioRepository.findByRemitenteId(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Envio> listarPorDestinatario(Long destinatarioId) {
        return envioRepository.findByDestinatarioId(destinatarioId);
    }

    public Paquete buscarPaquetePorEnvioId(Long envioId) {
        return paqueteRepository.findByEnvioId(envioId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroEnvioDTO obtenerDetalleEnvio(Long id) {
        Envio envio = envioRepository.findById(id).orElse(null);
        if (envio == null) {
            return null;
        }

        Paquete paquete = paqueteRepository.findByEnvioId(envio.getId()).orElse(null);
        RegistroEnvioDTO dto = new RegistroEnvioDTO();
        dto.setId(envio.getId());
        if (envio.getDireccionRecogida() != null) {
            dto.setDireccionRecogida(envio.getDireccionRecogida().getDireccionTexto());
            dto.setLatitudRecogida(envio.getDireccionRecogida().getLatitud());
            dto.setLongitudRecogida(envio.getDireccionRecogida().getLongitud());
        }
        if (envio.getDireccionEntrega() != null) {
            dto.setDireccionEntrega(envio.getDireccionEntrega().getDireccionTexto());
            dto.setLatitudEntrega(envio.getDireccionEntrega().getLatitud());
            dto.setLongitudEntrega(envio.getDireccionEntrega().getLongitud());
        }
        if (envio.getDestinatario() != null) {
            dto.setNombreDestinatario(envio.getDestinatario().getNombre());
            dto.setTelefonoDestinatario(envio.getDestinatario().getTelefono());
        }
        dto.setTipoServicio(envio.getTipoServicio());
        dto.setInstrucciones(envio.getTipoServicio());
        dto.setValorDeclarado(envio.getCostoTotal());
        dto.setDistanciaKm(envio.getDistanciaKm());
        dto.setTiempoEstimadoMin(envio.getTiempoEstimadoMin());
        dto.setCostoTotal(envio.getCostoTotal());

        if (paquete != null) {
            dto.setDescripcion(paquete.getDescripcion());
            dto.setPeso(paquete.getPeso());
            dto.setAlto(paquete.getAlto());
            dto.setAncho(paquete.getAncho());
            dto.setLargo(paquete.getLargo());
            dto.setFragil(paquete.getFragil());
            dto.setTipo(paquete.getTipo());
            dto.setValorDeclarado(paquete.getValorDeclarado());
        }

        return dto;
    }

    @Override
    @Transactional
    public Envio actualizarEnvio(Long id, RegistroEnvioDTO dto) {
        Envio envio = envioRepository.findById(id).orElse(null);
        if (envio == null) {
            throw new IllegalArgumentException("El envío no fue encontrado.");
        }

        if (envio.getRepartidor() != null) {
            throw new IllegalArgumentException("No se puede editar un envío que ya tiene repartidor asignado.");
        }

        if (envio.getDireccionRecogida() != null) {
            Direccion recogida = envio.getDireccionRecogida();
            recogida.setDireccionTexto(dto.getDireccionRecogida());
            recogida.setLatitud(dto.getLatitudRecogida());
            recogida.setLongitud(dto.getLongitudRecogida());
            direccionRepository.save(recogida);
            envio.setDireccionRecogida(recogida);
        }

        if (envio.getDireccionEntrega() != null) {
            Direccion entrega = envio.getDireccionEntrega();
            entrega.setDireccionTexto(dto.getDireccionEntrega());
            entrega.setLatitud(dto.getLatitudEntrega());
            entrega.setLongitud(dto.getLongitudEntrega());
            direccionRepository.save(entrega);
            envio.setDireccionEntrega(entrega);
        }

        if (envio.getDestinatario() != null) {
            Usuario destinatario = envio.getDestinatario();
            destinatario.setNombre(dto.getNombreDestinatario());
            destinatario.setTelefono(dto.getTelefonoDestinatario());
            usuarioRepository.save(destinatario);
            envio.setDestinatario(destinatario);
        }

        envio.setTipoServicio(dto.getTipoServicio());
        envio.setCostoTotal(dto.getCostoTotal() != null ? dto.getCostoTotal() : envio.getCostoTotal());
        envio.setDistanciaKm(dto.getDistanciaKm() != null ? dto.getDistanciaKm() : envio.getDistanciaKm());
        envio.setTiempoEstimadoMin(dto.getTiempoEstimadoMin() != null ? dto.getTiempoEstimadoMin() : envio.getTiempoEstimadoMin());

        Envio actualizado = envioRepository.save(envio);

        Paquete paquete = paqueteRepository.findByEnvioId(envio.getId()).orElse(new Paquete());
        paquete.setEnvio(actualizado);
        paquete.setDescripcion(dto.getDescripcion());
        paquete.setPeso(dto.getPeso());
        paquete.setAlto(dto.getAlto());
        paquete.setAncho(dto.getAncho());
        paquete.setLargo(dto.getLargo());
        paquete.setFragil(Boolean.TRUE.equals(dto.getFragil()));
        paquete.setTipo(dto.getTipo());
        paquete.setValorDeclarado(dto.getValorDeclarado());
        paqueteRepository.save(paquete);

        return actualizado;
    }

    private Direccion crearDireccion(String texto, Double latitud, Double longitud) {
        Direccion direccion = new Direccion();
        direccion.setDireccionTexto(texto == null || texto.isBlank() ? "Sin especificar" : texto);
        direccion.setLatitud(latitud);
        direccion.setLongitud(longitud);
        return direccionRepository.save(direccion);
    }

    private Usuario obtenerOCrearDestinatario(String nombre, String telefono) {
        Usuario existente = usuarioRepository.findByNombreAndTelefono(nombre, telefono).orElse(null);
        if (existente != null) {
            return existente;
        }

        Usuario destinatario = new Usuario();
        destinatario.setNombre(nombre);
        destinatario.setEmail(generarEmailTemporal(nombre));
        destinatario.setPassword("temp123");
        destinatario.setTelefono(telefono);
        destinatario.setRol(Rol.CLIENTE);
        return usuarioRepository.save(destinatario);
    }

    private Tarifa obtenerTarifaPorTipo(String tipoServicio) {
        return tarifaRepository.findFirstByTipoServicioIgnoreCase(tipoServicio)
                .orElseGet(() -> {
                    Tarifa tarifa = new Tarifa();
                    tarifa.setTipoServicio(tipoServicio == null ? "estandar" : tipoServicio);
                    tarifa.setPrecioBase(new BigDecimal("5.00"));
                    tarifa.setPrecioPorKg(new BigDecimal("1.50"));
                    tarifa.setZona("General");
                    return tarifaRepository.save(tarifa);
                });
    }

    private boolean tieneDatosDePaquete(RegistroEnvioDTO dto) {
        return !esVacio(dto.getDescripcion()) || dto.getPeso() != null || dto.getAlto() != null
                || dto.getAncho() != null || dto.getLargo() != null || !esVacio(dto.getTipo())
                || dto.getValorDeclarado() != null || Boolean.TRUE.equals(dto.getFragil());
    }

    private void validarDatosObligatorios(Usuario remitente, RegistroEnvioDTO dto) {
        if (remitente == null) {
            throw new IllegalArgumentException("El remitente es obligatorio");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del envío son obligatorios");
        }
        if (esVacio(dto.getDireccionRecogida()) || dto.getLatitudRecogida() == null || dto.getLongitudRecogida() == null) {
            throw new IllegalArgumentException("La dirección de recogida y sus coordenadas son obligatorias");
        }
        if (esVacio(dto.getDireccionEntrega()) || dto.getLatitudEntrega() == null || dto.getLongitudEntrega() == null) {
            throw new IllegalArgumentException("La dirección de entrega y sus coordenadas son obligatorias");
        }
        if (esVacio(dto.getDescripcion())) {
            throw new IllegalArgumentException("El paquete y la descripción del paquete son obligatorias");
        }
        if (esVacio(dto.getNombreDestinatario()) || esVacio(dto.getTelefonoDestinatario())) {
            throw new IllegalArgumentException("El nombre y teléfono del destinatario son obligatorios");
        }
        if (esVacio(dto.getTipoServicio())) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio");
        }
        if (esVacio(dto.getTipo()) || dto.getPeso() == null || dto.getPeso() <= 0 || dto.getAlto() == null || dto.getAlto() <= 0) {
            throw new IllegalArgumentException("Los campos del paquete obligatorios son: tipo, peso y alto con valores válidos");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String generarEmailTemporal(String nombre) {
        String slug = nombre == null ? "user" : nombre.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String rand = UUID.randomUUID().toString().substring(0,6);
        return String.format("%s+%s@temp.local", slug.isEmpty() ? "user" : slug, rand);
    }

    private String generarCodigoGuia() {
        return "RX-" + UUID.randomUUID().toString().substring(0, 3).toUpperCase();
    }
}
