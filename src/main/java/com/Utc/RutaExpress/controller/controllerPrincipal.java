package com.Utc.RutaExpress.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;


import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.EstadoEnvio;
import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.entity.Usuario;
import com.Utc.RutaExpress.entity.Rol;
import com.Utc.RutaExpress.service.EnvioServiceImpl;

import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.repository.IncidenciaRepository;
import com.Utc.RutaExpress.repository.PagoRepository;
import com.Utc.RutaExpress.repository.UsuarioRepository;
import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
import com.Utc.RutaExpress.service.IncidenciaService;
import com.Utc.RutaExpress.service.RepartidorService;
import com.Utc.RutaExpress.service.UsuarioService;
import com.Utc.RutaExpress.service.EnvioService;
import com.Utc.RutaExpress.DTO.RegistroUsuarioDTO;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")



public class controllerPrincipal {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;
    private final EnvioServiceImpl envioServiceImpl;
    private final IncidenciaService incidenciaService;
    private final EnvioService envioService;
    private final UsuarioService usuarioService; 
    private final RepartidorService repartidorService; 
    
    public controllerPrincipal(EnvioServiceImpl envioServiceImpl,IncidenciaService incidenciaService, EnvioService envioService, UsuarioService usuarioService ,RepartidorService repartidorService) {
        this.envioServiceImpl = envioServiceImpl;
        this.incidenciaService = incidenciaService;
        this.envioService = envioService;
        this.usuarioService = usuarioService;
        this.repartidorService = repartidorService;
    }

    @GetMapping()
    public String index() {
        return "index";
    }

    @GetMapping("/usuario/dashboard/perfil/editar/{id}")
    public String editarPerfil(@PathVariable("id") Long id ,  HttpSession session , Model model ){
        Usuario usuario = usuarioService.buscarUsuario(id);
        RegistroUsuarioDTO nuevoUser = new RegistroUsuarioDTO();
        
        nuevoUser.setNombre(usuario.getNombre());
        nuevoUser.setEmail(usuario.getEmail());
        nuevoUser.setPassword(usuario.getPassword());
        nuevoUser.setTelefono(usuario.getTelefono());
        nuevoUser.setRol(usuario.getRol());
        Repartidor repar = repartidorService.buscarPorUsuarioId(id).orElse(null);
        if (repar != null) {
        nuevoUser.setVehiculoTipo(repar.getVehiculoTipo());
        nuevoUser.setPlaca(repar.getPlaca());
        }



        model.addAttribute("usuario", usuario);
        model.addAttribute("nuevoDto", nuevoUser);
        return "perfil-editar";
    }

    @PostMapping("/usuario/dashboard/perfil/actulizar/{id}")
    public String actulizarperfil(@PathVariable("id") Long id, @ModelAttribute RegistroUsuarioDTO dto){
        Usuario usuario = usuarioService.buscarUsuario(id);
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());

        usuarioService.guardarCliente(usuario);

        if(dto.getRol() == Rol.REPARTIDOR){

        Repartidor repartidor = repartidorService.buscarPorUsuarioId(id).orElse(null);

        repartidor.setUsuario(usuario);
        repartidor.setVehiculoTipo(dto.getVehiculoTipo());
        repartidor.setPlaca(dto.getPlaca());

        repartidorService.guardarRepartidor(repartidor);
            }

        return "redirect:/";
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

        // El model.addAttribute("envioDTO", new RegistroEnvioDTO()); que estaba aquí LO LINDAMOS.

        // 2. Cargamos el resto de datos de la vista
        model.addAttribute("usuario", usuario);

        List<Envio> envios = envioServiceImpl.listarPorCliente(usuario.getId());
        model.addAttribute("envios", envios);
        long totalEntregados = envios.stream()
                .filter(e -> e.getEstado() != null && e.getEstado() == EstadoEnvio.ENTREGADO)
                .count();
        int totalGenerados = envios.size(); 
        
        List<Envio> enviosRecibidos = envioServiceImpl.listarPorDestinatario(usuario.getId());
        int totalRecibidos = enviosRecibidos.size();
                
        model.addAttribute("totalGenerados", totalGenerados);
        model.addAttribute("totalEntregados", totalEntregados);
        model.addAttribute("enviosRecibidos", enviosRecibidos);
        
        return "cliente/dashboard";
    }

    @GetMapping("/administrador/dashboard")
    public String mostrarDashboardAdmin( HttpSession session,  Model model ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Usuario> destinatarios = usuarios.stream()
            .filter(item -> item.getRol() == Rol.CLIENTE)
            .toList();
        List<Envio> enviosList = envioRepository.findAll();

        Map<Long, Long> totalEnviosPorDestinatario = new HashMap<>();
        Map<Long, String> estadoDestinatario = new HashMap<>();
        Map<Long, String> direccionDestinatario = new HashMap<>();
        Map<Long, String> historialDestinatario = new HashMap<>();

        for (Usuario destinatario : destinatarios) {
            List<Envio> historial = envioRepository.findByDestinatarioId(destinatario.getId());
            totalEnviosPorDestinatario.put(destinatario.getId(), (long) historial.size());

            boolean enRuta = historial.stream().anyMatch(envio ->
                envio.getEstado() == EstadoEnvio.EN_CAMINO || envio.getEstado() == EstadoEnvio.RECOGIDO
            );
            estadoDestinatario.put(destinatario.getId(), enRuta ? "EN_RUTA" : "ACTIVO");

            String direccion = destinatario.getDireccion() != null && !destinatario.getDireccion().isBlank()
                ? destinatario.getDireccion()
                : historial.stream()
                    .filter(envio -> envio.getDireccionEntrega() != null && envio.getDireccionEntrega().getDireccionTexto() != null)
                    .map(envio -> envio.getDireccionEntrega().getDireccionTexto())
                    .findFirst()
                    .orElse("Sin dirección registrada");
            direccionDestinatario.put(destinatario.getId(), direccion);

            StringBuilder historialBuilder = new StringBuilder();
            for (Envio envio : historial) {
                if (historialBuilder.length() > 0) {
                    historialBuilder.append(";");
                }
                historialBuilder.append(envio.getRemitente() != null && envio.getRemitente().getNombre() != null ? envio.getRemitente().getNombre() : "Sin remitente")
                    .append("|")
                    .append(envio.getFechaEntrega() != null ? envio.getFechaEntrega().toLocalDate().toString() : "Sin fecha")
                    .append("|")
                    .append(envio.getEstado() == EstadoEnvio.ENTREGADO ? "Entregado" : "En camino");
            }
            historialDestinatario.put(destinatario.getId(), historialBuilder.toString());
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("usuariosCount", usuarioRepository.count());
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("remitentes", usuarios);
        model.addAttribute("destinatarios", destinatarios);
        model.addAttribute("envios", envioRepository.count());
        model.addAttribute("enviosList", enviosList);
        model.addAttribute("totalEnviosPorDestinatario", totalEnviosPorDestinatario);
        model.addAttribute("estadoDestinatario", estadoDestinatario);
        model.addAttribute("direccionDestinatario", direccionDestinatario);
        model.addAttribute("historialDestinatario", historialDestinatario);
        model.addAttribute("pagos", pagoRepository.count());
        model.addAttribute("incidencias", incidenciaRepository.count());
        model.addAttribute("incidencias", incidenciaService.listarTodas());
        return "administrador/dashboard";
    }



}
