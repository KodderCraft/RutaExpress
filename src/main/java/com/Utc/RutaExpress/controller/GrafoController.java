package com.Utc.RutaExpress.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Utc.RutaExpress.DTO.RutaRequestDTO;
import com.Utc.RutaExpress.DTO.RutaResponseDTO;
import com.Utc.RutaExpress.entity.Direccion;
import com.Utc.RutaExpress.grafo.modelo.Nodo;
import com.Utc.RutaExpress.service.GrafoService;

@Controller
@RequestMapping("/grafo")
public class GrafoController {

    private final GrafoService grafoService;

    public GrafoController(GrafoService grafoService) {
        this.grafoService = grafoService;
    }

    @GetMapping
    public String mostrarMapa() {
        return "grafo/grafo";
    }

    @GetMapping("/ciudades")
    @ResponseBody
    public List<Direccion> obtenerCiudades() {

        return grafoService.obtenerCiudades();

    }

    

    @PostMapping("/calcular")
    @ResponseBody
    public RutaResponseDTO calcularRuta(@RequestBody RutaRequestDTO request){

        List<Nodo> camino = grafoService.calcularRuta(

                request.getOrigenId(),
                request.getParadas(),
                request.getDestinoId()

        );

        List<String> ciudades = camino.stream()

                .map((Nodo n) -> n.getNombre())
                .toList();

        return new RutaResponseDTO(ciudades);

    }

}