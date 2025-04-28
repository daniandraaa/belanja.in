package com.ecommerce.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/buyer/index.html";
    }

    @GetMapping("/buyer")
    public String redirectBuyer() {
        return "redirect:/buyer/index.html";
    }

    @GetMapping("/seller")
    public String redirectSeller() {
        return "redirect:/seller/dashboard.html";
    }
}
