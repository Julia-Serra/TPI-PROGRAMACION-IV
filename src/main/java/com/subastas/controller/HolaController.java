package com.subastas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HolaController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/index.html";
    }
}