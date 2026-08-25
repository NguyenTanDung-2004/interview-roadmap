package com.leon.spring_boot_base_poc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SayHelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
}