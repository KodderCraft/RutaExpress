package com.Utc.RutaExpress.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Envio;
import com.Utc.RutaExpress.repository.EnvioRepository;
import com.Utc.RutaExpress.service.EnvioService;

@Service
public class EnvioServiceImpl implements EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Override
    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }

    @Override
    public Envio buscarPorId(Long id) {
        return envioRepository.findById(id).orElse(null);
    }

    @Override
    public Envio guardar(Envio envio) {
        return envioRepository.save(envio);
    }

    @Override
    public Envio actualizar(Long id, Envio envio) {

        Envio existente = envioRepository.findById(id).orElse(null);

        if (existente != null) {

            existente.setCodigoGuia(envio.getCodigoGuia());
            existente.setRemitente(envio.getRemitente());
            existente.setDestinatario(envio.getDestinatario());
            existente.setDireccionEntrega(envio.getDireccionEntrega());
            existente.setSucursalOrigen(envio.getSucursalOrigen());
            existente.setSucursalDestino(envio.getSucursalDestino());
            existente.setRepartidor(envio.getRepartidor());
            existente.setPeso(envio.getPeso());
            existente.setTipoServicio(envio.getTipoServicio());
            existente.setCostoTotal(envio.getCostoTotal());
            existente.setEstado(envio.getEstado());
            existente.setFechaEntrega(envio.getFechaEntrega());

            return envioRepository.save(existente);

        }

        return null;
    }

    @Override
    public void eliminar(Long id) {
        envioRepository.deleteById(id);
    }

}