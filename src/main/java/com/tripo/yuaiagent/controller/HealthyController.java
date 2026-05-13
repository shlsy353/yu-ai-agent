package com.tripo.yuaiagent.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/healthy")
public class HealthyController {


    @GetMapping
    public String healthy() {
        return "healthy";
    }
}
