package com.Utc.RutaExpress.service;

import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.DTO.RutaDTO;
import com.Utc.RutaExpress.entity.Ruta;
import com.Utc.RutaExpress.repository.DireccionRepository;
import com.Utc.RutaExpress.repository.RutaRepository;

@Service
public class RutaService {
    private final RutaRepository rutaRepository;
    private final DireccionRepository direccionRepository;

    public RutaService(RutaRepository rutaRepository,
                       DireccionRepository direccionRepository){

        this.rutaRepository = rutaRepository;
        this.direccionRepository = direccionRepository;

    }

    public void guardar(RutaDTO dto){

        Ruta ruta = new Ruta();

        ruta.setOrigen(
                direccionRepository.findById(dto.getOrigenId()).orElseThrow());

        ruta.setDestino(
                direccionRepository.findById(dto.getDestinoId()).orElseThrow());

        ruta.setDistancia(dto.getDistancia());

        ruta.setTiempoEstimado(dto.getTiempoEstimado());

        ruta.setActiva(true);

        rutaRepository.save(ruta);

    }

    
}
