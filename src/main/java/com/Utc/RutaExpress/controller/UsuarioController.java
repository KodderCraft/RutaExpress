package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.Utc.RutaExpress.DTO.loginValidar;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.entity.Rol;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.DTO.RegistroUsuarioDTO;
import com.Utc.RutaExpress.service.RepartidorService;
import com.Utc.RutaExpress.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final RepartidorService repartidorService;

    private void cargarFormulario(Model model, RegistroUsuarioDTO dto) {
    model.addAttribute("registro", new RegistroUsuarioDTO() );
    model.addAttribute("loginRequest", new loginValidar());
    model.addAttribute("roles", Rol.values());
}

    public UsuarioController(UsuarioService ser , RepartidorService re ){
        this.usuarioService = ser;
        this.repartidorService = re;
    }
   @GetMapping("/login")
    public String login(@RequestParam(defaultValue = "login") String panel,Model model ) {
        
        cargarFormulario(model, null);
        return "login";
    }

     @PostMapping("/loginValidar")
    public String login(@ModelAttribute("loginRequest") loginValidar request, Model model ,HttpSession session){
        Usuario acceso = usuarioService.login(request);
        
        if(acceso == null){
        model.addAttribute("error","Usuario o contraseña incorrectos");
        model.addAttribute("registro", new RegistroUsuarioDTO() );
        model.addAttribute("roles", Rol.values());
        return "login";
            }
        
        session.setAttribute("usuario", acceso);

        if(acceso.getRol().equals(Rol.ADMINISTRADOR)){
            return "redirect:/administrador/dashboard";
        }
        if(acceso.getRol().equals(Rol.REPARTIDOR)){
            return "redirect:/repartidor/dashboard";
        }      
        return "redirect:/cliente/dashboard";

    }


    @PostMapping("/validar")
    public String validar(@ModelAttribute RegistroUsuarioDTO dto , Model model){
    if (usuarioService.existeCorreo(dto.getEmail()) ) {
            cargarFormulario(model, dto);
            model.addAttribute("error", "Ya existe el correo   O   campos vacios");
            return "login";
    } else if (dto.getRol() == null){
            model.addAttribute("error", "No se ha seleccionado ningún rol.");
            cargarFormulario(model, dto);
            return "login";
    }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());

        usuarioService.guardarCliente(usuario);

        if(dto.getRol() == Rol.REPARTIDOR){

        Repartidor repartidor = new Repartidor();

        repartidor.setUsuario(usuario);
        repartidor.setVehiculoTipo(dto.getVehiculoTipo());
        repartidor.setPlaca(dto.getPlaca());

        repartidorService.guardarRepartidor(repartidor);
            }

        return "redirect:/login";
    }    
   
    

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

       return "redirect:/";
}

}
