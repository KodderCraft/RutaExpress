package com.Utc.RutaExpress.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Rol;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.repository.IncidenciaRepository;
import com.Utc.RutaExpress.repository.PagoRepository;
import com.Utc.RutaExpress.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class controllerPrincipal {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @GetMapping()
    public String index() {
        return "index";
    }


    @GetMapping("/cliente/dashboard")
    public String mostrarDashboardCliente( HttpSession session , Model model) {  
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        
        return "cliente/dashboard";
    }

     @GetMapping("/repartidor/dashboard")
    public String mostrarDashboardRepartidor( HttpSession session , Model model ) {  
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        return "repartidor/dashboard";
    }

    @GetMapping("/administrador/dashboard")
    public String mostrarDashboardAdmin( HttpSession session,  Model model ) {  
        Usuario usuario = (Usuario) session.getAttribute("usuario");
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

        model.addAttribute("usuario", usuario);
        model.addAttribute("usuariosCount", usuarioRepository.count());
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("remitentes", usuarios);
        model.addAttribute("destinatarios", destinatarios);
        model.addAttribute("envios", envioRepository.count());
        model.addAttribute("enviosList", enviosList);
        model.addAttribute("totalEnviosPorDestinatario", totalEnviosPorDestinatario);
        model.addAttribute("estadoDestinatario", estadoDestinatario);
        model.addAttribute("direccionDestinatario", direccionDestinatario);
        model.addAttribute("historialDestinatario", historialDestinatario);
        model.addAttribute("pagos", pagoRepository.count());
        model.addAttribute("incidencias", incidenciaRepository.count());
        return "administrador/dashboard";
    }



}
