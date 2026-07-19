package com.Utc.RutaExpress.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.entity.Direccion;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Paquete;
import com.Utc.RutaExpress.entity.Repartidor;
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

    private static final Map<EstadoEnvio, EstadoEnvio> SIGUIENTE_ESTADO = Map.of(
            EstadoEnvio.PENDIENTE, EstadoEnvio.RECOGIDO,
            EstadoEnvio.RECOGIDO, EstadoEnvio.EN_CAMINO);

    private final EnvioRepository envioRepository;
    private final DireccionRepository direccionRepository;
    private final PaqueteRepository paqueteRepository;
    private final TarifaRepository tarifaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaService incidenciaService;

    @Value("${app.repartidor.plazo-entrega-horas}")
    private int plazoEntregaHoras;

    @Value("${app.repartidor.max-intentos-entrega}")
    private int maxIntentosEntrega;

    public EnvioServiceImpl(EnvioRepository envioRepository,
                            DireccionRepository direccionRepository,
                            PaqueteRepository paqueteRepository,
                            TarifaRepository tarifaRepository,
                            UsuarioRepository usuarioRepository,
                            IncidenciaService incidenciaService) {
        this.envioRepository = envioRepository;
        this.direccionRepository = direccionRepository;
        this.paqueteRepository = paqueteRepository;
        this.tarifaRepository = tarifaRepository;
        this.usuarioRepository = usuarioRepository;
        this.incidenciaService = incidenciaService;
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
    public void eliminar(Long id) {
        envioRepository.deleteById(id);
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
        envio.setPagador("DESTINATARIO".equalsIgnoreCase(dto.getPagador()) ? "DESTINATARIO" : "REMITENTE");
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setCostoTotal(calcularCostoPorDistancia(tarifa, dto.getDistanciaKm()));
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

    @Override
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
        dto.setPagador(envio.getPagador());
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

        Double distanciaKm = dto.getDistanciaKm() != null ? dto.getDistanciaKm() : envio.getDistanciaKm();
        Tarifa tarifa = obtenerTarifaPorTipo(dto.getTipoServicio());

        envio.setTipoServicio(dto.getTipoServicio());
        envio.setPagador("DESTINATARIO".equalsIgnoreCase(dto.getPagador()) ? "DESTINATARIO" : "REMITENTE");
        envio.setTarifa(tarifa);
        envio.setDistanciaKm(distanciaKm);
        envio.setCostoTotal(calcularCostoPorDistancia(tarifa, distanciaKm));
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

    // precioBase se usa como tarifa por kilómetro según el tipo de servicio elegido
    // (ver calcularCostoPorDistancia): estandar/express/prioritario cobran distinto por km.
    private static final Map<String, BigDecimal> PRECIO_POR_KM_POR_TIPO = Map.of(
            "estandar", new BigDecimal("0.10"),
            "express", new BigDecimal("0.25"),
            "prioritario", new BigDecimal("0.50"));

    private Tarifa obtenerTarifaPorTipo(String tipoServicio) {
        return tarifaRepository.findFirstByTipoServicioIgnoreCase(tipoServicio)
                .orElseGet(() -> {
                    String tipo = esVacio(tipoServicio) ? "estandar" : tipoServicio;
                    Tarifa tarifa = new Tarifa();
                    tarifa.setTipoServicio(tipo);
                    tarifa.setPrecioBase(
                            PRECIO_POR_KM_POR_TIPO.getOrDefault(tipo.toLowerCase(), new BigDecimal("0.10")));
                    tarifa.setPrecioPorKg(new BigDecimal("1.50"));
                    tarifa.setZona("General");
                    return tarifaRepository.save(tarifa);
                });
    }

    // Tarifas por km para que el formulario de "Nuevo envío" muestre una vista previa del
    // costo (mapa.js) que coincida con lo que el backend realmente va a cobrar. Parte de
    // los valores por defecto y los sobrescribe con lo que haya guardado en la tabla tarifas.
    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> obtenerTarifasPorKm() {
        Map<String, BigDecimal> tarifas = new java.util.HashMap<>(PRECIO_POR_KM_POR_TIPO);
        for (Tarifa tarifa : tarifaRepository.findAll()) {
            if (tarifa.getTipoServicio() != null && tarifa.getPrecioBase() != null) {
                tarifas.put(tarifa.getTipoServicio().toLowerCase(), tarifa.getPrecioBase());
            }
        }
        return tarifas;
    }

    // Costo del envío = tarifa por km del tipo de servicio × distancia real del trayecto
    // (calculada en el navegador vía OSRM y validada como obligatoria en
    // validarDatosObligatorios). El backend es quien decide el costo final, no el navegador.
    private BigDecimal calcularCostoPorDistancia(Tarifa tarifa, Double distanciaKm) {
        BigDecimal precioPorKm = tarifa != null && tarifa.getPrecioBase() != null
                ? tarifa.getPrecioBase()
                : new BigDecimal("0.10");
        BigDecimal distancia = BigDecimal.valueOf(distanciaKm != null ? distanciaKm : 0.0);
        return precioPorKm.multiply(distancia).setScale(2, RoundingMode.HALF_UP);
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
        if (dto.getDistanciaKm() == null || dto.getDistanciaKm() <= 0) {
            throw new IllegalArgumentException("Debes calcular la ruta (\"Ver ruta\") antes de generar el envío");
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

    @Override
    public List<Envio> listarDisponibles() {
        return envioRepository.findByEstadoAndRepartidorIsNull(EstadoEnvio.PENDIENTE);
    }

    @Override
    @Transactional
    public boolean reclamar(Long envioId, Repartidor repartidor) {
        LocalDateTime ahora = LocalDateTime.now();
        int filas = envioRepository.reclamar(envioId, repartidor, ahora, ahora.plusHours(plazoEntregaHoras));
        return filas == 1;
    }

    @Override
    public long contarReclamadosHoy(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return envioRepository.countByRepartidorAndFechaAsignacionBetween(repartidor, inicio, fin);
    }

    @Override
    public List<Envio> listarAsignadosHoy(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return envioRepository.findByRepartidorAndFechaAsignacionBetween(repartidor, inicio, fin);
    }

    // Cuándo se considera "ganado" un envío depende de quién paga (Envio.pagador):
    // - DESTINATARIO (contra entrega): se gana al ENTREGAR, porque el cobro real ocurre ahí.
    // - REMITENTE (prepagado, o pagador no seteado): se gana al RECOGER, porque el envío ya
    //   estaba pagado de antemano y la recogida es la confirmación de que se hizo el trabajo.
    private boolean seGano(Envio envio) {
        if (envio.getEstado() == EstadoEnvio.CANCELADO) {
            return false;
        }
        if ("DESTINATARIO".equalsIgnoreCase(envio.getPagador())) {
            return envio.getEstado() == EstadoEnvio.ENTREGADO;
        }
        return envio.getFechaRecogido() != null;
    }

    // Suma de "lo ganado hoy" entre los envios asignados hoy, aplicando la regla de seGano()
    // segun quien paga cada envio (ver mas arriba).
    @Override
    public BigDecimal calcularGanadoHoy(Repartidor repartidor) {
        return listarAsignadosHoy(repartidor).stream()
                .filter(this::seGano)
                .map(Envio::getCostoTotal)
                .filter(costo -> costo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Optional<Envio> buscarGestionable(Long envioId, Repartidor repartidor) {
        return envioRepository.findByIdAndRepartidor(envioId, repartidor);
    }

    @Override
    @Transactional
    public boolean marcarEntregado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return false;
        }
        envio.setEstado(EstadoEnvio.ENTREGADO);
        envio.setFechaEntrega(LocalDateTime.now());
        envioRepository.save(envio);
        return true;
    }

    // Registra un intento de entrega fallido. Devuelve Optional<Envio> (en vez de boolean)
    // para que el controller pueda leer el estado resultante y armar el mensaje correcto
    // (reintento vs. devolucion) sin tener que volver a consultar la base.
    @Override
    @Transactional
    public Optional<Envio> marcarNoEntregado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return Optional.empty();
        }

        int intentos = (envio.getIntentosEntrega() == null ? 0 : envio.getIntentosEntrega()) + 1;
        envio.setIntentosEntrega(intentos);

        if (intentos >= maxIntentosEntrega) {
            envio.setEstado(EstadoEnvio.DEVUELTO);
        } else {
            // Se queda asignado al mismo repartidor (a diferencia de CANCELADO/PENDIENTE),
            // asi no desaparece de "Ruta de hoy" y puede reintentarse con reintentarEntrega.
            envio.setEstado(EstadoEnvio.NO_ENTREGADO);
        }

        return Optional.of(envioRepository.save(envio));
    }

    // Vuelve un NO_ENTREGADO a EN_CAMINO para que el repartidor pueda intentar entregarlo
    // de nuevo, sin pasar por "Disponibles" ni perder el conteo de intentos ya usados.
    @Override
    @Transactional
    public boolean reintentarEntrega(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())
                || envio.getEstado() != EstadoEnvio.NO_ENTREGADO) {
            return false;
        }
        envio.setEstado(EstadoEnvio.EN_CAMINO);
        envioRepository.save(envio);
        return true;
    }

    @Override
    @Transactional
    public boolean avanzarEstado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return false;
        }
        EstadoEnvio siguiente = SIGUIENTE_ESTADO.get(envio.getEstado());
        if (siguiente == null) {
            return false;
        }
        envio.setEstado(siguiente);
        if (siguiente == EstadoEnvio.RECOGIDO) {
            envio.setFechaRecogido(LocalDateTime.now());
        }
        envioRepository.save(envio);
        return true;
    }

    // "Eliminar" hace cosas distintas segun el estado, por eso retorna el envio resultante:
    // - ENTREGADO: se borra la fila de verdad (primero su Incidencia si tenia una, porque
    //   la FK incidencias.envio_id impide borrar el envio mientras exista esa referencia).
    // - DEVUELTO: no se borra, se LIBERA — vuelve a PENDIENTE sin repartidor y con los
    //   intentos reiniciados, para que reaparezca en "Disponibles" y otro repartidor lo
    //   intente desde cero.
    @Override
    @Transactional
    public Optional<Envio> eliminarEntregado(Long envioId, Repartidor repartidor) {
        Envio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || envio.getRepartidor() == null
                || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            return Optional.empty();
        }

        if (envio.getEstado() == EstadoEnvio.ENTREGADO) {
            incidenciaService.eliminarPorEnvio(envio);
            // El paquete tiene una FK hacia el envío (paquetes.envio_id); si no se borra
            // primero, el delete del envío revienta con DataIntegrityViolationException.
            paqueteRepository.findByEnvioId(envio.getId()).ifPresent(paquete -> {
                paquete.setEnvio(null);
                paqueteRepository.saveAndFlush(paquete);
                paqueteRepository.delete(paquete);
            });
            envioRepository.flush();
            envioRepository.delete(envio);
            return Optional.of(envio);
        }

        if (envio.getEstado() == EstadoEnvio.DEVUELTO) {
            envio.setEstado(EstadoEnvio.PENDIENTE);
            envio.setRepartidor(null);
            envio.setFechaAsignacion(null);
            envio.setFechaLimite(null);
            envio.setIntentosEntrega(0);
            return Optional.of(envioRepository.save(envio));
        }

        return Optional.empty();
    }

    // Panel "Ganancias": a diferencia de calcularGanadoHoy (que proyecta lo asignado hoy),
    // esto usa fechaEntrega -> solo cuenta dinero de envios que de verdad llegaron a
    // ENTREGADO (cobro confirmado), dentro de la semana actual (lunes a domingo).
    @Override
    public List<Envio> listarEntregadosSemana(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime fin = inicio.plusDays(7);
        return envioRepository.findByRepartidorAndEstadoAndFechaEntregaBetween(
                repartidor, EstadoEnvio.ENTREGADO, inicio, fin);
    }

    // Envios cuya ganancia se reconoce esta semana, combinando los dos casos de seGano():
    // los que paga el REMITENTE cuentan por fechaRecogido, los que paga el DESTINATARIO
    // cuentan por fechaEntrega (y solo si ya llegaron a ENTREGADO). Se usa tanto para sumar
    // calcularGanadoSemana como para las barras de "Ganado por dia" (RepartidorController).
    @Override
    public List<Envio> listarGananciasSemana(Repartidor repartidor) {
        LocalDateTime inicio = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime fin = inicio.plusDays(7);

        List<Envio> pagadosPorRemitente = envioRepository
                .findByRepartidorAndPagadorAndFechaRecogidoBetween(repartidor, "REMITENTE", inicio, fin);
        
        List<Envio> pagadosPorDestinatario = envioRepository
                .findByRepartidorAndPagadorAndEstadoAndFechaEntregaBetween(
                        repartidor, "DESTINATARIO", EstadoEnvio.ENTREGADO, inicio, fin);

        List<Envio> combinados = new java.util.ArrayList<>(pagadosPorRemitente);
        combinados.addAll(pagadosPorDestinatario);
        return combinados;
    }

    @Override
    public BigDecimal calcularGanadoSemana(Repartidor repartidor) {
        return listarGananciasSemana(repartidor).stream()
                .map(Envio::getCostoTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Historial de "Ganancias": las ultimas 10 entregas completadas, mas reciente primero.
    @Override
    public List<Envio> listarEntregasRecientes(Repartidor repartidor) {
        return envioRepository.findTop10ByRepartidorAndEstadoOrderByFechaEntregaDesc(
                repartidor, EstadoEnvio.ENTREGADO);
    }

    @Override
    public List<Envio> listarVencidosNoResueltos() {
        return envioRepository.findByEstadoNotInAndFechaLimiteBefore(
                List.of(EstadoEnvio.ENTREGADO, EstadoEnvio.CANCELADO, EstadoEnvio.DEVUELTO),
                LocalDateTime.now());
    }
}
