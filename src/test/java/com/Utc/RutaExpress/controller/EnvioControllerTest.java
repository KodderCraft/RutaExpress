// package com.Utc.RutaExpress.controller;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import org.junit.jupiter.api.Test;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

// import com.Utc.RutaExpress.DTO.RegistroEnvioDTO;
// import com.Utc.RutaExpress.controller.EnvioController;
// import com.Utc.RutaExpress.entity.Envio;
// import com.Utc.RutaExpress.entity.Rol;
// import com.Utc.RutaExpress.entity.Usuario;
// import com.Utc.RutaExpress.service.EnvioServiceImpl;

// import jakarta.servlet.http.HttpSession;

// class EnvioControllerTest {

//     @Test
//     void registrarEnvioDebeUsarUsuarioDeSesionYRedirigirAlDashboardConSuccess() {
//         EnvioServiceImpl envioService = mock(EnvioServiceImpl.class);
//         EnvioController controller = new EnvioController(envioService);

//         Usuario usuario = new Usuario();
//         usuario.setNombre("Ana");
//         usuario.setRol(Rol.CLIENTE);

//         HttpSession session = mock(HttpSession.class);
//         when(session.getAttribute("usuario")).thenReturn(usuario);

//         when(envioService.registrarEnvio(eq(usuario), any(RegistroEnvioDTO.class))).thenReturn(new Envio());

//         RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
//         String vista = controller.registrarEnvio(new RegistroEnvioDTO(), session, redirectAttributes);

//         assertEquals("redirect:/cliente/dashboard", vista);
//         verify(envioService).registrarEnvio(eq(usuario), any(RegistroEnvioDTO.class));
//         assertTrue(redirectAttributes.getFlashAttributes().containsKey("successMessage"));
//     }

//     @Test
//     void registrarEnvioDebeAgregarErrorMessageCuandoFallaValidacion() {
//         EnvioServiceImpl envioService = mock(EnvioServiceImpl.class);
//         EnvioController controller = new EnvioController(envioService);

//         Usuario usuario = new Usuario();
//         usuario.setNombre("Luis");
//         usuario.setRol(Rol.CLIENTE);

//         HttpSession session = mock(HttpSession.class);
//         when(session.getAttribute("usuario")).thenReturn(usuario);

//         when(envioService.registrarEnvio(eq(usuario), any(RegistroEnvioDTO.class)))
//             .thenThrow(new IllegalArgumentException("Los campos del paquete obligatorios son: tipo, peso y alto con valores válidos"));

//         RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
//         String vista = controller.registrarEnvio(new RegistroEnvioDTO(), session, redirectAttributes);

//         assertEquals("redirect:/cliente/dashboard", vista);
//         assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"));
//         assertEquals("Error al registrar el envío: Los campos del paquete obligatorios son: tipo, peso y alto con valores válidos",
//                 redirectAttributes.getFlashAttributes().get("errorMessage"));
//     }
// }
