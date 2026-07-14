package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import com.Utc.RutaExpress.DTO.loginValidar;
import com.Utc.RutaExpress.entity.Cliente;
import com.Utc.RutaExpress.service.ClienteService;

@Controller
@RequestMapping("/")
public class controller {
    private final ClienteService clienteService;
    
    public controller(ClienteService ser){
        this.clienteService = ser;
    }

    @GetMapping()
    public String index() {
        return "index";
    }
    
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("cliente", new Cliente());

        return "registro";
    }
    
    @GetMapping("/login")
    public String login(Model model ) {
        model.addAttribute("loginRequest", new loginValidar());
        return "login";
    }

     @PostMapping("/loginValidar")
    public String login(@ModelAttribute("loginRequest") loginValidar request, Model model){

        boolean acceso = clienteService.login(request);

        if (acceso) {
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Correo o contraseña incorrectos");
        return "login";
    }


    @PostMapping("validar")
    public String validar(@ModelAttribute("Cliente") Cliente cliente){
        clienteService.guardarCliente(cliente);
        return "redirect:/login";
    }


    @GetMapping("/dashboard")
public String mostrarDashboard() {
    return "dashboard"; 
}
}
