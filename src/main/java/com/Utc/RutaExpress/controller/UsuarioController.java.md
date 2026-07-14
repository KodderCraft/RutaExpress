package com.Utc.RutaExpress.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.UsuarioService;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model){

        model.addAttribute("usuarios", usuarioService.listarTodos());

        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model){

        model.addAttribute("usuario", new Usuario());

        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuario){

        usuarioService.guardar(usuario);

        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model){

        model.addAttribute("usuario",
                usuarioService.buscarPorId(id));

        return "usuarios/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        usuarioService.eliminar(id);

        return "redirect:/usuarios";
    }

}