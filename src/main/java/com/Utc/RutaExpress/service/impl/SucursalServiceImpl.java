package com.Utc.RutaExpress.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Sucursal;
import com.Utc.RutaExpress.repository.SucursalRepository;
import com.Utc.RutaExpress.service.SucursalService;

@Service
public class SucursalServiceImpl implements SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    @Override
    public List<Sucursal> listarTodos() {
        return sucursalRepository.findAll();
    }

    @Override
    public Sucursal buscarPorId(Long id) {
        return sucursalRepository.findById(id).orElse(null);
    }

    @Override
    public Sucursal guardar(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    @Override
    public Sucursal actualizar(Long id, Sucursal sucursal) {

        Sucursal existente = sucursalRepository.findById(id).orElse(null);

        if (existente != null) {

            existente.setNombre(sucursal.getNombre());
            existente.setCiudad(sucursal.getCiudad());
            existente.setDireccion(sucursal.getDireccion());

            return sucursalRepository.save(existente);

        }

        return null;
    }

    @Override
    public void eliminar(Long id) {
        sucursalRepository.deleteById(id);
    }

}