package com.Utc.RutaExpress.service;

import com.Utc.RutaExpress.entity.Ciudad;
import com.Utc.RutaExpress.entity.Direccion;
import com.Utc.RutaExpress.repository.CiudadRepository;
import com.Utc.RutaExpress.repository.DireccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    public Direccion registrar(String nombreCiudad, String telefono, String direccionExacta, Double latitud, Double longitud) {
        Ciudad ciudad = ciudadRepository.findByNombreIgnoreCase(nombreCiudad)
                .orElseGet(() -> ciudadRepository.save(new Ciudad(nombreCiudad)));

        Direccion direccion = new Direccion(ciudad, telefono, direccionExacta, latitud, longitud);
        return direccionRepository.save(direccion);
    }
}