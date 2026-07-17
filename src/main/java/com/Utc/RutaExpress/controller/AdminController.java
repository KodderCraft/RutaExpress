package com.Utc.RutaExpress.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.Utc.RutaExpress.repository.UsuarioRepository;
import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.repository.PagoRepository;
import com.Utc.RutaExpress.repository.IncidenciaRepository;

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
    
    @GetMapping("/administrador/inicio")
    public String dashboard(Model model){

        model.addAttribute("usuarios",
                usuarioRepository.count());

        model.addAttribute("envios",
                envioRepository.count());

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