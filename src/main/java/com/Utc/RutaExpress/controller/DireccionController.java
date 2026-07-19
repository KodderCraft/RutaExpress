package com.Utc.RutaExpress.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Utc.RutaExpress.entity.Direccion;
import com.Utc.RutaExpress.repository.DireccionRepository;

@RestController
@RequestMapping("/direcciones")
public class DireccionController {

    private final DireccionRepository direccionRepository;

    public DireccionController(DireccionRepository direccionRepository) {
        this.direccionRepository = direccionRepository;
    }

    @GetMapping("/listar")
    public List<Direccion> listar() {

        return direccionRepository.findAll();

    }

}