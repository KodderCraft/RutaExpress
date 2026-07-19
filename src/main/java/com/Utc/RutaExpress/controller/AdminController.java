package com.Utc.RutaExpress.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.entity.Rol;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.repository.UsuarioRepository;
import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.repository.PagoRepository;
import com.Utc.RutaExpress.repository.IncidenciaRepository;
import com.Utc.RutaExpress.service.RepartidorService;
import com.Utc.RutaExpress.service.UsuarioService;

@Controller
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RepartidorService repartidorService;

    @PostMapping(value = "/api/admin/repartidores", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> registrarRepartidor(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        String nombre = String.valueOf(payload.getOrDefault("nombre", "")).trim();
        String email = String.valueOf(payload.getOrDefault("email", "")).trim();
        String telefono = String.valueOf(payload.getOrDefault("telefono", "")).trim();
        String password = String.valueOf(payload.getOrDefault("password", "")).trim();
        String zona = String.valueOf(payload.getOrDefault("zona", "Centro")).trim();
        String tipoVehiculo = String.valueOf(payload.getOrDefault("tipoVehiculo", "Moto")).trim();

        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            response.put("success", false);
            response.put("message", "Nombre, correo y contraseña son obligatorios.");
            return response;
        }

        if (usuarioService.existeCorreo(email)) {
            response.put("success", false);
            response.put("message", "El correo ya está registrado.");
            return response;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setTelefono(telefono);
        usuario.setRol(Rol.REPARTIDOR);
        usuarioService.guardarCliente(usuario);

        Repartidor repartidor = new Repartidor();
        repartidor.setUsuario(usuario);
        repartidor.setDisponible(true);
        repartidor.setZona(zona);
        repartidor.setVehiculoTipo(tipoVehiculo);
        repartidor.setPlaca("TMP-" + usuario.getId());
        repartidorService.guardarRepartidor(repartidor);

        response.put("success", true);
        response.put("message", "Repartidor registrado correctamente.");
        response.put("id", usuario.getId());
        return response;
    }
    
    @GetMapping("/administrador/inicio")
    public String dashboard(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Usuario> destinatarios = usuarios.stream()
                .filter(item -> item.getRol() == Rol.CLIENTE)
                .toList();
        List<Envio> enviosList = envioRepository.findAll();

        Map<Long, Long> totalEnviosPorDestinatario = new HashMap<>();
        Map<Long, String> estadoDestinatario = new HashMap<>();
        Map<Long, String> direccionDestinatario = new HashMap<>();
        Map<Long, String> historialDestinatario = new HashMap<>();

        for (Usuario destinatario : destinatarios) {
            List<Envio> historial = envioRepository.findByDestinatarioId(destinatario.getId());
            totalEnviosPorDestinatario.put(destinatario.getId(), (long) historial.size());
            boolean enRuta = historial.stream().anyMatch(envio ->
                envio.getEstado() == EstadoEnvio.EN_CAMINO || envio.getEstado() == EstadoEnvio.RECOGIDO
            );
            estadoDestinatario.put(destinatario.getId(), enRuta ? "EN_RUTA" : "ACTIVO");

            String direccion = destinatario.getDireccion() != null && !destinatario.getDireccion().isBlank()
                ? destinatario.getDireccion()
                : historial.stream()
                    .filter(envio -> envio.getDireccionEntrega() != null && envio.getDireccionEntrega().getDireccionTexto() != null)
                    .map(envio -> envio.getDireccionEntrega().getDireccionTexto())
                    .findFirst()
                    .orElse("Sin dirección registrada");
            direccionDestinatario.put(destinatario.getId(), direccion);

            StringBuilder historialBuilder = new StringBuilder();
            for (Envio envio : historial) {
                if (historialBuilder.length() > 0) {
                    historialBuilder.append(";");
                }
                historialBuilder.append(envio.getRemitente() != null && envio.getRemitente().getNombre() != null ? envio.getRemitente().getNombre() : "Sin remitente")
                    .append("|")
                    .append(envio.getFechaEntrega() != null ? envio.getFechaEntrega().toLocalDate().toString() : "Sin fecha")
                    .append("|")
                    .append(envio.getEstado() == EstadoEnvio.ENTREGADO ? "Entregado" : "En camino");
            }
            historialDestinatario.put(destinatario.getId(), historialBuilder.toString());
        }

        model.addAttribute("usuariosCount",
                usuarioRepository.count());
        model.addAttribute("usuarios",
                usuarios);
        model.addAttribute("remitentes",
                usuarios);
        model.addAttribute("destinatarios",
                destinatarios);
        model.addAttribute("envios",
                envioRepository.count());
        model.addAttribute("enviosList",
                enviosList);
        model.addAttribute("totalEnviosPorDestinatario",
                totalEnviosPorDestinatario);
        model.addAttribute("estadoDestinatario",
                estadoDestinatario);
        model.addAttribute("direccionDestinatario",
                direccionDestinatario);
        model.addAttribute("historialDestinatario",
                historialDestinatario);

        model.addAttribute("pagos",
                pagoRepository.count());

        model.addAttribute("incidencias",
                incidenciaRepository.count());

        return "administrador/dashboard";
    }
    @GetMapping("/administrador/usuarios")
    public String usuarios(Model model){

        model.addAttribute(
            "usuarios",
            usuarioRepository.findAll()
        );

        return "administrador/usuarios";
    }
    @GetMapping("/administrador/envios")
    public String envios(Model model){

        model.addAttribute(
                "envios",
                envioRepository.findAll());

        return "administrador/envios";
    }
    @GetMapping("/administrador/pagos")
    public String pagos(Model model){

        model.addAttribute(
                "pagos",
                pagoRepository.findAll());

        return "administrador/pagos";
    }
    @GetMapping("/administrador/incidencias")
    public String incidencias(Model model){

        model.addAttribute(
                "incidencias",
                incidenciaRepository.findAll());

        return "administrador/incidencias";
    }

}