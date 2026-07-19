package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import java.util.List;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.IncidenciaService;
import com.Utc.RutaExpress.service.EnvioService;
import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")



public class controllerPrincipal {

    private final IncidenciaService incidenciaService;
    private final EnvioService envioService;

    public controllerPrincipal(IncidenciaService incidenciaService, EnvioService envioService) {
        this.incidenciaService = incidenciaService;
        this.envioService = envioService;
    }

    @GetMapping()
    public String index() {
        return "index";
    }


    @GetMapping("/cliente/dashboard")
    public String mostrarDashboardCliente(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // 1. Controlamos el formulario (si viene de error o es uno nuevo)
        RegistroEnvioDTO formularioConErrores = (RegistroEnvioDTO) session.getAttribute("formularioGuardado");
        if (formularioConErrores != null) {
            model.addAttribute("envioDTO", formularioConErrores);
        } else {
            model.addAttribute("envioDTO", new RegistroEnvioDTO());
        }

        // 2. Cargamos el resto de datos de la vista
        model.addAttribute("usuario", usuario);

        List<Envio> envios = envioService.listarPorCliente(usuario.getId());
        model.addAttribute("envios", envios);

        List<Envio> enviosRecibidos = envioService.listarPorDestinatario(usuario.getId());
        model.addAttribute("enviosRecibidos", enviosRecibidos);
        int totalGenerados = envios.size();
        int totalRecibidos = enviosRecibidos.size();
        long totalEntregados = envios.stream()
                .filter(e -> e.getEstado() != null && e.getEstado() == EstadoEnvio.ENTREGADO)
                .count();

        model.addAttribute("totalGenerados", totalGenerados);
        model.addAttribute("totalEntregados", totalEntregados);
        model.addAttribute("totalRecibidos", totalRecibidos);
        model.addAttribute("tarifasPorKm", envioService.obtenerTarifasPorKm());

        return "cliente/dashboard";
    }

    @GetMapping("/administrador/dashboard")
    public String mostrarDashboardAdmin( HttpSession session,  Model model ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("incidencias", incidenciaService.listarTodas());
        return "administrador/dashboard";
    }



}
