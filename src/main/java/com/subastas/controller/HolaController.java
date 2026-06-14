package com.subastas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaController {

    @GetMapping("/")
    @ResponseBody
    public String hola() {
        return "Bienvenido al sistema de subastas online";
    }
}