package com.Utc.RutaExpress.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Incidencia;
import com.Utc.RutaExpress.repository.IncidenciaRepository;
import com.Utc.RutaExpress.service.IncidenciaService;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Override
    public List<Incidencia> listarTodos() {
        return incidenciaRepository.findAll();
    }

    @Override
    public Incidencia buscarPorId(Long id) {
        return incidenciaRepository.findById(id).orElse(null);
    }

    @Override
    public Incidencia guardar(Incidencia incidencia) {
        return incidenciaRepository.save(incidencia);
    }

    @Override
    public Incidencia actualizar(Long id, Incidencia incidencia) {

        Incidencia existente = incidenciaRepository.findById(id).orElse(null);

        if (existente != null) {

            existente.setEnvio(incidencia.getEnvio());
            existente.setDescripcion(incidencia.getDescripcion());
            existente.setEstado(incidencia.getEstado());

            return incidenciaRepository.save(existente);

        }

        return null;
    }

    @Override
    public void eliminar(Long id) {
        incidenciaRepository.deleteById(id);
    }

}