package com.Utc.RutaExpress.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.EnvioService;
import com.Utc.RutaExpress.service.IncidenciaService;
import com.Utc.RutaExpress.service.RepartidorService;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class RepartidorController {

    private final EnvioService envioService;
    private final RepartidorService repartidorService;
    private final IncidenciaService incidenciaService;

    @Value("${app.repartidor.meta-diaria}")
    private int metaDiaria;

    public RepartidorController(EnvioService envioService, RepartidorService repartidorService,
            IncidenciaService incidenciaService) {
        this.envioService = envioService;
        this.repartidorService = repartidorService;
        this.incidenciaService = incidenciaService;
    }

    @GetMapping("/repartidor/dashboard")
    public String mostrarDashboardRepartidor(HttpSession session, Model model,
            @RequestParam(required = false) Long gestionar) {
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

            Optional<Envio> envioGestionado = gestionar != null
                    ? envioService.buscarGestionable(gestionar, repartidor)
                    : Optional.<Envio>empty();
            model.addAttribute("entregaActual", envioGestionado.orElse(null));
            model.addAttribute("mostrarGestion", envioGestionado.isPresent());
        } else {
            model.addAttribute("enviosDisponibles", Collections.<Envio>emptyList());
            model.addAttribute("asignadosHoy", Collections.<Envio>emptyList());
            model.addAttribute("reclamadosHoy", 0L);
            model.addAttribute("completadasHoy", 0L);
            model.addAttribute("entregaActual", null);
            model.addAttribute("mostrarGestion", false);
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

    @PostMapping("/repartidor/envios/{id}/entregado")
    public String marcarEntregado(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró tu perfil de repartidor.");
            return "redirect:/repartidor/dashboard";
        }

        boolean exito = envioService.marcarEntregado(id, repartidorOpt.get());
        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "Entrega marcada como completada.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar este envío.");
        }

        return "redirect:/repartidor/dashboard?gestionar=" + id;
    }

    @PostMapping("/repartidor/envios/{id}/no-entregado")
    public String noEntregado(@PathVariable Long id, @RequestParam String motivo, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró tu perfil de repartidor.");
            return "redirect:/repartidor/dashboard";
        }

        Repartidor repartidor = repartidorOpt.get();
        Optional<Envio> envioOpt = envioService.buscarGestionable(id, repartidor);
        boolean exito = envioOpt.isPresent() && envioService.marcarNoEntregado(id, repartidor);
        if (exito) {
            Envio envio = envioOpt.get();
            incidenciaService.registrarSiNoExiste(envio,
                    "Entrega fallida: " + envio.getCodigoGuia() + " - Motivo: " + motivo);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Entrega marcada como no realizada. Se generó una incidencia para revisión.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar este envío.");
        }

        return "redirect:/repartidor/dashboard?gestionar=" + id;
    }

    @PostMapping("/repartidor/envios/{id}/avanzar-estado")
    public String avanzarEstado(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró tu perfil de repartidor.");
            return "redirect:/repartidor/dashboard";
        }

        boolean exito = envioService.avanzarEstado(id, repartidorOpt.get());
        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "Estado del envío actualizado.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado de este envío.");
        }

        return "redirect:/repartidor/dashboard?gestionar=" + id;
    }
}
