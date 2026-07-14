package com.Utc.RutaExpress.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Pago;
import com.Utc.RutaExpress.repository.PagoRepository;
import com.Utc.RutaExpress.service.PagoService;

@Service
public class PagoServiceImpl implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Override
    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    @Override
    public Pago buscarPorId(Long id) {
        return pagoRepository.findById(id).orElse(null);
    }

    @Override
    public Pago guardar(Pago pago) {
        return pagoRepository.save(pago);
    }

    @Override
    public Pago actualizar(Long id, Pago pago) {

        Pago existente = pagoRepository.findById(id).orElse(null);

        if (existente != null) {

            existente.setEnvio(pago.getEnvio());
            existente.setMetodo(pago.getMetodo());
            existente.setMonto(pago.getMonto());
            existente.setEstado(pago.getEstado());

            return pagoRepository.save(existente);

        }

        return null;
    }

    @Override
    public void eliminar(Long id) {
        pagoRepository.deleteById(id);
    }

}