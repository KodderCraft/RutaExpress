package com.Utc.RutaExpress.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Tarifa;
import com.Utc.RutaExpress.repository.TarifaRepository;
import com.Utc.RutaExpress.service.TarifaService;

@Service
public class TarifaServiceImpl implements TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Override
    public List<Tarifa> listarTodos() {
        return tarifaRepository.findAll();
    }

    @Override
    public Tarifa buscarPorId(Long id) {
        return tarifaRepository.findById(id).orElse(null);
    }

    @Override
    public Tarifa guardar(Tarifa tarifa) {
        return tarifaRepository.save(tarifa);
    }

    @Override
    public Tarifa actualizar(Long id, Tarifa tarifa) {

        Tarifa existente = tarifaRepository.findById(id).orElse(null);

        if (existente != null) {

            existente.setTipoServicio(tarifa.getTipoServicio());
            existente.setCostoBase(tarifa.getCostoBase());
            existente.setCostoKgAdicional(tarifa.getCostoKgAdicional());

            return tarifaRepository.save(existente);

        }

        return null;
    }

    @Override
    public void eliminar(Long id) {
        tarifaRepository.deleteById(id);
    }

}