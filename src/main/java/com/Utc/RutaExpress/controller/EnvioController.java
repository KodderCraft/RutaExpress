package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Paquete;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.service.EnvioServiceImpl;

import jakarta.servlet.http.HttpSession;

@Controller
public class EnvioController {

    private final EnvioServiceImpl envioServiceImpl;

    public EnvioController(EnvioServiceImpl envioServiceImpl) {
        this.envioServiceImpl = envioServiceImpl;
    }

@PostMapping("/cliente/envio/registrar")
public String registrarEnvio(@ModelAttribute("envioDTO") RegistroEnvioDTO registroEnvioDTO, 
                             HttpSession session, 
                             RedirectAttributes flash) {
    try {
        // Validaciones de tus campos reales
        boolean dirRecogidaVacia = (registroEnvioDTO.getDireccionRecogida() == null || registroEnvioDTO.getDireccionRecogida().trim().isEmpty());
        boolean dirEntregaVacia = (registroEnvioDTO.getDireccionEntrega() == null || registroEnvioDTO.getDireccionEntrega().trim().isEmpty());
        boolean servicioVacio    = (registroEnvioDTO.getTipoServicio() == null || registroEnvioDTO.getTipoServicio().trim().isEmpty());
        boolean nombreDestVacio  = (registroEnvioDTO.getNombreDestinatario() == null || registroEnvioDTO.getNombreDestinatario().trim().isEmpty());
        boolean altoVacio  = (registroEnvioDTO.getAlto() == null || registroEnvioDTO.getAlto().byteValue() == 0);
        boolean anchoVacio  = (registroEnvioDTO.getAncho() == null || registroEnvioDTO.getAncho().byteValue() == 0);
        boolean largoVacio  = (registroEnvioDTO.getLargo() == null || registroEnvioDTO.getLargo().byteValue() == 0);
        
        
        if (dirRecogidaVacia && dirEntregaVacia && nombreDestVacio && servicioVacio) {
            throw new IllegalArgumentException("No puedes registrar un envío vacío.");
        }
        if (dirRecogidaVacia) throw new IllegalArgumentException("La dirección de recogida es obligatoria.");
        if (dirEntregaVacia)  throw new IllegalArgumentException("La dirección de entrega es obligatoria.");
        if(altoVacio || anchoVacio || largoVacio) throw new IllegalArgumentException("Las dimensiones del paquete no pueden ser cero.");
        // Si todo está correcto, guardas en la base de datos:
        // envioServiceImpl.registrar(registroEnvioDTO);

        //  ¡ÉXITO! Aquí sí destruimos los datos de la sesión porque ya se guardaron
        session.removeAttribute("formularioGuardado");
        
        flash.addFlashAttribute("successMessage", "Envío registrado con éxito.");
        return "redirect:/cliente/dashboard";

    } catch (IllegalArgumentException ex) {
        //  ¡ERROR! Guardamos los datos en la sesión.
        // Mientras el usuario no corrija el error, este DTO se quedará congelado en la sesión
        // permitiendo hacer 1, 5 o 50 F5 seguidos sin perder absolutamente nada.
        session.setAttribute("formularioGuardado", registroEnvioDTO);
        
        flash.addFlashAttribute("errorMessage", "Error: " + ex.getMessage());
        flash.addFlashAttribute("errorCss", true);

        return "redirect:/cliente/dashboard";
    }
}






    @GetMapping("/cliente/envio/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, HttpSession session, Model model) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        Envio envio = envioServiceImpl.buscarPorId(id);
        if (envio == null || envio.getRemitente() == null || !envio.getRemitente().getId().equals(usuarioSesion.getId())) {
            return "redirect:/cliente/dashboard";
        }
        RegistroEnvioDTO dto = envioServiceImpl.obtenerDetalleEnvio(id);
        model.addAttribute("registroEnvioDTO", dto);

        return "cliente/editar-envio";
    }

 
    @PostMapping("/cliente/envio/actualizar/{id}")
      public String actualizarEnvio(@PathVariable("id") Long id, @ModelAttribute("registroEnvioDTO") RegistroEnvioDTO dto) {
            envioServiceImpl.actualizarEnvio(id, dto);
    return "redirect:/cliente/dashboard";
}
    @GetMapping("/cliente/envio/eliminar/{id}")
    public String eliminarEnvio(@PathVariable("id") Long id , Model model) {
        envioServiceImpl.eliminarEnvio(id);
        try{
            model.addAttribute("successMessage", "Por favor completa todos los campos obligatorios del envío y del paquete.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al eliminar el envío: " + e.getMessage());
            
            return "redirect:/cliente/dashboard";
        }
        return "redirect:/cliente/dashboard";
   }

}

