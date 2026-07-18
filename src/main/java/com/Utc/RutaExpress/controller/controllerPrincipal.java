package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.IncidenciaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class controllerPrincipal {

    private final IncidenciaService incidenciaService;

    public controllerPrincipal(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

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

    @GetMapping("/administrador/dashboard")
    public String mostrarDashboardAdmin( HttpSession session,  Model model ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("incidencias", incidenciaService.listarTodas());
        return "administrador/dashboard";
    }



}
