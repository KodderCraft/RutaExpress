package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import com.Utc.RutaExpress.DTO.loginValidar;
import com.Utc.RutaExpress.entity.Rol;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class controller {
    private final UsuarioService usuarioService;
    
    public controller(UsuarioService ser){
        this.usuarioService = ser;
    }

    @GetMapping()
    public String index() {
        return "index";
    }
    
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());

        return "registro";
    }
    
    @GetMapping("/login")
    public String login(Model model ) {
        model.addAttribute("loginRequest", new loginValidar());
        return "login";
    }

     @PostMapping("/loginValidar")
    public String login(@ModelAttribute("loginRequest") loginValidar request, Model model ,HttpSession session){

        Usuario acceso = usuarioService.login(request);


        if (acceso != null) {
            session.setAttribute("usuario", acceso);

            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Correo o contraseña incorrectos");
        return "login";
    }


    @PostMapping("/validar")
    public String validar(@ModelAttribute("usuario") Usuario usuario){
        // usuarioService.guardarusuario(usuario);
        usuarioService.guardarCliente(usuario);
        return "redirect:/login";
    }


    @GetMapping("/dashboard")
    public String mostrarDashboard( HttpSession session ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
            }

        switch (usuario.getRol()) {

        case CLIENTE:
            return "cliente/dashboard";

        case REPARTIDOR:
            return "repartidor/dashboard";

        case ADMINISTRADOR:
            return "admin/dashboard";

        default:
            return "redirect:/login";
    }    
   
         
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

       return "redirect:/";
}

}
