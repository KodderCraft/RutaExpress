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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class RepartidorController {

    private static final List<DayOfWeek> ORDEN_SEMANA = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private static final List<String> ETIQUETAS_SEMANA = List.of(
            "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom");

    // Vista-modelo de una barra del grafico de "Ganado por dia" en Ganancias. porcentaje
    // ya viene calculado sobre el maximo de la semana (ver construirBarrasSemana) para que
    // el template solo tenga que usarlo directo en el height del CSS.
    public static class BarraDia {
        private final String label;
        private final BigDecimal monto;
        private final int porcentaje;
        private final boolean hoy;

        public BarraDia(String label, BigDecimal monto, int porcentaje, boolean hoy) {
            this.label = label;
            this.monto = monto;
            this.porcentaje = porcentaje;
            this.hoy = hoy;
        }

        public String getLabel() { return label; }
        public BigDecimal getMonto() { return monto; }
        public int getPorcentaje() { return porcentaje; }
        public boolean isHoy() { return hoy; }
    }

    private final EnvioService envioService;
    private final RepartidorService repartidorService;
    private final IncidenciaService incidenciaService;

    @Value("${app.repartidor.meta-diaria}")
    private int metaDiaria;

    @Value("${app.repartidor.max-intentos-entrega}")
    private int maxIntentosEntrega;

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
        model.addAttribute("maxIntentosEntrega", maxIntentosEntrega);

        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isPresent()) {
            Repartidor repartidor = repartidorOpt.get();
            model.addAttribute("repartidor", repartidor);
            List<Envio> asignadosHoy = envioService.listarAsignadosHoy(repartidor);
            long completadasHoy = asignadosHoy.stream()
                    .filter(envio -> envio.getEstado() == EstadoEnvio.ENTREGADO)
                    .count();

            model.addAttribute("enviosDisponibles", envioService.listarDisponibles());
            model.addAttribute("asignadosHoy", asignadosHoy);
            model.addAttribute("reclamadosHoy", envioService.contarReclamadosHoy(repartidor));
            model.addAttribute("completadasHoy", completadasHoy);
            model.addAttribute("ganadoHoy", envioService.calcularGanadoHoy(repartidor));

            // Panel "Ganancias": ganado hoy ya viene calculado arriba; el resto usa entregas
            // reales de la semana (fechaEntrega), no lo asignado/en curso.
            List<Envio> entregadosSemana = envioService.listarEntregadosSemana(repartidor);
            model.addAttribute("ganadoSemana", envioService.calcularGanadoSemana(repartidor));
            model.addAttribute("entregasSemana", (long) entregadosSemana.size());
            model.addAttribute("barrasSemana", construirBarrasSemana(entregadosSemana));
            model.addAttribute("entregasRecientes", envioService.listarEntregasRecientes(repartidor));

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
            model.addAttribute("ganadoHoy", java.math.BigDecimal.ZERO);
            model.addAttribute("ganadoSemana", java.math.BigDecimal.ZERO);
            model.addAttribute("entregasSemana", 0L);
            model.addAttribute("barrasSemana", construirBarrasSemana(Collections.emptyList()));
            model.addAttribute("entregasRecientes", Collections.<Envio>emptyList());
            model.addAttribute("entregaActual", null);
            model.addAttribute("mostrarGestion", false);
        }

        return "repartidor/dashboard";
    }

    // Agrupa las entregas de la semana por dia (Lun-Dom) y calcula el porcentaje de
    // altura de cada barra en base al dia con mas ganado (si todos los dias estan en
    // cero, todas las barras quedan en 0% en vez de dividir por cero).
    private List<BarraDia> construirBarrasSemana(List<Envio> entregadosSemana) {
        Map<DayOfWeek, BigDecimal> porDia = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dia : ORDEN_SEMANA) {
            porDia.put(dia, BigDecimal.ZERO);
        }
        for (Envio envio : entregadosSemana) {
            if (envio.getFechaEntrega() != null && envio.getCostoTotal() != null) {
                DayOfWeek dia = envio.getFechaEntrega().getDayOfWeek();
                porDia.merge(dia, envio.getCostoTotal(), BigDecimal::add);
            }
        }

        BigDecimal maximo = porDia.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        DayOfWeek hoy = java.time.LocalDate.now().getDayOfWeek();

        List<BarraDia> barras = new java.util.ArrayList<>();
        for (int i = 0; i < ORDEN_SEMANA.size(); i++) {
            DayOfWeek dia = ORDEN_SEMANA.get(i);
            BigDecimal monto = porDia.get(dia);
            int porcentaje = maximo.compareTo(BigDecimal.ZERO) > 0
                    ? monto.multiply(BigDecimal.valueOf(100)).divide(maximo, 0, java.math.RoundingMode.HALF_UP).intValue()
                    : 0;
            barras.add(new BarraDia(ETIQUETAS_SEMANA.get(i), monto, porcentaje, dia == hoy));
        }
        return barras;
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
        Optional<Envio> resultado = envioOpt.isPresent()
                ? envioService.marcarNoEntregado(id, repartidor)
                : Optional.<Envio>empty();

        if (resultado.isPresent()) {
            Envio envio = resultado.get();
            // Dedup por envio (IncidenciaService.registrarSiNoExiste): en varios intentos
            // fallidos sobre el mismo envio solo queda la primera incidencia registrada.
            incidenciaService.registrarSiNoExiste(envio,
                    "Entrega fallida: " + envio.getCodigoGuia() + " - Motivo: " + motivo);

            // El mensaje depende del estado resultante (reintento vs. devolucion), por eso
            // marcarNoEntregado devuelve el envio actualizado en vez de solo un boolean.
            String mensaje = envio.getEstado() == EstadoEnvio.DEVUELTO
                    ? "Se agotaron los " + maxIntentosEntrega + " intentos. El paquete se marcará para devolución."
                    : "Entrega marcada como no realizada (intento " + envio.getIntentosEntrega() + "/"
                            + maxIntentosEntrega + "). Puedes reintentarla cuando quieras.";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar este envío.");
        }

        return "redirect:/repartidor/dashboard?gestionar=" + id;
    }

    @PostMapping("/repartidor/envios/{id}/reintentar")
    public String reintentarEntrega(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró tu perfil de repartidor.");
            return "redirect:/repartidor/dashboard";
        }

        boolean exito = envioService.reintentarEntrega(id, repartidorOpt.get());
        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "Entrega reactivada. Ya puedes intentar entregarla de nuevo.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo reintentar este envío.");
        }

        return "redirect:/repartidor/dashboard?gestionar=" + id;
    }

    @PostMapping("/repartidor/envios/{id}/eliminar")
    public String eliminarEnvio(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Repartidor> repartidorOpt = usuario == null
                ? Optional.empty()
                : repartidorService.buscarPorUsuarioId(usuario.getId());

        if (repartidorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró tu perfil de repartidor.");
            return "redirect:/repartidor/dashboard";
        }

        Optional<Envio> resultado = envioService.eliminarEntregado(id, repartidorOpt.get());
        if (resultado.isPresent()) {
            // Si quedo en PENDIENTE es porque era un DEVUELTO y se liberó a Disponibles;
            // cualquier otro caso (o el envio ya no existe) significa que se borró de verdad.
            String mensaje = resultado.get().getEstado() == EstadoEnvio.PENDIENTE
                    ? "Envío liberado. Volvió a la lista de disponibles para que alguien más lo entregue."
                    : "Envío eliminado del historial.";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar este envío.");
        }

        return "redirect:/repartidor/dashboard";
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
