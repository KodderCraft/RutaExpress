package com.Utc.RutaExpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class controller {

    @GetMapping()
    public String index() {
        return "index";
    }
    
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
