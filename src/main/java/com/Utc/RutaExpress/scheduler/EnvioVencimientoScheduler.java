package com.Utc.RutaExpress.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.service.EnvioService;
import com.Utc.RutaExpress.service.IncidenciaService;

@Component
public class EnvioVencimientoScheduler {

    private final EnvioService envioService;
    private final IncidenciaService incidenciaService;

    public EnvioVencimientoScheduler(EnvioService envioService, IncidenciaService incidenciaService) {
        this.envioService = envioService;
        this.incidenciaService = incidenciaService;
    }

    @Scheduled(fixedRate = 300000)
    public void revisarEnviosVencidos() {
        for (Envio envio : envioService.listarVencidosNoResueltos()) {
            incidenciaService.registrarSiNoExiste(envio,
                    "Entrega vencida: " + envio.getCodigoGuia() + " no se completó antes de la fecha límite.");
        }
    }
}
