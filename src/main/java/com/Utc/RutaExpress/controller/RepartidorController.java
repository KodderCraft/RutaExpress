package com.Utc.RutaExpress.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.EnvioService;
import com.Utc.RutaExpress.service.RepartidorService;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class RepartidorController {

    private final EnvioService envioService;
    private final RepartidorService repartidorService;

    @Value("${app.repartidor.meta-diaria}")
    private int metaDiaria;

    public RepartidorController(EnvioService envioService, RepartidorService repartidorService) {
        this.envioService = envioService;
        this.repartidorService = repartidorService;
    }

    @GetMapping("/repartidor/dashboard")
    public String mostrarDashboardRepartidor(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("metaDiaria", metaDiaria);

        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isPresent()) {
            Repartidor repartidor = repartidorOpt.get();
            List<Envio> asignadosHoy = envioService.listarAsignadosHoy(repartidor);
            long completadasHoy = asignadosHoy.stream()
                    .filter(envio -> envio.getEstado() == EstadoEnvio.ENTREGADO)
                    .count();

            model.addAttribute("enviosDisponibles", envioService.listarDisponibles());
            model.addAttribute("asignadosHoy", asignadosHoy);
            model.addAttribute("reclamadosHoy", envioService.contarReclamadosHoy(repartidor));
            model.addAttribute("completadasHoy", completadasHoy);
        } else {
            model.addAttribute("enviosDisponibles", Collections.<Envio>emptyList());
            model.addAttribute("asignadosHoy", Collections.<Envio>emptyList());
            model.addAttribute("reclamadosHoy", 0L);
            model.addAttribute("completadasHoy", 0L);
        }

        return "repartidor/dashboard";
    }

    @PostMapping("/repartidor/envios/{id}/reclamar")
    public String reclamarEnvio(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró tu perfil de repartidor.");
            return "redirect:/repartidor/dashboard";
        }

        boolean exito = envioService.reclamar(id, repartidorOpt.get());
        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "Reclamaste el envío correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Este envío ya fue tomado por otro repartidor.");
        }

        return "redirect:/repartidor/dashboard";
    }
}
