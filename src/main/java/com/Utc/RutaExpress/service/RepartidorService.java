package com.Utc.RutaExpress.service;

import org.springframework.stereotype.Service;

import com.Utc.RutaExpress.entity.Repartidor;
import com.Utc.RutaExpress.repository.RepartidorRepository;

import java.util.Optional;

@Service
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;

    public RepartidorService(RepartidorRepository repoRepar){
        this.repartidorRepository = repoRepar;
    }

    public Repartidor guardarRepartidor(Repartidor repartidor){
        return repartidorRepository.save(repartidor);
    }


    public Optional<Repartidor> buscarPorUsuarioId(Long usuarioId){
        return repartidorRepository.findByUsuario_Id(usuarioId);
    }

 }

