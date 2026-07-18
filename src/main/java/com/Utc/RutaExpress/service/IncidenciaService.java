package com.Utc.RutaExpress.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Utc.RutaExpress.entity.EstadoIncidencia;
import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.entity.Incidencia;
import com.Utc.RutaExpress.repository.IncidenciaRepository;

import java.util.List;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;

    public IncidenciaService(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    public List<Incidencia> listarTodas() {
        return incidenciaRepository.findAllByOrderByIdDesc();
    }

    @Transactional
    public void registrarSiNoExiste(Envio envio, String descripcion) {
        if (!incidenciaRepository.existsByEnvio(envio)) {
            Incidencia incidencia = new Incidencia();
            incidencia.setEnvio(envio);
            incidencia.setDescripcion(descripcion);
            incidencia.setEstado(EstadoIncidencia.PENDIENTE);
            incidenciaRepository.save(incidencia);
        }
    }
}
