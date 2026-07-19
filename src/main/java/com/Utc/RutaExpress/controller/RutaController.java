package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Utc.RutaExpress.DTO.RutaDTO;
import com.Utc.RutaExpress.service.RutaService;


@Controller
@RequestMapping("/rutas")
public class RutaController {
    private final RutaService rutaService;

    public RutaController(RutaService rutaService){

        this.rutaService = rutaService;

    }

    @PostMapping("/guardar")
    @ResponseBody
    public String guardar(@RequestBody RutaDTO dto){

        rutaService.guardar(dto);

        return "OK";
    }

    //Prueba
    @GetMapping("/prueba")
    @ResponseBody
    public String prueba() {
        return "RutaController funcionando";
    }
    
}
